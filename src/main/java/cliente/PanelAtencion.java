package cliente;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;  // ¡Este import es crucial!
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import modelo.Ticket;
import java.io.*;
import java.net.Socket;
import java.util.List;

public class PanelAtencion extends VBox {
    private static final String HOST = "localhost";
    private static final int PUERTO = 5000;
    private TableView<Ticket> tablaTickets;

    public PanelAtencion() {
        super(10);
        setPadding(new Insets(15));  // Usando la clase Insets correctamente
        crearUI();
        actualizarTickets();
    }

    private void crearUI() {
        TableColumn<Ticket, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));

        TableColumn<Ticket, String> colServicio = new TableColumn<>("Servicio");
        colServicio.setCellValueFactory(new PropertyValueFactory<>("servicio"));

        TableColumn<Ticket, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        tablaTickets = new TableView<>();
        tablaTickets.getColumns().addAll(colCodigo, colServicio, colEstado);

        Button btnActualizar = new Button("Actualizar Lista");
        btnActualizar.setOnAction(e -> actualizarTickets());

        Button btnAtender = new Button("Atender Ticket");
        btnAtender.setOnAction(e -> cambiarEstadoTicket("atendiendo"));

        Button btnFinalizar = new Button("Finalizar Ticket");
        btnFinalizar.setOnAction(e -> cambiarEstadoTicket("atendido"));

        HBox botonera = new HBox(10, btnActualizar, btnAtender, btnFinalizar);
        getChildren().addAll(new Label("Tickets Pendientes:"), tablaTickets, botonera);
    }

    private void actualizarTickets() {
        try (Socket socket = new Socket(HOST, PUERTO);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("listar");
            List<Ticket> tickets = (List<Ticket>) in.readObject();
            ObservableList<Ticket> items = FXCollections.observableArrayList(tickets);
            tablaTickets.setItems(items);

        } catch (IOException | ClassNotFoundException e) {
            mostrarAlerta("Error al cargar tickets: " + e.getMessage());
        }
    }

    private void cambiarEstadoTicket(String nuevoEstado) {
        Ticket seleccionado = tablaTickets.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            try (Socket socket = new Socket(HOST, PUERTO);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

                out.writeObject(nuevoEstado.equals("atendiendo") ? "atender" : "finalizar");
                out.writeObject(seleccionado.getCodigo());
                actualizarTickets();

            } catch (IOException e) {
                mostrarAlerta("Error al cambiar estado: " + e.getMessage());
            }
        } else {
            mostrarAlerta("Seleccione un ticket primero");
        }
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Advertencia");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

