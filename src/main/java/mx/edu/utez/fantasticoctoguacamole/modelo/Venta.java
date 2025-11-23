package mx.edu.utez.fantasticoctoguacamole.modelo;

import java.util.Date;
import java.util.List;

public class Venta {
    private int idVenta;
    private Date fecha;
    private double total;
    private int idUsuario;
    private List<DetalleVenta> detalles;

    public Venta(){}

    public Venta(int idUsuario, double total, Date fecha, int idVenta) {
        this.idUsuario = idUsuario;
        this.total = total;
        this.fecha = fecha;
        this.idVenta = idVenta;
    }

    //Id venta
    public int getIdVenta() {
        return idVenta;
    }
    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    //Fecha
    public Date getFecha() {
        return fecha;
    }
    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    //Total
    public double getTotal() {
        return total;
    }
    public void setTotal(double total) {
        this.total = total;
    }

    //Id usuario
    public int getIdUsuario() {
        return idUsuario;
    }
    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    //Detalle venta
    public List<DetalleVenta> getDetalles() {
        return detalles;
    }
    public void setDetalles(List<DetalleVenta> detalles) {
        this.detalles = detalles;
    }

}
