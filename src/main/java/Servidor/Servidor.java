package Servidor;

import modelo.Ticket;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;  // Import necesario

public class Servidor {
    private static Queue<Ticket> tickets = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(5000);
        System.out.println("Servidor iniciado en el puerto 5000...");

        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(new ClienteHandler(socket)).start();
        }
    }

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
                String accion = (String) entrada.readObject();

                switch (accion) {
                    case "crear":
                        String servicio = (String) entrada.readObject();
                        Ticket ticket = new Ticket(servicio);
                        tickets.add(ticket);
                        CreadorLogs.log("NUEVO TICKET - " + ticket.getCodigo() +
                                " | Servicio: " + servicio +
                                " | Estado: " + ticket.getEstado());

                        salida.writeObject(ticket);

                        break;

                    case "listar":
                        salida.writeObject(new ArrayList<>(tickets));
                        break;

                    case "listarPendientes":
                        List<Ticket> pendientes = tickets.stream()
                                .filter(t -> t.getEstado().equals("pendiente"))
                                .collect(Collectors.toList());  // Uso de Collectors
                        salida.writeObject(pendientes);
                        break;

                    case "atender":
                        String codigoAtender = (String) entrada.readObject();
                        cambiarEstado(codigoAtender, "atendiendo");
                        CreadorLogs.log("TICKET ATENDIDO - " + codigoAtender +
                                " | Nuevo estado: atendiendo");
                        break;

                    case "finalizar":
                        String codigoFinalizar = (String) entrada.readObject();
                        cambiarEstado(codigoFinalizar, "atendido");
                        CreadorLogs.log("TICKET FINALIZADO - " + codigoFinalizar +
                                " | Nuevo estado: atendido");
                        break;

                    default:
                        salida.writeObject("Accion no reconocida");
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

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

