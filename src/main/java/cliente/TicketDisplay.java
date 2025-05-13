package cliente;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class TicketDisplay {
    private static TicketDisplay instance;
    private Stage stage;
    private Label lblTicket;
    private Label lblMesa;

    private TicketDisplay() {
        stage = new Stage();
        lblTicket = new Label("Esperando ticket...");
        lblTicket.setFont(new Font(24));
        lblTicket.setStyle("-fx-text-fill: white;");

        lblMesa = new Label("");
        lblMesa.setFont(new Font(20));
        lblMesa.setStyle("-fx-text-fill: white;");

        VBox root = new VBox(20, lblTicket, lblMesa);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #003366; -fx-padding: 30px;");
        root.setMinSize(400, 300);

        Scene scene = new Scene(root);
        stage.setScene(scene);
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
        lblTicket.setText("Ticket: " + codigoTicket);
        lblMesa.setText("Diríjase a: Mesa " + mesa);
    }
}
