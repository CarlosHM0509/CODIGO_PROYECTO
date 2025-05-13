package Servidor;

import modelo.Ticket;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class Servidor {
    // Cola concurrente para almacenar tickets (segura para hilos)
    private static Queue<Ticket> tickets = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(5000);
        System.out.println("Servidor iniciado en el puerto 5000...");

        while (true) {
            Socket socket = serverSocket.accept();  // Espera una conexión entrante
            new Thread(new ClienteHandler(socket)).start();  // Hilo para manejar cada cliente
        }
    }

    // Clase interna que maneja la comunicación con cada cliente
    static class ClienteHandler implements Runnable {
        private Socket socket;

        public ClienteHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                    ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
                    ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream())
            ) {
                String accion = (String) entrada.readObject();  // Acción solicitada

                switch (accion) {
                    case "crear":
                        // Crear un nuevo ticket
                        String servicio = (String) entrada.readObject();
                        Ticket ticket = new Ticket(servicio);
                        tickets.add(ticket);
                        salida.writeObject(ticket);  // Devuelve ticket al cliente
                        break;

                    case "listar":
                        // Enviar tickets que están pendientes o en atención
                        List<Ticket> activos = tickets.stream()
                                .filter(t -> t.getEstado().equals("pendiente") || t.getEstado().equals("atendiendo"))
                                .collect(Collectors.toList());
                        salida.writeObject(activos);
                        break;


                    case "listarPendientes":
                        // Solo tickets pendientes
                        List<Ticket> pendientes = tickets.stream()
                                .filter(t -> t.getEstado().equals("pendiente"))
                                .collect(Collectors.toList());
                        salida.writeObject(pendientes);
                        break;

                    case "atender":
                        // Atender un ticket y asignarle una mesa/caja
                        String codigoAtender = (String) entrada.readObject();
                        String mesa = (String) entrada.readObject();
                        Ticket ticketAtendido = tickets.stream()
                                .filter(t -> t.getCodigo().equals(codigoAtender))
                                .findFirst()
                                .orElse(null);

                        if (ticketAtendido != null) {
                            ticketAtendido.setEstado("atendiendo");
                            ticketAtendido.setMesaAsignada(mesa);
                            CreadorLogs.log("TICKET ATENDIDO - " + codigoAtender + " | Mesa: " + mesa);
                            salida.writeObject(new ArrayList<>(tickets));
                        } else {
                            salida.writeObject("Ticket no encontrado");
                        }
                        break;

                    case "finalizar":
                        // Finalizar atención del ticket
                        String codigoFinalizar = (String) entrada.readObject();
                        Ticket ticketFinalizado = tickets.stream()
                                .filter(t -> t.getCodigo().equals(codigoFinalizar))
                                .findFirst()
                                .orElse(null);

                        if (ticketFinalizado != null) {
                            ticketFinalizado.setEstado("atendido");
                            CreadorLogs.log("TICKET FINALIZADO - " + codigoFinalizar + " | Nuevo estado: atendido");
                            salida.writeObject(new ArrayList<>(tickets));
                        } else {
                            salida.writeObject("Ticket no encontrado");
                        }
                        break;

                    default:
                        salida.writeObject("Accion no reconocida");
                        break;
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();  // Cierra conexión
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
