package cliente;

import javafx.application.Application;
import javafx.stage.Stage;

public class Cliente extends Application {
    @Override
    public void start(Stage primaryStage) {
        new GeneradorTickets().mostrar();
        new PanelAtencion().mostrarVentanaOperador();
        TicketDisplay.getInstance().mostrar();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
