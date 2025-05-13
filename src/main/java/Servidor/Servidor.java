package Servidor;

import modelo.Ticket;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Servidor principal que gestiona los tickets y las conexiones de clientes
 */
public class Servidor {
    // Cola concurrente para almacenar tickets (segura para hilos)
    private static Queue<Ticket> tickets = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) throws IOException {
        // Crea el socket del servidor en el puerto 5000
        ServerSocket serverSocket = new ServerSocket(5000);
        System.out.println("Servidor iniciado en el puerto 5000...");

        // Bucle infinito para aceptar conexiones
        while (true) {
            Socket socket = serverSocket.accept();  // Espera conexión entrante
            // Crea un nuevo hilo para manejar al cliente
            new Thread(new ClienteHandler(socket)).start();
        }
    }

    /**
     * Clase interna que maneja la comunicación con cada cliente
     */
    static class ClienteHandler implements Runnable {
        private Socket socket;  // Socket de conexión con el cliente

        public ClienteHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                    // Flujos de entrada/salida para comunicación con el cliente
                    ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
                    ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream())
            ) {
                // Lee la acción solicitada por el cliente
                String accion = (String) entrada.readObject();

                // Switch para manejar diferentes acciones
                switch (accion) {
                    case "crear":
                        String servicio = (String) entrada.readObject();
                        Ticket ticket = new Ticket(servicio);
                        tickets.add(ticket);
                        salida.writeObject(ticket);
                        break;

                    case "listar":
                        // Envía lista completa de tickets
                        salida.writeObject(new ArrayList<>(tickets));
                        break;

                    case "listarPendientes":
                        // Filtra y envía solo tickets pendientes
                        List<Ticket> pendientes = tickets.stream()
                                .filter(t -> t.getEstado().equals("pendiente"))
                                .collect(Collectors.toList());
                        salida.writeObject(pendientes);
                        break;

                    // En el case "atender" del servidor:
                    case "atender":
                        String codigoAtender = (String) entrada.readObject();
                        String mesa = (String) entrada.readObject(); // Recibe el número de mesa

                        tickets.stream()
                                .filter(t -> t.getCodigo().equals(codigoAtender))
                                .findFirst()
                                .ifPresent(t -> {
                                    t.setEstado("atendiendo");
                                    t.setMesaAsignada(mesa);
                                });

                        CreadorLogs.log("TICKET ATENDIDO - " + codigoAtender +
                                " | Mesa: " + mesa);
                        break;

                    case "finalizar":
                        // Cambia estado a "atendido"
                        String codigoFinalizar = (String) entrada.readObject();
                        cambiarEstado(codigoFinalizar, "atendido");
                        CreadorLogs.log("TICKET FINALIZADO - " + codigoFinalizar +
                                " | Nuevo estado: atendido");
                        break;

                    default:
                        // Acción no reconocida
                        salida.writeObject("Accion no reconocida");
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        // Método auxiliar para cambiar estado de un ticket
        private void cambiarEstado(String codigo, String nuevoEstado) {
            tickets.stream()
                    .filter(t -> t.getCodigo().equals(codigo))
                    .findFirst()
                    .ifPresent(t -> {
                        t.setEstado(nuevoEstado);
                        CreadorLogs.log("Ticket " + codigo + " cambiado a estado: " + nuevoEstado);
                    });
        }
    }
}