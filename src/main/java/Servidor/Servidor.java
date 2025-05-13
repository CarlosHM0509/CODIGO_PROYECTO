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
            new Thread(new ClienteHandler(socket)).start();  // Crea un nuevo hilo para manejar la conexión
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
                String accion = (String) entrada.readObject();  // Lee la acción del cliente

                switch (accion) {
                    case "crear":
                        // Crea un nuevo ticket
                        String servicio = (String) entrada.readObject();
                        Ticket ticket = new Ticket(servicio);
                        tickets.add(ticket);
                        salida.writeObject(ticket);  // Envía el ticket creado al cliente
                        break;

                    case "listar":
                        // Envía la lista completa de tickets al cliente
                        salida.writeObject(new ArrayList<>(tickets));
                        break;

                    case "listarPendientes":
                        // Filtra y envía solo los tickets pendientes
                        List<Ticket> pendientes = tickets.stream()
                                .filter(t -> t.getEstado().equals("pendiente"))
                                .collect(Collectors.toList());
                        salida.writeObject(pendientes);
                        break;

                    case "atender":
                        // Atender un ticket y asignar una mesa
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
                            // Envía la lista actualizada de tickets
                            salida.writeObject(new ArrayList<>(tickets));
                        } else {
                            salida.writeObject("Ticket no encontrado");
                        }
                        break;

                    case "finalizar":
                        // Finalizar el ticket
                        String codigoFinalizar = (String) entrada.readObject();
                        Ticket ticketFinalizado = tickets.stream()
                                .filter(t -> t.getCodigo().equals(codigoFinalizar))
                                .findFirst()
                                .orElse(null);

                        if (ticketFinalizado != null) {
                            ticketFinalizado.setEstado("atendido");
                            CreadorLogs.log("TICKET FINALIZADO - " + codigoFinalizar + " | Nuevo estado: atendido");
                            // Envía la lista actualizada de tickets
                            salida.writeObject(new ArrayList<>(tickets));
                        } else {
                            salida.writeObject("Ticket no encontrado");
                        }
                        break;

                    default:
                        salida.writeObject("Accion no reconocida");
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();  // Cierra el socket al finalizar la comunicación con el cliente
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
