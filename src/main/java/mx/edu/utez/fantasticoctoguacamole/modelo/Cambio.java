package mx.edu.utez.fantasticoctoguacamole.modelo;

import java.util.Date;

public class Cambio {
    private int idCambio;
    private int idDetalleVentaOrigen;
    private int idProductoNuevo;
    private Date fechaCambio;
    private int cantidadCambiada;
    private double precioUnitarioOrigen;
    private double precioUnitarioNuevo;
    private double diferenciaPago;
    private String motivo;
    private DetalleVenta detalleVentaOrigen;
    private Producto productoNuevo;

    public Cambio() {}

    public Cambio(int idCambio, int idDetalleVentaOrigen, int idProductoNuevo, Date fechaCambio,
                  int cantidadCambiada, double precioUnitarioOrigen, double precioUnitarioNuevo,
                  double diferenciaPago, String motivo) {
        this.idCambio = idCambio;
        this.idDetalleVentaOrigen = idDetalleVentaOrigen;
        this.idProductoNuevo = idProductoNuevo;
        this.fechaCambio = fechaCambio;
        this.cantidadCambiada = cantidadCambiada;
        this.precioUnitarioOrigen = precioUnitarioOrigen;
        this.precioUnitarioNuevo = precioUnitarioNuevo;
        this.diferenciaPago = diferenciaPago;
        this.motivo = motivo;
    }

    // Getters y Setters
    public int getIdCambio() { return idCambio; }
    public void setIdCambio(int idCambio) { this.idCambio = idCambio; }

    public int getIdDetalleVentaOrigen() { return idDetalleVentaOrigen; }
    public void setIdDetalleVentaOrigen(int idDetalleVentaOrigen) { this.idDetalleVentaOrigen = idDetalleVentaOrigen; }

    public int getIdProductoNuevo() { return idProductoNuevo; }
    public void setIdProductoNuevo(int idProductoNuevo) { this.idProductoNuevo = idProductoNuevo; }

    public Date getFechaCambio() { return fechaCambio; }
    public void setFechaCambio(Date fechaCambio) { this.fechaCambio = fechaCambio; }

    public int getCantidadCambiada() { return cantidadCambiada; }
    public void setCantidadCambiada(int cantidadCambiada) { this.cantidadCambiada = cantidadCambiada; }

    public double getPrecioUnitarioOrigen() { return precioUnitarioOrigen; }
    public void setPrecioUnitarioOrigen(double precioUnitarioOrigen) { this.precioUnitarioOrigen = precioUnitarioOrigen; }

    public double getPrecioUnitarioNuevo() { return precioUnitarioNuevo; }
    public void setPrecioUnitarioNuevo(double precioUnitarioNuevo) { this.precioUnitarioNuevo = precioUnitarioNuevo; }

    public double getDiferenciaPago() { return diferenciaPago; }
    public void setDiferenciaPago(double diferenciaPago) { this.diferenciaPago = diferenciaPago; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public DetalleVenta getDetalleVentaOrigen() { return detalleVentaOrigen; }
    public void setDetalleVentaOrigen(DetalleVenta detalleVentaOrigen) { this.detalleVentaOrigen = detalleVentaOrigen; }

    public Producto getProductoNuevo() { return productoNuevo; }
    public void setProductoNuevo(Producto productoNuevo) { this.productoNuevo = productoNuevo; }
}
