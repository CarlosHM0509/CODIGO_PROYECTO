package cliente;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import modelo.Ticket;
import java.io.*;
import java.net.Socket;

public class GeneradorTickets {
    private static final String HOST = "localhost";
    private static final int PUERTO = 5000;
    private Stage stage;

    public void mostrar() {
        stage = new Stage();
        stage.setTitle("Generar Nuevos Tickets");

        ComboBox<String> cbServicios = new ComboBox<>();
        cbServicios.getItems().addAll("Retiro", "Depósito", "Consulta", "Pago", "Otros");
        cbServicios.setPromptText("Seleccione servicio");

        Button btnGenerar = new Button("Generar Ticket");
        btnGenerar.setOnAction(e -> {
            String servicioSeleccionado = cbServicios.getValue();
            if (servicioSeleccionado != null) {
                try {
                    generarTicket(servicioSeleccionado);
                } catch (IOException ex) {
                    mostrarAlerta("Error al conectar con el servidor: " + ex.getMessage());
                }
            } else {
                mostrarAlerta("Por favor seleccione un servicio");
            }
        });

        VBox layout = new VBox(10, cbServicios, btnGenerar);
        layout.setPadding(new Insets(15));

        stage.setScene(new Scene(layout, 300, 200));
        stage.show();
    }

    private void generarTicket(String servicio) throws IOException {
        try (Socket socket = new Socket(HOST, PUERTO);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("crear");
            out.writeObject(servicio);
            Ticket ticket = (Ticket) in.readObject();
            mostrarAlerta("Ticket generado: " + ticket.getCodigo());

        } catch (ClassNotFoundException e) {
            mostrarAlerta("Error en el formato de respuesta del servidor");
        }
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}