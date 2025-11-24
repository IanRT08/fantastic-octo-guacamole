package mx.edu.utez.fantasticoctoguacamole.modelo.dao;

import mx.edu.utez.fantasticoctoguacamole.modelo.Devolucion;
import mx.edu.utez.fantasticoctoguacamole.modelo.DetalleVenta;
import mx.edu.utez.fantasticoctoguacamole.modelo.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static mx.edu.utez.fantasticoctoguacamole.utils.OracleDatabaseConnectionManager.getConnection;

public class DevolucionDao {

    //Metodo para obtener el próximo ID de devolucion
    public int obtenerProximoIdDevolucion() {
        String query = "SELECT NVL(MAX(IdDevolucion), 0) + 1 AS NEXT_ID FROM Devoluciones";
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

    //Registrar una devolucion
    public boolean registrarDevolucion(Devolucion devolucion) {
        Connection conn = null;
        PreparedStatement pstmtDevolucion = null;
        PreparedStatement pstmtActualizarStock = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            //Insertar devolucion
            String sqlDevolucion = "INSERT INTO Devoluciones (IdDevolucion, IdDetalleVenta, FechaDevolucion, CantidadDevuelta, MontoReembolsado, Motivo) VALUES (?, ?, ?, ?, ?, ?)";
            pstmtDevolucion = conn.prepareStatement(sqlDevolucion);
            pstmtDevolucion.setInt(1, obtenerProximoIdDevolucion());
            pstmtDevolucion.setInt(2, devolucion.getIdDetalleVenta());
            pstmtDevolucion.setDate(3, new java.sql.Date(devolucion.getFechaDevolucion().getTime()));
            pstmtDevolucion.setInt(4, devolucion.getCantidadDevuelta());
            pstmtDevolucion.setDouble(5, devolucion.getMontoReembolsado());
            pstmtDevolucion.setString(6, devolucion.getMotivo());
            int filasAfectadas = pstmtDevolucion.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se pudo insertar la devolución");
            }
            //Actualizar stock del producto
            String sqlActualizarStock = "UPDATE Productos SET Stock = Stock + ? WHERE IdProducto = (SELECT IdProducto FROM DetalleVentas WHERE IdDetalle = ?)";
            pstmtActualizarStock = conn.prepareStatement(sqlActualizarStock);
            pstmtActualizarStock.setInt(1, devolucion.getCantidadDevuelta());
            pstmtActualizarStock.setInt(2, devolucion.getIdDetalleVenta());
            pstmtActualizarStock.executeUpdate();
            conn.commit();
            return true;
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                System.err.println("Error al hacer rollback: " + ex.getMessage());
            }
            System.err.println("Error al registrar devolución: " + e.getMessage());
            return false;
        } finally {
            try {
                if (pstmtDevolucion != null) pstmtDevolucion.close();
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

    //Obtener devoluciones por venta
    public List<Devolucion> obtenerDevolucionesPorVenta(int idVenta) {
        String query = "SELECT d.*, dv.IdProducto, dv.Cantidad, dv.PrecioUnitario, p.NombreProducto " +
                "FROM Devoluciones d " +
                "JOIN DetalleVentas dv ON d.IdDetalleVenta = dv.IdDetalle " +
                "JOIN Productos p ON dv.IdProducto = p.IdProducto " +
                "WHERE dv.IdVenta = ? " +
                "ORDER BY d.FechaDevolucion DESC";
        List<Devolucion> devoluciones = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idVenta);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Devolucion devolucion = new Devolucion();
                devolucion.setIdDevolucion(rs.getInt("IdDevolucion"));
                devolucion.setIdDetalleVenta(rs.getInt("IdDetalleVenta"));
                devolucion.setFechaDevolucion(rs.getDate("FechaDevolucion"));
                devolucion.setCantidadDevuelta(rs.getInt("CantidadDevuelta"));
                devolucion.setMontoReembolsado(rs.getDouble("MontoReembolsado"));
                devolucion.setMotivo(rs.getString("Motivo"));
                //Crear detalle de venta asociado
                DetalleVenta detalle = new DetalleVenta();
                detalle.setIdProducto(rs.getInt("IdProducto"));
                detalle.setCantidad(rs.getInt("Cantidad"));
                detalle.setPrecioUnitario(rs.getDouble("PrecioUnitario"));
                Producto producto = new Producto();
                producto.setNombre(rs.getString("NombreProducto"));
                detalle.setProducto(producto);
                devolucion.setDetalleVenta(detalle);
                devoluciones.add(devolucion);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener devoluciones por venta: " + e.getMessage());
        }
        return devoluciones;
    }

    //Verificar si una venta es elegible para devolucion
    public boolean esVentaElegibleParaDevolucion(Date fechaVenta) {
        long tiempoTranscurrido = System.currentTimeMillis() - fechaVenta.getTime();
        long diasTranscurridos = tiempoTranscurrido / (1000 * 60 * 60 * 24);
        return diasTranscurridos <= 30;
    }

    //Obtener cantidad maxima que se puede devolver de un detalle
    public int obtenerCantidadMaximaDevolucion(int idDetalleVenta) {
        String query = "SELECT dv.Cantidad - NVL(SUM(d.CantidadDevuelta), 0) as CantidadDisponible " +
                "FROM DetalleVentas dv " +
                "LEFT JOIN Devoluciones d ON dv.IdDetalle = d.IdDetalleVenta " +
                "WHERE dv.IdDetalle = ? " +
                "GROUP BY dv.Cantidad";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idDetalleVenta);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("CantidadDisponible");
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener cantidad máxima de devolución: " + e.getMessage());
        }
        return 0;
    }

    public List<Devolucion> obtenerTodasLasDevoluciones() {
        String query = "SELECT d.*, dv.IdVenta FROM Devoluciones d " +
                "JOIN DetalleVentas dv ON d.IdDetalleVenta = dv.IdDetalle " +
                "ORDER BY d.FechaDevolucion DESC";
        List<Devolucion> devoluciones = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Devolucion devolucion = new Devolucion();
                devolucion.setIdDevolucion(rs.getInt("IdDevolucion"));
                devolucion.setIdDetalleVenta(rs.getInt("IdDetalleVenta"));
                devolucion.setFechaDevolucion(rs.getDate("FechaDevolucion"));
                devolucion.setCantidadDevuelta(rs.getInt("CantidadDevuelta"));
                devolucion.setMontoReembolsado(rs.getDouble("MontoReembolsado"));
                devolucion.setMotivo(rs.getString("Motivo"));
                devoluciones.add(devolucion);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener todas las devoluciones: " + e.getMessage());
        }
        return devoluciones;
    }

    public Devolucion obtenerDevolucionPorId(int idDevolucion) {
        String query = "SELECT * FROM Devoluciones WHERE IdDevolucion = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idDevolucion);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Devolucion devolucion = new Devolucion();
                devolucion.setIdDevolucion(rs.getInt("IdDevolucion"));
                devolucion.setIdDetalleVenta(rs.getInt("IdDetalleVenta"));
                devolucion.setFechaDevolucion(rs.getDate("FechaDevolucion"));
                devolucion.setCantidadDevuelta(rs.getInt("CantidadDevuelta"));
                devolucion.setMontoReembolsado(rs.getDouble("MontoReembolsado"));
                devolucion.setMotivo(rs.getString("Motivo"));
                return devolucion;
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener devolución por ID: " + e.getMessage());
        }
        return null;
    }
}