package Servidor;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CreadorLogs {
    private static final String RUTA_LOG = "HISTORIAL DE TICKETS";
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static synchronized void log(String mensaje) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(RUTA_LOG, true))) {
            String entrada = String.format("[%s] %s%n",
                    LocalDateTime.now().format(FORMATO_FECHA),
                    mensaje);
            writer.write(entrada);
            System.out.print(entrada); // También imprime en consola
        } catch (IOException e) {
            System.err.println("Error al escribir en el log: " + e.getMessage());
        }
    }
}