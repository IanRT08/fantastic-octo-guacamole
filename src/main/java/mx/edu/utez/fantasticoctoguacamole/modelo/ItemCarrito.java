package mx.edu.utez.fantasticoctoguacamole.modelo;

public class ItemCarrito {
    private Producto producto;
    private int cantidad;

    //Constructor
    public ItemCarrito(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
    }

    //Producto
    public Producto getProducto() {
        return producto;
    }
    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    //Cantidad
    public int getCantidad() {
        return cantidad;
    }
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getSubtotal() {
        return producto.getPrecio() * cantidad;
    }
}