package modelo;

import java.io.Serializable;

public class Ticket implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int contador = 1;

    private final int numero;  // número único
    private final String servicio;  // tipo de servicio
    private String estado;
    private String mesaAsignada;

    private String codigoCache; // ya no es transient

    public Ticket(String servicio) {
        if (servicio == null || servicio.trim().isEmpty()) {
            throw new IllegalArgumentException("El servicio no puede ser nulo o vacío");
        }
        this.servicio = servicio.trim();
        this.numero = contador++;
        this.estado = "pendiente";
        this.mesaAsignada = "Sin asignar";
    }

    public String getCodigo() {
        if (codigoCache == null) {
            codigoCache = servicio.substring(0, 1).toUpperCase() + "-" + String.format("%04d", numero);
        }
        return codigoCache;
    }

    // Solo si deseas permitir restaurar el código tras deserialización (no obligatorio)
    public void setCodigoCache(String codigoCache) {
        this.codigoCache = codigoCache;
    }

    public String getServicio() {
        return servicio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        if (estado == null || estado.trim().isEmpty()) {
            throw new IllegalArgumentException("El estado no puede ser nulo o vacío");
        }
        this.estado = estado.trim();
    }

    public String getMesaAsignada() {
        return mesaAsignada;
    }

    public void setMesaAsignada(String mesaAsignada) {
        if (mesaAsignada == null || mesaAsignada.trim().isEmpty()) {
            this.mesaAsignada = "Sin asignar";
        } else {
            this.mesaAsignada = mesaAsignada.trim();
        }
    }

    public static void resetContador(int nuevoValor) {
        if (nuevoValor <= 0) {
            throw new IllegalArgumentException("El contador debe ser mayor que cero");
        }
        contador = nuevoValor;
    }

    @Override
    public String toString() {
        return String.format("Código: %s | Servicio: %s | Estado: %s | Mesa: %s",
                getCodigo(), servicio, estado, mesaAsignada);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ticket ticket = (Ticket) o;
        return numero == ticket.numero && servicio.equals(ticket.servicio);
    }

    @Override
    public int hashCode() {
        return 31 * numero + servicio.hashCode();
    }
}
