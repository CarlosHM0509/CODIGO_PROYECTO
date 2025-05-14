module SistemaTicketsBanco {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;

    exports cliente;
    exports modelo;
    exports Servidor;
    opens cliente to javafx.graphics;
    opens modelo to javafx.base;
}
