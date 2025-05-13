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
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PanelAtencion extends VBox {
    private static final String HOST = "localhost";
    private static final int PUERTO = 5000;
    private TableView<Ticket> tablaTickets;
    private TextField cajaInput;
    private TicketDisplay display;
    private Stage stageOperador;

    public PanelAtencion() {
        super(10);
        setPadding(new Insets(15));

        // ✅ Usar el Singleton correctamente
        display = TicketDisplay.getInstance();
        display.mostrar();

        crearUI();
        actualizarTicketsPeriodicamente();
    }

    public void mostrarVentanaOperador() {
        if (stageOperador == null) {
            stageOperador = new Stage();
            Scene escena = new Scene(this, 600, 400);
            stageOperador.setScene(escena);
            stageOperador.setTitle("Panel de Operador - Banco");
            stageOperador.setOnCloseRequest(e -> System.exit(0));
            stageOperador.show();
        }
    }

    private void actualizarTicketsPeriodicamente() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(3), e -> actualizarTickets())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void crearUI() {
        TableColumn<Ticket, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));

        TableColumn<Ticket, String> colServicio = new TableColumn<>("Servicio");
        colServicio.setCellValueFactory(new PropertyValueFactory<>("servicio"));

        TableColumn<Ticket, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        TableColumn<Ticket, String> colMesa = new TableColumn<>("Mesa Asignada");
        colMesa.setCellValueFactory(new PropertyValueFactory<>("mesaAsignada"));

        tablaTickets = new TableView<>();
        tablaTickets.getColumns().addAll(colCodigo, colServicio, colEstado, colMesa);

        cajaInput = new TextField();
        cajaInput.setPromptText("Ingrese número de caja/mesa");
        cajaInput.setPrefWidth(150);

        Button btnAtender = new Button("Atender Ticket");
        btnAtender.setOnAction(e -> atenderTicket());

        Button btnFinalizar = new Button("Finalizar Atención");
        btnFinalizar.setOnAction(e -> finalizarTicket());

        HBox panelBotones = new HBox(10, cajaInput, btnAtender, btnFinalizar);

        getChildren().addAll(
                new Label("Sistema de Gestión de Tickets - Banco"),
                tablaTickets,
                panelBotones
        );
    }

    private void atenderTicket() {
        Ticket seleccionado = tablaTickets.getSelectionModel().getSelectedItem();
        String mesa = cajaInput.getText().trim();

        if (seleccionado == null) {
            mostrarAlerta("Por favor seleccione un ticket");
            return;
        }

        if (mesa.isEmpty()) {
            mostrarAlerta("Ingrese un número de caja/mesa");
            return;
        }

        try (Socket socket = new Socket(HOST, PUERTO);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            out.writeObject("atender");
            out.writeObject(seleccionado.getCodigo());
            out.writeObject(mesa);

            // ✅ Actualizar pantalla del cliente
            display.actualizarTicket(seleccionado.getCodigo(), mesa);
            actualizarTickets();

        } catch (IOException e) {
            mostrarAlerta("Error al atender ticket: " + e.getMessage());
        }
    }

    private void finalizarTicket() {
        Ticket seleccionado = tablaTickets.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta("Por favor seleccione un ticket");
            return;
        }

        try (Socket socket = new Socket(HOST, PUERTO);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            out.writeObject("finalizar");
            out.writeObject(seleccionado.getCodigo());
            actualizarTickets();

        } catch (IOException e) {
            mostrarAlerta("Error al finalizar ticket: " + e.getMessage());
        }
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
            mostrarAlerta("Error al actualizar tickets: " + e.getMessage());
        }
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
