package modelo;

import java.io.Serializable;

/**
 * Clase que representa un Ticket en el sistema
 * Implementa Serializable para permitir su transmisión por red
 */
public class Ticket implements Serializable {
    private static final long serialVersionUID = 1L;  // Versión para control de serialización
    private static int contador = 1;  // Contador estático para números de ticket
    private int numero;  // Número único del ticket
    private String servicio;  // Tipo de servicio solicitado
    private String estado;  // Estado actual del ticket (pendiente, atendiendo, atendido)

    // Constructor que inicializa un nuevo ticket
    public Ticket(String servicio) {
        this.servicio = servicio;
        this.numero = contador++;  // Asigna número y luego incrementa el contador
        this.estado = "pendiente";  // Estado inicial siempre es pendiente
    }

    // Genera un código único para el ticket (ej: "S-1")
    public String getCodigo() {
        return servicio.substring(0, 1).toUpperCase() + "-" + numero;
    }

    // Getters y setters básicos
    public String getServicio() {
        return servicio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    // Representación textual del ticket
    @Override
    public String toString() {
        return getCodigo() + " - " + servicio + " [" + estado + "]";
    }
}
