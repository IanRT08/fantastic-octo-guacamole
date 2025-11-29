package mx.edu.utez.fantasticoctoguacamole.modelo;

public class ProductoMasVendido extends Producto {
    private int totalVendido;

    public ProductoMasVendido() {
        super();
    }

    public int getTotalVendido() {
        return totalVendido;
    }

    public void setTotalVendido(int totalVendido) {
        this.totalVendido = totalVendido;
    }
}