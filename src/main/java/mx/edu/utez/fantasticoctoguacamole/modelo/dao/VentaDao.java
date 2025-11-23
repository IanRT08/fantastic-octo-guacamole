package mx.edu.utez.fantasticoctoguacamole.modelo.dao;

import mx.edu.utez.fantasticoctoguacamole.modelo.Producto;
import mx.edu.utez.fantasticoctoguacamole.modelo.Venta;
import mx.edu.utez.fantasticoctoguacamole.modelo.DetalleVenta;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static mx.edu.utez.fantasticoctoguacamole.utils.OracleDatabaseConnectionManager.getConnection;

public class VentaDao {

    //Metodo para obtener el proximo ID de venta
    public int obtenerProximoIdVenta() {
        String query = "SELECT NVL(MAX(IdVenta), 0) + 1 AS NEXT_ID FROM Ventas";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("NEXT_ID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    //Metodo para obtener el próximo ID de detalle
    public int obtenerProximoIdDetalle() {
        String query = "SELECT NVL(MAX(IdDetalle), 0) + 1 AS NEXT_ID FROM DetalleVentas";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("NEXT_ID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    //Registrar venta completa con transaccion
    public boolean registrarVenta(Venta venta) {
        Connection conn = null;
        PreparedStatement pstmtVenta = null;
        PreparedStatement pstmtDetalle = null;
        PreparedStatement pstmtActualizarStock = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); //Iniciar transacción
            //Insertar venta
            int idVenta = obtenerProximoIdVenta();
            String sqlVenta = "INSERT INTO Ventas (IdVenta, Fecha, Total, IdUsuario) VALUES (?, ?, ?, ?)";
            pstmtVenta = conn.prepareStatement(sqlVenta);
            pstmtVenta.setInt(1, idVenta);
            pstmtVenta.setDate(2, new java.sql.Date(venta.getFecha().getTime()));
            pstmtVenta.setDouble(3, venta.getTotal());
            pstmtVenta.setInt(4, venta.getIdUsuario());
            int filasAfectadas = pstmtVenta.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se pudo insertar la venta");
            }
            //Insertar detalles y actualizar stock
            String sqlDetalle = "INSERT INTO DetalleVentas (IdDetalle, IdVenta, IdProducto, Cantidad, PrecioUnitario, Subtotal) VALUES (?, ?, ?, ?, ?, ?)";
            String sqlActualizarStock = "UPDATE Productos SET Stock = Stock - ? WHERE IdProducto = ? AND Estado = 1";
            pstmtDetalle = conn.prepareStatement(sqlDetalle);
            pstmtActualizarStock = conn.prepareStatement(sqlActualizarStock);
            int idDetalle = obtenerProximoIdDetalle();
            for (DetalleVenta detalle : venta.getDetalles()) {
                //Insertar detalle
                pstmtDetalle.setInt(1, idDetalle);
                pstmtDetalle.setInt(2, idVenta);
                pstmtDetalle.setInt(3, detalle.getIdProducto());
                pstmtDetalle.setInt(4, detalle.getCantidad());
                pstmtDetalle.setDouble(5, detalle.getPrecioUnitario());
                pstmtDetalle.setDouble(6, detalle.getSubtotal());
                pstmtDetalle.addBatch();
                //Actualizar stock
                pstmtActualizarStock.setInt(1, detalle.getCantidad());
                pstmtActualizarStock.setInt(2, detalle.getIdProducto());
                pstmtActualizarStock.addBatch();
                idDetalle++;
            }
            pstmtDetalle.executeBatch();
            pstmtActualizarStock.executeBatch();
            conn.commit(); //Confirmar transaccion
            return true;
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback(); //Revertir en caso de error
                }
            } catch (SQLException ex) {
                System.err.println("Error al hacer rollback: " + ex.getMessage());
            }
            System.err.println("Error al registrar venta: " + e.getMessage());
            return false;
        } finally {
            //Cerrar recursos
            try {
                if (pstmtVenta != null) pstmtVenta.close();
                if (pstmtDetalle != null) pstmtDetalle.close();
                if (pstmtActualizarStock != null) pstmtActualizarStock.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
    }

    //Obtener todas las ventas
    public List<Venta> obtenerVentas() {
        String query = "SELECT IdVenta, Fecha, Total, IdUsuario FROM Ventas ORDER BY Fecha DESC";
        List<Venta> ventas = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Venta venta = new Venta();
                venta.setIdVenta(rs.getInt("IdVenta"));
                venta.setFecha(rs.getDate("Fecha"));
                venta.setTotal(rs.getFloat("Total"));
                venta.setIdUsuario(rs.getInt("IdUsuario"));
                ventas.add(venta);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener ventas: " + e.getMessage());
        }
        return ventas;
    }

    //Obtener detalles de una venta especifica
    public List<DetalleVenta> obtenerDetallesVenta(int idVenta) {
        String query = "SELECT dv.IdDetalle, dv.IdVenta, dv.IdProducto, dv.Cantidad, dv.PrecioUnitario, dv.Subtotal, " +
                "p.NombreProducto, p.Codigo " +
                "FROM DetalleVentas dv " +
                "LEFT JOIN Productos p ON dv.IdProducto = p.IdProducto " +
                "WHERE dv.IdVenta = ? " +
                "ORDER BY dv.IdDetalle";
        List<DetalleVenta> detalles = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idVenta);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                DetalleVenta detalle = new DetalleVenta();
                detalle.setIdDetalle(rs.getInt("IdDetalle"));
                detalle.setIdVenta(rs.getInt("IdVenta"));
                detalle.setIdProducto(rs.getInt("IdProducto"));
                detalle.setCantidad(rs.getInt("Cantidad"));
                detalle.setPrecioUnitario(rs.getFloat("PrecioUnitario"));
                detalle.setSubtotal(rs.getFloat("Subtotal"));
                // Crear producto básico para mostrar información
                Producto producto = new Producto();
                producto.setNombre(rs.getString("NombreProducto"));
                producto.setCodigo(rs.getString("Codigo"));
                detalle.setProducto(producto);
                detalles.add(detalle);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener detalles de venta: " + e.getMessage());
        }
        return detalles;
    }

    //Obtener venta por ID
    public List<Venta> obtenerVentasPorUsuario(int idUsuario) {
        String query = "SELECT IdVenta, Fecha, Total, IdUsuario FROM Ventas WHERE IdUsuario = ? ORDER BY Fecha DESC";
        List<Venta> ventas = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idUsuario);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Venta venta = new Venta();
                venta.setIdVenta(rs.getInt("IdVenta"));
                venta.setFecha(rs.getDate("Fecha"));
                venta.setTotal(rs.getDouble("Total"));
                venta.setIdUsuario(rs.getInt("IdUsuario"));
                ventas.add(venta);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener ventas por usuario: " + e.getMessage());
        }
        return ventas;
    }

    //Obtener ventas por rango de fechas
    public List<Venta> obtenerVentasPorFecha(Date fechaInicio, Date fechaFin) {
        String query = "SELECT IdVenta, Fecha, Total, IdUsuario FROM Ventas " +
                "WHERE Fecha BETWEEN ? AND ? " +
                "ORDER BY Fecha DESC";
        List<Venta> ventas = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDate(1, new java.sql.Date(fechaInicio.getTime()));
            pstmt.setDate(2, new java.sql.Date(fechaFin.getTime()));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Venta venta = new Venta();
                venta.setIdVenta(rs.getInt("IdVenta"));
                venta.setFecha(rs.getDate("Fecha"));
                venta.setTotal(rs.getFloat("Total"));
                venta.setIdUsuario(rs.getInt("IdUsuario"));
                ventas.add(venta);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener ventas por fecha: " + e.getMessage());
        }
        return ventas;
    }

    //Obtener total de ventas por dia
    public double obtenerTotalVentasDelDia(Date fecha) {
        String query = "SELECT NVL(SUM(Total), 0) AS TotalDia FROM Ventas WHERE TRUNC(Fecha) = TRUNC(?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDate(1, new java.sql.Date(fecha.getTime()));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("TotalDia");
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener total de ventas del día: " + e.getMessage());
        }
        return 0.0;
    }

    //Obtener productos más vendidos
    public List<Object[]> obtenerProductosMasVendidos(int limite) {
        String query = "SELECT p.NombreProducto, p.Codigo, SUM(dv.Cantidad) as TotalVendido " +
                "FROM DetalleVentas dv " +
                "JOIN Productos p ON dv.IdProducto = p.IdProducto " +
                "GROUP BY p.NombreProducto, p.Codigo " +
                "ORDER BY TotalVendido DESC " +
                "FETCH FIRST ? ROWS ONLY";
        List<Object[]> resultados = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, limite);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Object[] fila = new Object[3];
                fila[0] = rs.getString("NombreProducto");
                fila[1] = rs.getString("Codigo");
                fila[2] = rs.getInt("TotalVendido");
                resultados.add(fila);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener productos más vendidos: " + e.getMessage());
        }
        return resultados;
    }

    //Verificar si existe una venta
    public boolean existeVenta(int idVenta) {
        String query = "SELECT COUNT(*) FROM Ventas WHERE IdVenta = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idVenta);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar venta: " + e.getMessage());
        }
        return false;
    }







}
