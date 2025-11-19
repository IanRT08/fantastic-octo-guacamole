package mx.edu.utez.fantasticoctoguacamole.modelo;

import mx.edu.utez.fantasticoctoguacamole.utils.OracleDatabaseConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class UsuarioDao {
    //Validar datos al iniciar sesion
    public Map<String, Object> validarUsuarioYObternerDatos(String correo, String contrasenia) {
        String query = "SELECT IdUsuario, Nombre, ApellidoPaterno, Rol FROM usuarios WHERE CorreoElectronico = ? AND Contrasenia = ? AND Estado = 1";
        Map<String, Object> usuarioData = new HashMap<>();
        try (Connection conn = OracleDatabaseConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, correo.toLowerCase().trim());
            ps.setString(2, contrasenia);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    usuarioData.put("valido", true);
                    usuarioData.put("idUsuario", rs.getInt("IdUsuario"));
                    usuarioData.put("nombre", rs.getString("Nombre"));
                    usuarioData.put("apellidoPaterno", rs.getString("ApellidoPaterno"));
                    usuarioData.put("rol", rs.getInt("Rol"));
                } else {
                    usuarioData.put("valido", false);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al validar usuario: " + e.getMessage());
            usuarioData.put("valido", false);
            usuarioData.put("error", e.getMessage());
        }
        return usuarioData;
    }

}
