package cliente;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import modelo.Ticket;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class Cliente extends Application {
    private static final String HOST = "localhost";
    private static final int PUERTO = 5000;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Sistema de Tickets - Banco");

        TabPane tabPane = new TabPane();

        // Pestaña 1: Generación de Tickets
        Tab tabGenerar = new Tab("Generar Ticket");
        tabGenerar.setContent(crearPanelGeneracion());
        tabGenerar.setClosable(false);

        // Pestaña 2: Panel de Atención
        Tab tabAtencion = new Tab("Atención de Tickets");
        tabAtencion.setContent(new PanelAtencion());
        tabAtencion.setClosable(false);

        tabPane.getTabs().addAll(tabGenerar, tabAtencion);

        Scene scene = new Scene(tabPane, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox crearPanelGeneracion() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));

        ComboBox<String> comboServicios = new ComboBox<>();
        comboServicios.getItems().addAll(
                "Servicio al Cliente",
                "Cuenta Monetaria",
                "Cuenta Ahorro",
                "Tarjeta de Credito",
                "Cerrar Cuenta"
        );
        comboServicios.setPromptText("Seleccione un servicio");

        Button btnSolicitar = new Button("Solicitar Ticket");
        Label resultadoLabel = new Label();

        btnSolicitar.setOnAction(e -> {
            String servicioSeleccionado = comboServicios.getValue();
            if (servicioSeleccionado != null) {
                solicitarTicket(servicioSeleccionado, resultadoLabel);
            } else {
                resultadoLabel.setText("Debe seleccionar un servicio.");
            }
        });

        panel.getChildren().addAll(
                new Label("Seleccione el servicio requerido:"),
                comboServicios,
                btnSolicitar,
                resultadoLabel
        );

        return panel;
    }

    private void solicitarTicket(String servicio, Label resultadoLabel) {
        try (Socket socket = new Socket(HOST, PUERTO);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("crear");
            out.writeObject(servicio);

            Object respuesta = in.readObject();
            if (respuesta instanceof Ticket) {
                Ticket ticket = (Ticket) respuesta;
                resultadoLabel.setText("Ticket generado: " + ticket.getCodigo());
            } else {
                resultadoLabel.setText("Error: Respuesta inesperada del servidor");
            }

        } catch (ClassNotFoundException e) {
            resultadoLabel.setText("Error: Versión incompatible con el servidor");
            e.printStackTrace();
        } catch (IOException e) {
            resultadoLabel.setText("Error de conexión con el servidor");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Clase interna para el panel de atención
    class PanelAtencion extends VBox {
        private TableView<Ticket> tablaTickets;

        public PanelAtencion() {
            super(10);
            setPadding(new Insets(15));
            crearUI();
            actualizarTickets();
        }

        private void crearUI() {
            // Configuración de columnas
            TableColumn<Ticket, String> colCodigo = new TableColumn<>("Código");
            colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));

            TableColumn<Ticket, String> colServicio = new TableColumn<>("Servicio");
            colServicio.setCellValueFactory(new PropertyValueFactory<>("servicio"));

            TableColumn<Ticket, String> colEstado = new TableColumn<>("Estado");
            colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

            tablaTickets = new TableView<>();
            tablaTickets.getColumns().addAll(colCodigo, colServicio, colEstado);

            // Botones de acción
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
}
