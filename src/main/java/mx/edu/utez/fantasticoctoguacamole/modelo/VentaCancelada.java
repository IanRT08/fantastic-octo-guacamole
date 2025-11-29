package mx.edu.utez.fantasticoctoguacamole.modelo;

import java.util.Date;
import java.util.List;

public class VentaCancelada {
    private int idVentaCancelada;
    private int idVentaOriginal;
    private Date fechaVentaOriginal;
    private Date fechaCancelacion;
    private double totalVenta;
    private int idUsuarioVenta;
    private int idUsuarioCancelacion;
    private String motivoCancelacion;
    private List<DetalleVenta> detalles;
    private String nombreUsuarioVenta;
    private String nombreUsuarioCancelacion;

    public VentaCancelada() {}

    //Getters y Setters
    public int getIdVentaCancelada() { return idVentaCancelada; }
    public void setIdVentaCancelada(int idVentaCancelada) { this.idVentaCancelada = idVentaCancelada; }

    public int getIdVentaOriginal() { return idVentaOriginal; }
    public void setIdVentaOriginal(int idVentaOriginal) { this.idVentaOriginal = idVentaOriginal; }

    public Date getFechaVentaOriginal() { return fechaVentaOriginal; }
    public void setFechaVentaOriginal(Date fechaVentaOriginal) { this.fechaVentaOriginal = fechaVentaOriginal; }

    public Date getFechaCancelacion() { return fechaCancelacion; }
    public void setFechaCancelacion(Date fechaCancelacion) { this.fechaCancelacion = fechaCancelacion; }

    public double getTotalVenta() { return totalVenta; }
    public void setTotalVenta(double totalVenta) { this.totalVenta = totalVenta; }

    public int getIdUsuarioVenta() { return idUsuarioVenta; }
    public void setIdUsuarioVenta(int idUsuarioVenta) { this.idUsuarioVenta = idUsuarioVenta; }

    public int getIdUsuarioCancelacion() { return idUsuarioCancelacion; }
    public void setIdUsuarioCancelacion(int idUsuarioCancelacion) { this.idUsuarioCancelacion = idUsuarioCancelacion; }

    public String getMotivoCancelacion() { return motivoCancelacion; }
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }

    public List<DetalleVenta> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVenta> detalles) { this.detalles = detalles; }

    public String getNombreUsuarioVenta() { return nombreUsuarioVenta; }
    public void setNombreUsuarioVenta(String nombreUsuarioVenta) { this.nombreUsuarioVenta = nombreUsuarioVenta; }

    public String getNombreUsuarioCancelacion() { return nombreUsuarioCancelacion; }
    public void setNombreUsuarioCancelacion(String nombreUsuarioCancelacion) { this.nombreUsuarioCancelacion = nombreUsuarioCancelacion; }
}