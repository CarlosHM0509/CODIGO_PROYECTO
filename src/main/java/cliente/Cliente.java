package cliente;

import javafx.application.Application;
import javafx.stage.Stage;

public class Cliente extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        new GeneradorTickets().mostrar();
        new PanelAtencion().mostrarVentanaOperador();
        TicketDisplay.getInstance().mostrar();
    }
}