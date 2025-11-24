package mx.edu.utez.fantasticoctoguacamole.modelo.dao;

import mx.edu.utez.fantasticoctoguacamole.modelo.Cambio;
import mx.edu.utez.fantasticoctoguacamole.modelo.DetalleVenta;
import mx.edu.utez.fantasticoctoguacamole.modelo.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static mx.edu.utez.fantasticoctoguacamole.utils.OracleDatabaseConnectionManager.getConnection;

public class CambioDao {

    public int obtenerProximoIdCambio() {
        String query = "SELECT NVL(MAX(IdCambio), 0) + 1 AS NEXT_ID FROM Cambios";
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

    public boolean registrarCambio(Cambio cambio) {
        Connection conn = null;
        PreparedStatement pstmtCambio = null;
        PreparedStatement pstmtStockOrigen = null;
        PreparedStatement pstmtStockNuevo = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            //1. Insertar cambio
            String sqlCambio = "INSERT INTO Cambios (IdCambio, IdDetalleVentaOrigen, IdProductoNuevo, FechaCambio, " +
                    "CantidadCambiada, PrecioUnitarioOrigen, PrecioUnitarioNuevo, DiferenciaPago, Motivo) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            pstmtCambio = conn.prepareStatement(sqlCambio);
            pstmtCambio.setInt(1, obtenerProximoIdCambio());
            pstmtCambio.setInt(2, cambio.getIdDetalleVentaOrigen());
            pstmtCambio.setInt(3, cambio.getIdProductoNuevo());
            pstmtCambio.setDate(4, new java.sql.Date(cambio.getFechaCambio().getTime()));
            pstmtCambio.setInt(5, cambio.getCantidadCambiada());
            pstmtCambio.setDouble(6, cambio.getPrecioUnitarioOrigen());
            pstmtCambio.setDouble(7, cambio.getPrecioUnitarioNuevo());
            pstmtCambio.setDouble(8, cambio.getDiferenciaPago());
            pstmtCambio.setString(9, cambio.getMotivo());

            int filasAfectadas = pstmtCambio.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se pudo insertar el cambio");
            }
            //Actualizar stock - aumentar stock del producto devuelto
            String sqlStockOrigen = "UPDATE Productos SET Stock = Stock + ? WHERE IdProducto = " +
                    "(SELECT IdProducto FROM DetalleVentas WHERE IdDetalle = ?)";
            pstmtStockOrigen = conn.prepareStatement(sqlStockOrigen);
            pstmtStockOrigen.setInt(1, cambio.getCantidadCambiada());
            pstmtStockOrigen.setInt(2, cambio.getIdDetalleVentaOrigen());
            pstmtStockOrigen.executeUpdate();
            //Actualizar stock - disminuir stock del producto nuevo
            String sqlStockNuevo = "UPDATE Productos SET Stock = Stock - ? WHERE IdProducto = ? AND Estado = 1";
            pstmtStockNuevo = conn.prepareStatement(sqlStockNuevo);
            pstmtStockNuevo.setInt(1, cambio.getCantidadCambiada());
            pstmtStockNuevo.setInt(2, cambio.getIdProductoNuevo());
            pstmtStockNuevo.executeUpdate();
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
            System.err.println("Error al registrar cambio: " + e.getMessage());
            return false;
        } finally {
            try {
                if (pstmtCambio != null) pstmtCambio.close();
                if (pstmtStockOrigen != null) pstmtStockOrigen.close();
                if (pstmtStockNuevo != null) pstmtStockNuevo.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
    }

    public List<Cambio> obtenerTodosLosCambios() {
        String query = "SELECT c.*, dv.IdVenta FROM Cambios c " +
                "JOIN DetalleVentas dv ON c.IdDetalleVentaOrigen = dv.IdDetalle " +
                "ORDER BY c.FechaCambio DESC";
        List<Cambio> cambios = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Cambio cambio = new Cambio();
                cambio.setIdCambio(rs.getInt("IdCambio"));
                cambio.setIdDetalleVentaOrigen(rs.getInt("IdDetalleVentaOrigen"));
                cambio.setIdProductoNuevo(rs.getInt("IdProductoNuevo"));
                cambio.setFechaCambio(rs.getDate("FechaCambio"));
                cambio.setCantidadCambiada(rs.getInt("CantidadCambiada"));
                cambio.setPrecioUnitarioOrigen(rs.getDouble("PrecioUnitarioOrigen"));
                cambio.setPrecioUnitarioNuevo(rs.getDouble("PrecioUnitarioNuevo"));
                cambio.setDiferenciaPago(rs.getDouble("DiferenciaPago"));
                cambio.setMotivo(rs.getString("Motivo"));
                cambios.add(cambio);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener todos los cambios: " + e.getMessage());
        }
        return cambios;
    }

    public Cambio obtenerCambioPorId(int idCambio) {
        String query = "SELECT * FROM Cambios WHERE IdCambio = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idCambio);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Cambio cambio = new Cambio();
                cambio.setIdCambio(rs.getInt("IdCambio"));
                cambio.setIdDetalleVentaOrigen(rs.getInt("IdDetalleVentaOrigen"));
                cambio.setIdProductoNuevo(rs.getInt("IdProductoNuevo"));
                cambio.setFechaCambio(rs.getDate("FechaCambio"));
                cambio.setCantidadCambiada(rs.getInt("CantidadCambiada"));
                cambio.setPrecioUnitarioOrigen(rs.getDouble("PrecioUnitarioOrigen"));
                cambio.setPrecioUnitarioNuevo(rs.getDouble("PrecioUnitarioNuevo"));
                cambio.setDiferenciaPago(rs.getDouble("DiferenciaPago"));
                cambio.setMotivo(rs.getString("Motivo"));
                return cambio;
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener cambio por ID: " + e.getMessage());
        }
        return null;
    }

}