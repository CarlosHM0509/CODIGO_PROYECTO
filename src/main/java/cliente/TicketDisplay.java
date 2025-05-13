package cliente;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
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

        StackPane textContainer = new StackPane();
        textContainer.getChildren().addAll(lblTicket, lblMesa);
        textContainer.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-padding: 20px;");

        VBox root = new VBox(20, lblTicket, lblMesa);
        root.setAlignment(Pos.CENTER);
        root.setMinSize(400, 300);

        // Cargar la imagen de fondo (reemplaza "background.jpg" con tu archivo)
        try {
            Image backgroundImage = new Image(getClass().getResourceAsStream("/images/resourcesimages.png"));
            BackgroundImage bgImage = new BackgroundImage(
                    backgroundImage,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(
                            100,                    // Ancho al 100%
                            100,                    // Alto al 100%
                            true,                   // Forzar ajuste al ancho
                            true,                   // Forzar ajuste al alto
                            false,                  // No mantener relación de aspecto (puede distorsionarse)
                            true                    // Cubrir toda el área
                    )
            );
            root.setBackground(new Background(bgImage));
        } catch (Exception e) {
            // Si falla la carga de la imagen, usa un color de fondo sólido
            root.setStyle("-fx-background-color: #003066; -fx-padding: 30px;");
            System.err.println("Error al cargar la imagen de fondo: " + e.getMessage());
        }

        Scene scene = new Scene(root);
        stage.setScene(scene);

        resetTimer = new Timer();
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
        lblMesa.setText("DIRIJASE A: Mesa " + mesa);

        // Cancelar cualquier temporizador previo
        resetTimer.cancel();
        resetTimer = new Timer();

        // Programar la tarea para resetear después de 7 segundos
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