package modelo;

import java.io.Serializable;

public class Ticket implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int contador = 1;
    private int numero;
    private String servicio;
    private String estado;

    public Ticket(String servicio) {
        this.servicio = servicio;
        this.numero = contador++;
        this.estado = "pendiente";
    }

    public String getCodigo() {
        return servicio.substring(0, 1).toUpperCase() + "-" + numero;
    }

    public String getServicio() {
        return servicio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return getCodigo() + " - " + servicio + " [" + estado + "]";
    }
}
