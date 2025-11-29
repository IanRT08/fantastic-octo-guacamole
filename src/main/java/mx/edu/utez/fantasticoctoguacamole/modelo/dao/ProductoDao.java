package mx.edu.utez.fantasticoctoguacamole.modelo.dao;

import mx.edu.utez.fantasticoctoguacamole.modelo.Producto;
import mx.edu.utez.fantasticoctoguacamole.modelo.ProductoMasVendido;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static mx.edu.utez.fantasticoctoguacamole.utils.OracleDatabaseConnectionManager.getConnection;

public class ProductoDao {

    //Metodo para obtener el proximo ID
    public int obtenerProximoId(){
        String query = "SELECT NVL(MAX(IdProducto), 0) + 1 AS NEXT_ID FROM PRODUCTOS";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()){
                return rs.getInt("NEXT_ID");
            }
        } catch(SQLException e){
            e.printStackTrace();
        }
        return 1; //Si hay error retorna 1 como valor por defecto
    }

    //Funcion de crear (C) del CRUD
    public boolean createProducto(Producto p) {
        String query = "INSERT INTO PRODUCTOS (IdProducto, Codigo, NombreProducto, Descripción, Precio, Stock, Estado) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, p.getIdProducto());
            ps.setString(2, p.getCodigo());
            ps.setString(3, p.getNombre());
            ps.setString(4, p.getDescripcion());
            ps.setDouble(5, p.getPrecio());
            ps.setInt(6, p.getStock());
            ps.setInt(7, p.getEstado() ? 1 : 0);
            int resultado = ps.executeUpdate();
            return resultado > 0;
        } catch (SQLException e) {
            System.err.println("Error al crear producto: " + e.getMessage());
            // Verificar si es error de código duplicado
            if (e.getMessage().contains("unique constraint") || e.getErrorCode() == 1) {
                System.err.println("Código de producto ya está en uso");
            }
            return false;
        }
    }

    //Funcion de lectura (R) del CRUD
    public List<Producto> readProductos(){
        String query = "SELECT IdProducto, Codigo, NombreProducto, Descripción, Precio, Stock, Estado FROM Productos ORDER BY NombreProducto ASC";
        List<Producto> lista = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while(rs.next()){
                Producto p = new Producto();
                p.setIdProducto(rs.getInt("IdProducto"));
                p.setCodigo(rs.getString("Codigo"));
                p.setNombre(rs.getString("NombreProducto"));
                p.setDescripcion(rs.getString("Descripción"));
                p.setPrecio(rs.getDouble("Precio"));
                p.setStock(rs.getInt("Stock"));
                int estadoNum = rs.getInt("Estado");
                p.setEstado(estadoNum == 1);
                lista.add(p);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    //Funcion de actualizar (U) del CRUD
    public boolean updateProducto(Producto p) {
        String query = "UPDATE PRODUCTOS SET Codigo=?, NombreProducto=?, Descripción=?, Precio=?, Stock=?, Estado=? WHERE IdProducto=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getDescripcion());
            ps.setDouble(4, p.getPrecio());
            ps.setInt(5, p.getStock());
            ps.setInt(6, p.getEstado() ? 1 : 0);
            ps.setInt(7, p.getIdProducto());
            int resultado = ps.executeUpdate();
            return resultado > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
            // Verificar si es error de código duplicado
            if (e.getMessage().contains("unique constraint") || e.getErrorCode() == 1) {
                System.err.println("Código de producto ya está en uso");
            }
            return false;
        }
    }

    //Funcion de eliminar (D) del CRUD
    public boolean deleteProducto(int idProducto) {
        String query = "DELETE FROM PRODUCTOS WHERE IdProducto = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, idProducto);
            int resultado = ps.executeUpdate();
            return resultado > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
            return false;
        }
    }

    //Metodo para obtener un producto completo por su ID - ACTUALIZADO con Estado
    public Producto obtenerProductoPorId(int idProducto) {
        String query = "SELECT IdProducto, Codigo, NombreProducto, Descripción, Precio, Stock, Estado FROM PRODUCTOS WHERE IdProducto = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Producto producto = new Producto();
                    producto.setIdProducto(rs.getInt("IdProducto"));
                    producto.setCodigo(rs.getString("Codigo"));
                    producto.setNombre(rs.getString("NombreProducto"));
                    producto.setDescripcion(rs.getString("Descripción"));
                    producto.setPrecio(rs.getDouble("Precio"));
                    producto.setStock(rs.getInt("Stock"));
                    producto.setEstado(rs.getInt("Estado") == 1);
                    return producto;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener producto por ID: " + e.getMessage());
        }
        return null;
    }

    //Metodo para cambiar el estado del producto
    public boolean cambiarEstadoProducto(int idProducto, boolean nuevoEstado) {
        String query = "UPDATE PRODUCTOS SET Estado = ? WHERE IdProducto = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, nuevoEstado ? 1 : 0);
            ps.setInt(2, idProducto);
            int resultado = ps.executeUpdate();
            return resultado > 0;
        } catch (SQLException e) {
            System.err.println("Error al cambiar estado del producto: " + e.getMessage());
            return false;
        }
    }

    //Metodo adicional: verificar si un código ya existe
    public boolean existeCodigo(String codigo) {
        String query = "SELECT COUNT(*) FROM PRODUCTOS WHERE Codigo = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar código: " + e.getMessage());
        }
        return false;
    }

    public List<ProductoMasVendido> obtenerProductosMasVendidos(int limite) {
        List<ProductoMasVendido> productos = new ArrayList<>();
        String query = "SELECT p.IdProducto, p.Codigo, p.NombreProducto, p.Precio, p.Stock, " +
                "SUM(dv.Cantidad) as TotalVendido " +
                "FROM Productos p " +
                "INNER JOIN DetalleVentas dv ON p.IdProducto = dv.IdProducto " +
                "GROUP BY p.IdProducto, p.Codigo, p.NombreProducto, p.Precio, p.Stock " +
                "ORDER BY TotalVendido DESC " +
                "FETCH FIRST ? ROWS ONLY";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, limite);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ProductoMasVendido producto = new ProductoMasVendido();
                producto.setIdProducto(rs.getInt("IdProducto"));
                producto.setCodigo(rs.getString("Codigo"));
                producto.setNombre(rs.getString("NombreProducto"));
                producto.setPrecio(rs.getDouble("Precio"));
                producto.setStock(rs.getInt("Stock"));
                producto.setTotalVendido(rs.getInt("TotalVendido"));
                productos.add(producto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productos;
    }

}