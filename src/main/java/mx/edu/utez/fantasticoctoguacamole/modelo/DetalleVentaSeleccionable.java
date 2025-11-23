package mx.edu.utez.fantasticoctoguacamole.modelo;

public class DetalleVentaSeleccionable {
    private DetalleVenta detalleVenta;
    private boolean seleccionado;
    private int cantidadDevolver;

    public DetalleVentaSeleccionable(DetalleVenta detalleVenta) {
        this.detalleVenta = detalleVenta;
        this.seleccionado = false;
        this.cantidadDevolver = 0;
    }

    //Getters y Setters
    public DetalleVenta getDetalleVenta() { return detalleVenta; }
    public void setDetalleVenta(DetalleVenta detalleVenta) { this.detalleVenta = detalleVenta; }

    public boolean isSeleccionado() { return seleccionado; }
    public void setSeleccionado(boolean seleccionado) { this.seleccionado = seleccionado; }

    public int getCantidadDevolver() { return cantidadDevolver; }
    public void setCantidadDevolver(int cantidadDevolver) { this.cantidadDevolver = cantidadDevolver; }

    //Metodos delegados para acceso facil a los datos del detalle
    public int getIdDetalle() { return detalleVenta.getIdDetalle(); }
    public int getIdProducto() { return detalleVenta.getIdProducto(); }
    public int getCantidad() { return detalleVenta.getCantidad(); }
    public double getPrecioUnitario() { return detalleVenta.getPrecioUnitario(); }
    public double getSubtotal() { return detalleVenta.getSubtotal(); }
    public Producto getProducto() { return detalleVenta.getProducto(); }
}