package cliente;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import modelo.Ticket;

import java.util.Timer;
import java.util.TimerTask;

public class TicketDisplay {
    private static TicketDisplay instance;
    private Stage stage;
    private Label lblTicket;
    private Label lblMesa;
    private Timer resetTimer;

    private TicketDisplay() {
        stage = new Stage();
        lblTicket = new Label("ESPERE SU TURNO...");
        lblTicket.setFont(new Font(24));
        lblTicket.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        lblMesa = new Label("");
        lblMesa.setFont(new Font(20));
        lblMesa.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        VBox root = new VBox(20, lblTicket, lblMesa);
        root.setAlignment(Pos.CENTER);
        root.setMinSize(400, 300);

        // Imagen de fondo
        try {
            Image backgroundImage = new Image(getClass().getResourceAsStream("/images/resourcesimages.png"));
            BackgroundImage bgImage = new BackgroundImage(
                    backgroundImage,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(100, 100, true, true, false, true)
            );
            root.setBackground(new Background(bgImage));
        } catch (Exception e) {
            root.setStyle("-fx-background-color: #003066; -fx-padding: 30px;");
            System.err.println("Error al cargar la imagen de fondo: " + e.getMessage());
        }

        Scene scene = new Scene(root);
        stage.setScene(scene);

        resetTimer = new Timer();

        // Timer para consultar ticket actual cada 2 segundos
        Timer consultaTimer = new Timer(true);
        consultaTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    Ticket ticketActual = ClienteUtils.obtenerTicketActual();
                    if (ticketActual != null) {
                        javafx.application.Platform.runLater(() -> {
                            actualizarTicket(ticketActual.getCodigo(), ticketActual.getMesaAsignada());
                        });
                    }
                } catch (Exception e) {
                    System.err.println("Error al consultar ticket actual: " + e.getMessage());
                }
            }
        }, 0, 2000);
    }

    public static TicketDisplay getInstance() {
        if (instance == null) {
            instance = new TicketDisplay();
        }
        return instance;
    }

    public void mostrar() {
        if (!stage.isShowing()) {
            stage.show();
        }
    }

    public void actualizarTicket(String codigoTicket, String mesa) {
        lblTicket.setText("TICKET: " + codigoTicket);
        lblMesa.setText("DIRÍJASE A: Mesa " + mesa);

        // Cancelar y reiniciar el temporizador de reseteo
        resetTimer.cancel();
        resetTimer = new Timer();

        resetTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                javafx.application.Platform.runLater(() -> {
                    lblTicket.setText("ESPERE SU TURNO...");
                    lblMesa.setText("");
                });
            }
        }, 7000);
    }
}
