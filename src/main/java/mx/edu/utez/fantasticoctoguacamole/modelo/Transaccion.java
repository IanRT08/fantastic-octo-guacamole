package mx.edu.utez.fantasticoctoguacamole.modelo;

import java.util.Date;

public class Transaccion {
    private String tipo; //Venta, Cambio, Devolucion
    private Date fecha;
    private int idTransaccion;
    private double monto;
    private int idUsuario;
    private String nombreCajero;
    private Object objetoTransaccion; //Puede ser Venta, Cambio o Devolucion

    public Transaccion() {}

    //Getters y Setters
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public int getIdTransaccion() { return idTransaccion; }
    public void setIdTransaccion(int idTransaccion) { this.idTransaccion = idTransaccion; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getNombreCajero() { return nombreCajero; }
    public void setNombreCajero(String nombreCajero) { this.nombreCajero = nombreCajero; }

    public Object getObjetoTransaccion() { return objetoTransaccion; }
    public void setObjetoTransaccion(Object objetoTransaccion) { this.objetoTransaccion = objetoTransaccion; }
}
