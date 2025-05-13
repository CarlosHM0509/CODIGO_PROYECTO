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
import java.util.Properties;

public class Cliente extends Application {
    private static String HOST;
    private static int PUERTO;

    private PanelAtencion panelAtencion;

    private static void cargarConfiguracion() {
        Properties prop = new Properties();
        InputStream input = null;

        try {
            File configFile = new File("config.properties");
            if (configFile.exists()) {
                input = new FileInputStream(configFile);
            } else {
                input = Cliente.class.getClassLoader().getResourceAsStream("config.properties");
                if (input == null) {
                    throw new FileNotFoundException("Archivo config.properties no encontrado");
                }
            }

            prop.load(input);
            HOST = prop.getProperty("servidor.ip", "localhost");
            String puertoStr = prop.getProperty("servidor.puerto", "5000");

            try {
                PUERTO = Integer.parseInt(puertoStr);
            } catch (NumberFormatException e) {
                System.err.println("Puerto no válido, usando 5000 por defecto");
                PUERTO = 5000;
            }

        } catch (IOException ex) {
            System.err.println("No se pudo cargar config.properties, usando valores por defecto. Error: " + ex.getMessage());
            HOST = "localhost";
            PUERTO = 5000;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    System.err.println("Error al cerrar el archivo: " + e.getMessage());
                }
            }
        }
    }

    static {
        cargarConfiguracion();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Sistema de Tickets - Banco");

        TabPane tabPane = new TabPane();

        Tab tabGenerar = new Tab("Generar Ticket");
        tabGenerar.setContent(crearPanelGeneracion());
        tabGenerar.setClosable(false);

        Tab tabAtencion = new Tab("Atención de Tickets");
        panelAtencion = new PanelAtencion();
        tabAtencion.setContent(panelAtencion);
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

                if (panelAtencion != null) {
                    panelAtencion.actualizarTickets();
                }
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

    // Clase interna que representa la pestaña de atención
    class PanelAtencion extends VBox {
        private TableView<Ticket> tablaTickets;

        public PanelAtencion() {
            super(10);
            setPadding(new Insets(15));
            crearUI();
            actualizarTickets();
            iniciarActualizacionAutomatica();
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

        public void actualizarTickets() {
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

        private void iniciarActualizacionAutomatica() {
            javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.seconds(3), e -> actualizarTickets())
            );
            timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
            timeline.play();
        }
    }
}
