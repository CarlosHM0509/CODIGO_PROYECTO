package Servidor;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Clase para registrar eventos en un archivo de log
 */
public class CreadorLogs {
    private static final String RUTA_LOG = "HISTORIAL DE TICKETS";  // Nombre del archivo de log
    private static final DateTimeFormatter FORMATO_FECHA =  // Formato para fechas
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Método sincronizado para evitar problemas con múltiples hilos
    public static synchronized void log(String mensaje) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(RUTA_LOG, true))) {
            // Formatea la entrada con fecha y mensaje
            String entrada = String.format("[%s] %s%n",
                    LocalDateTime.now().format(FORMATO_FECHA),
                    mensaje);
            writer.write(entrada);  // Escribe en archivo
            System.out.print(entrada);  // También muestra en consola
        } catch (IOException e) {
            System.err.println("Error al escribir en el log: " + e.getMessage());
        }
    }
}