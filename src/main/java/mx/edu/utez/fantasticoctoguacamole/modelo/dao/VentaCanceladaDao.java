package mx.edu.utez.fantasticoctoguacamole.modelo.dao;

import mx.edu.utez.fantasticoctoguacamole.modelo.Venta;
import mx.edu.utez.fantasticoctoguacamole.modelo.VentaCancelada;
import mx.edu.utez.fantasticoctoguacamole.modelo.DetalleVenta;
import mx.edu.utez.fantasticoctoguacamole.utils.OracleDatabaseConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VentaCanceladaDao {

    public boolean cancelarVenta(int idVenta, String motivo, int idUsuarioCancelacion) {
        Connection conn = null;
        try {
            conn = OracleDatabaseConnectionManager.getConnection();
            conn.setAutoCommit(false);
            VentaDao ventaDao = new VentaDao();
            Venta venta = ventaDao.obtenerVentaPorId(idVenta);
            if (venta == null) {
                throw new SQLException("Venta no encontrada");
            }
            List<DetalleVenta> detalles = ventaDao.obtenerDetallesVenta(idVenta);
            String sqlUpdateStock = "UPDATE Productos SET Stock = Stock + ? WHERE IdProducto = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdateStock)) {
                for (DetalleVenta detalle : detalles) {
                    pstmt.setInt(1, detalle.getCantidad());
                    pstmt.setInt(2, detalle.getIdProducto());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
            String sqlInsertVentaCancelada =
                    "INSERT INTO VentasCanceladas (IdVentaCancelada, IdVentaOriginal, FechaVentaOriginal, " +
                            "FechaCancelacion, TotalVenta, IdUsuarioVenta, IdUsuarioCancelacion, MotivoCancelacion) " +
                            "VALUES (seq_ventas_canceladas.NEXTVAL, ?, ?, ?, ?, ?, ?, ?)";
            int idVentaCancelada;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlInsertVentaCancelada, new String[]{"IdVentaCancelada"})) {
                pstmt.setInt(1, idVenta);
                pstmt.setDate(2, new java.sql.Date(venta.getFecha().getTime()));
                pstmt.setDate(3, new java.sql.Date(System.currentTimeMillis()));
                pstmt.setDouble(4, venta.getTotal());
                pstmt.setInt(5, venta.getIdUsuario());
                pstmt.setInt(6, idUsuarioCancelacion);
                pstmt.setString(7, motivo);
                pstmt.executeUpdate();
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    idVentaCancelada = rs.getInt(1);
                } else {
                    throw new SQLException("No se pudo obtener el ID de la venta cancelada");
                }
            }
            String sqlInsertDetalleCancelado =
                    "INSERT INTO DetallesVentasCanceladas (IdDetalleCancelado, IdVentaCancelada, IdProducto, " +
                            "Cantidad, PrecioUnitario, Subtotal) VALUES (seq_detalles_cancelados.NEXTVAL, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlInsertDetalleCancelado)) {
                for (DetalleVenta detalle : detalles) {
                    pstmt.setInt(1, idVentaCancelada);
                    pstmt.setInt(2, detalle.getIdProducto());
                    pstmt.setInt(3, detalle.getCantidad());
                    pstmt.setDouble(4, detalle.getPrecioUnitario());
                    pstmt.setDouble(5, detalle.getSubtotal());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<VentaCancelada> obtenerTodasLasVentasCanceladas() {
        List<VentaCancelada> ventasCanceladas = new ArrayList<>();
        String sql =
                "SELECT vc.*, u1.Nombre as NombreVendedor, u2.Nombre as NombreCancelador " +
                        "FROM VentasCanceladas vc " +
                        "LEFT JOIN Usuarios u1 ON vc.IdUsuarioVenta = u1.IdUsuario " +
                        "LEFT JOIN Usuarios u2 ON vc.IdUsuarioCancelacion = u2.IdUsuario " +
                        "ORDER BY vc.FechaCancelacion DESC";
        try (Connection conn = OracleDatabaseConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                VentaCancelada ventaCancelada = new VentaCancelada();
                ventaCancelada.setIdVentaCancelada(rs.getInt("IdVentaCancelada"));
                ventaCancelada.setIdVentaOriginal(rs.getInt("IdVentaOriginal"));
                ventaCancelada.setFechaVentaOriginal(rs.getDate("FechaVentaOriginal"));
                ventaCancelada.setFechaCancelacion(rs.getDate("FechaCancelacion"));
                ventaCancelada.setTotalVenta(rs.getDouble("TotalVenta"));
                ventaCancelada.setIdUsuarioVenta(rs.getInt("IdUsuarioVenta"));
                ventaCancelada.setIdUsuarioCancelacion(rs.getInt("IdUsuarioCancelacion"));
                ventaCancelada.setMotivoCancelacion(rs.getString("MotivoCancelacion"));
                ventaCancelada.setNombreUsuarioVenta(rs.getString("NombreVendedor"));
                ventaCancelada.setNombreUsuarioCancelacion(rs.getString("NombreCancelador"));
                ventaCancelada.setDetalles(obtenerDetallesVentaCancelada(ventaCancelada.getIdVentaCancelada()));
                ventasCanceladas.add(ventaCancelada);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ventasCanceladas;
    }

    private List<DetalleVenta> obtenerDetallesVentaCancelada(int idVentaCancelada) {
        List<DetalleVenta> detalles = new ArrayList<>();
        String sql = "SELECT * FROM DetallesVentasCanceladas WHERE IdVentaCancelada = ?";
        try (Connection conn = OracleDatabaseConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idVentaCancelada);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                DetalleVenta detalle = new DetalleVenta();
                detalle.setIdDetalle(rs.getInt("IdDetalleCancelado"));
                detalle.setIdVenta(rs.getInt("IdVentaCancelada"));
                detalle.setIdProducto(rs.getInt("IdProducto"));
                detalle.setCantidad(rs.getInt("Cantidad"));
                detalle.setPrecioUnitario(rs.getDouble("PrecioUnitario"));
                detalle.setSubtotal(rs.getDouble("Subtotal"));
                detalles.add(detalle);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return detalles;
    }

    public VentaCancelada obtenerVentaCanceladaPorId(int idVentaCancelada) {
        String sql =
                "SELECT vc.*, u1.Nombre as NombreVendedor, u2.Nombre as NombreCancelador " +
                        "FROM VentasCanceladas vc " +
                        "LEFT JOIN Usuarios u1 ON vc.IdUsuarioVenta = u1.IdUsuario " +
                        "LEFT JOIN Usuarios u2 ON vc.IdUsuarioCancelacion = u2.IdUsuario " +
                        "WHERE vc.IdVentaCancelada = ?";
        try (Connection conn = OracleDatabaseConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idVentaCancelada);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                VentaCancelada ventaCancelada = new VentaCancelada();
                ventaCancelada.setIdVentaCancelada(rs.getInt("IdVentaCancelada"));
                ventaCancelada.setIdVentaOriginal(rs.getInt("IdVentaOriginal"));
                ventaCancelada.setFechaVentaOriginal(rs.getDate("FechaVentaOriginal"));
                ventaCancelada.setFechaCancelacion(rs.getDate("FechaCancelacion"));
                ventaCancelada.setTotalVenta(rs.getDouble("TotalVenta"));
                ventaCancelada.setIdUsuarioVenta(rs.getInt("IdUsuarioVenta"));
                ventaCancelada.setIdUsuarioCancelacion(rs.getInt("IdUsuarioCancelacion"));
                ventaCancelada.setMotivoCancelacion(rs.getString("MotivoCancelacion"));
                ventaCancelada.setNombreUsuarioVenta(rs.getString("NombreVendedor"));
                ventaCancelada.setNombreUsuarioCancelacion(rs.getString("NombreCancelador"));

                ventaCancelada.setDetalles(obtenerDetallesVentaCancelada(idVentaCancelada));
                return ventaCancelada;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public VentaCancelada obtenerVentaCanceladaPorIdOriginal(int idVentaOriginal) {
        String sql = "SELECT * FROM VentasCanceladas WHERE IdVentaOriginal = ?";
        try (Connection conn = OracleDatabaseConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idVentaOriginal);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                VentaCancelada ventaCancelada = new VentaCancelada();
                ventaCancelada.setIdVentaCancelada(rs.getInt("IdVentaCancelada"));
                ventaCancelada.setIdVentaOriginal(rs.getInt("IdVentaOriginal"));
                ventaCancelada.setFechaVentaOriginal(rs.getDate("FechaVentaOriginal"));
                ventaCancelada.setFechaCancelacion(rs.getDate("FechaCancelacion"));
                ventaCancelada.setTotalVenta(rs.getDouble("TotalVenta"));
                ventaCancelada.setIdUsuarioVenta(rs.getInt("IdUsuarioVenta"));
                ventaCancelada.setIdUsuarioCancelacion(rs.getInt("IdUsuarioCancelacion"));
                ventaCancelada.setMotivoCancelacion(rs.getString("MotivoCancelacion"));
                return ventaCancelada;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}