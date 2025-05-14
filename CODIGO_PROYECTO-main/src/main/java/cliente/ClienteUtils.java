package cliente;

import modelo.Ticket;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClienteUtils {
    public static Ticket obtenerTicketActual() throws Exception {
        try (Socket socket = new Socket("localhost", 5000);
             ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream())) {

            salida.writeObject("ticketActual");
            Object respuesta = entrada.readObject();

            if (respuesta instanceof Ticket) {
                return (Ticket) respuesta;
            } else {
                return null;
            }
        }
    }
}
