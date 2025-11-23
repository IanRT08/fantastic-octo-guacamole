package mx.edu.utez.fantasticoctoguacamole.modelo;

import java.util.Date;

public class Devolucion {
    private int idDevolucion;
    private int idDetalleVenta;
    private Date fechaDevolucion;
    private int cantidadDevuelta;
    private double montoReembolsado;
    private String motivo;
    private DetalleVenta detalleVenta; // Para mostrar información del detalle

    public Devolucion() {}

    public Devolucion(int idDevolucion, int idDetalleVenta, Date fechaDevolucion, int cantidadDevuelta, double montoReembolsado, String motivo) {
        this.idDevolucion = idDevolucion;
        this.idDetalleVenta = idDetalleVenta;
        this.fechaDevolucion = fechaDevolucion;
        this.cantidadDevuelta = cantidadDevuelta;
        this.montoReembolsado = montoReembolsado;
        this.motivo = motivo;
    }

    //Getters y Setters
    public int getIdDevolucion() { return idDevolucion; }
    public void setIdDevolucion(int idDevolucion) { this.idDevolucion = idDevolucion; }

    public int getIdDetalleVenta() { return idDetalleVenta; }
    public void setIdDetalleVenta(int idDetalleVenta) { this.idDetalleVenta = idDetalleVenta; }

    public Date getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(Date fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }

    public int getCantidadDevuelta() { return cantidadDevuelta; }
    public void setCantidadDevuelta(int cantidadDevuelta) { this.cantidadDevuelta = cantidadDevuelta; }

    public double getMontoReembolsado() { return montoReembolsado; }
    public void setMontoReembolsado(double montoReembolsado) { this.montoReembolsado = montoReembolsado; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public DetalleVenta getDetalleVenta() { return detalleVenta; }
    public void setDetalleVenta(DetalleVenta detalleVenta) { this.detalleVenta = detalleVenta; }
}