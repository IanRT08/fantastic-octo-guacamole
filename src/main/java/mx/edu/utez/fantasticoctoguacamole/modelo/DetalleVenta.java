package mx.edu.utez.fantasticoctoguacamole.modelo;

public class DetalleVenta {
    private int idDetalle;
    private int idVenta;
    private int idProducto;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;
    private Producto producto;

    public DetalleVenta() {}

    public DetalleVenta(int idProducto, int cantidad, double precioUnitario, double subtotal) {
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public DetalleVenta(int idDetalle, int idVenta, int idProducto, int cantidad, double precioUnitario, double subtotal, Producto producto) {
        this.idDetalle = idDetalle;
        this.idVenta = idVenta;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
        this.producto = producto;
    }

    //Getters y setters
    //Id detalle
    public int getIdDetalle() {
        return idDetalle;
    }
    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    //Id venta
    public int getIdVenta() {
        return idVenta;
    }
    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    //Id producto
    public int getIdProducto() {
        return idProducto;
    }
    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    //Cantidad
    public int getCantidad() {
        return cantidad;
    }
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    //Precio unitario
    public double getPrecioUnitario() {
        return precioUnitario;
    }
    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    //Subtotal
    public double getSubtotal() {
        return subtotal;
    }
    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    //Producto
    public Producto getProducto() {
        return producto;
    }
    public void setProducto(Producto producto) {
        this.producto = producto;
    }
}
