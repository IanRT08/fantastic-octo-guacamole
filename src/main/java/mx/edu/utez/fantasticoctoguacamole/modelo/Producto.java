package mx.edu.utez.fantasticoctoguacamole.modelo;

public class Producto {
    private int IdProducto;
    private String codigo;
    private String nombre;
    private String descripcion;
    private double precio;
    private int stock;
    private Boolean estado;

    //Constructores
    public Producto() {}

    public Producto(int idProducto, String codigo, String nombreProducto, String descripcion, double precio, int stock) {
        this.IdProducto = idProducto;
        this.codigo = codigo;
        this.nombre = nombreProducto;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.estado = true; // Por defecto activo
    }

    //ID
    public int getIdProducto() {
        return IdProducto;
    }
    public void setIdProducto(int idProducto) {
        IdProducto = idProducto;
    }
    //Codigo
    public String getCodigo() {
        return codigo;
    }
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
    //Nombre
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    //Descripcion
    public String getDescripcion() {
        return descripcion;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    //Precio
    public double getPrecio() {
        return precio;
    }
    public void setPrecio(double precio) {
        this.precio = precio;
    }
    //Stock
    public int getStock() {
        return stock;
    }
    public void setStock(int stock) {
        this.stock = stock;
    }
    //Estado
    public Boolean getEstado() {
        return estado;
    }
    public void setEstado(Boolean estado) {
        this.estado = estado;
    }
}
