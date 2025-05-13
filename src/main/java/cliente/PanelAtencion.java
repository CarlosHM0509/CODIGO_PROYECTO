package cliente;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import modelo.Ticket;
import java.io.*;
import java.net.Socket;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;


/**
 * Panel de atención de tickets para la interfaz gráfica
 */
public class PanelAtencion extends VBox {
    private static final String HOST = "localhost";  // Dirección del servidor
    private static final int PUERTO = 5000;  // Puerto del servidor
    private TableView<Ticket> tablaTickets;  // Tabla para mostrar tickets

    public PanelAtencion() {
        super(10);
        setPadding(new Insets(15));
        crearUI();
        actualizarTicketsPeriodicamente();  // ahora se actualiza solo
    }

    private void actualizarTicketsPeriodicamente() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(3), e -> actualizarTickets())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }


    // Configura los elementos de la interfaz
    private void crearUI() {
        // Configuración de columnas para la tabla
        TableColumn<Ticket, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));

        TableColumn<Ticket, String> colServicio = new TableColumn<>("Servicio");
        colServicio.setCellValueFactory(new PropertyValueFactory<>("servicio"));

        TableColumn<Ticket, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Crea la tabla y añade columnas
        tablaTickets = new TableView<>();
        tablaTickets.getColumns().addAll(colCodigo, colServicio, colEstado);

        // Botones con sus acciones
        Button btnActualizar = new Button("Actualizar Lista");
        btnActualizar.setOnAction(e -> actualizarTickets());

        Button btnAtender = new Button("Atender Ticket");
        btnAtender.setOnAction(e -> cambiarEstadoTicket("atendiendo"));

        Button btnFinalizar = new Button("Finalizar Ticket");
        btnFinalizar.setOnAction(e -> cambiarEstadoTicket("atendido"));

        // Panel horizontal para botones
        HBox botonera = new HBox(10, btnActualizar, btnAtender, btnFinalizar);
        // Añade todos los componentes al panel principal
        getChildren().addAll(new Label("Tickets Pendientes:"), tablaTickets, botonera);
    }

    // Actualiza la lista de tickets desde el servidor
    private void actualizarTickets() {
        try (Socket socket = new Socket(HOST, PUERTO);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("listar");  // Solicita lista de tickets
            List<Ticket> tickets = (List<Ticket>) in.readObject();  // Recibe la lista
            // Convierte a ObservableList para la tabla
            ObservableList<Ticket> items = FXCollections.observableArrayList(tickets);
            tablaTickets.setItems(items);  // Asigna a la tabla

        } catch (IOException | ClassNotFoundException e) {
            mostrarAlerta("Error al cargar tickets: " + e.getMessage());
        }
    }

    // Cambia el estado de un ticket seleccionado
    private void cambiarEstadoTicket(String nuevoEstado) {
        Ticket seleccionado = tablaTickets.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            try (Socket socket = new Socket(HOST, PUERTO);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

                // Envía comando según el nuevo estado
                out.writeObject(nuevoEstado.equals("atendiendo") ? "atender" : "finalizar");
                out.writeObject(seleccionado.getCodigo());  // Envía código del ticket
                actualizarTickets();  // Refresca la lista

            } catch (IOException e) {
                mostrarAlerta("Error al cambiar estado: " + e.getMessage());
            }
        } else {
            mostrarAlerta("Seleccione un ticket primero");
        }
    }

    // Muestra mensajes de alerta al usuario
    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Advertencia");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}