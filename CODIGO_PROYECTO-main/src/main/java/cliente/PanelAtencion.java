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
    private Stage stageOperador;
    private Timeline actualizador;

    public PanelAtencion() {
        super(10);
        setPadding(new Insets(15));
        crearUI();
        iniciarActualizacionPeriodica();
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

    private void iniciarActualizacionPeriodica() {
        actualizador = new Timeline(
                new KeyFrame(Duration.seconds(3), e -> actualizarTickets())
        );
        actualizador.setCycleCount(Timeline.INDEFINITE);
        actualizador.play();
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
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("atender");
            out.writeObject(seleccionado.getCodigo());
            out.writeObject(mesa);

            List<Ticket> ticketsActualizados = (List<Ticket>) in.readObject();
            actualizarTabla(ticketsActualizados);

            TicketDisplay display = TicketDisplay.getInstance();
            display.mostrar(); // Mostrar la pantalla si aún no se ha mostrado
            display.actualizarTicket(seleccionado.getCodigo(), mesa);

            cajaInput.clear();

        } catch (IOException | ClassNotFoundException e) {
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
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("finalizar");
            out.writeObject(seleccionado.getCodigo());

            List<Ticket> ticketsActualizados = (List<Ticket>) in.readObject();
            actualizarTabla(ticketsActualizados);

        } catch (IOException | ClassNotFoundException e) {
            mostrarAlerta("Error al finalizar ticket: " + e.getMessage());
        }
    }

    private void actualizarTickets() {
        try (Socket socket = new Socket(HOST, PUERTO);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("listar"); // <<< CAMBIO AQUÍ
            List<Ticket> tickets = (List<Ticket>) in.readObject();
            actualizarTabla(tickets);


        } catch (IOException | ClassNotFoundException e) {
            // Evitamos mostrar alerta cada 3 segundos, solo logueamos para depuración
            System.err.println("Error al actualizar tickets: " + e.getMessage());
        }
    }

    public void actualizarTabla(List<Ticket> tickets) {
        tablaTickets.getItems().clear(); // <--- Añade esta línea
        ObservableList<Ticket> items = FXCollections.observableArrayList(tickets);
        tablaTickets.setItems(items);
    }


    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
