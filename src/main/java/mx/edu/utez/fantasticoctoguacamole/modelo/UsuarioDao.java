package mx.edu.utez.fantasticoctoguacamole.modelo;

import mx.edu.utez.fantasticoctoguacamole.modelo.dao.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static mx.edu.utez.fantasticoctoguacamole.utils.OracleDatabaseConnectionManager.getConnection;


public class UsuarioDao {
    //Validar datos al iniciar sesion
    public Map<String, Object> validarUsuarioYObternerDatos(String correo, String contrasenia) {
        String query = "SELECT IdUsuario, Nombre, ApellidoPaterno, Rol FROM usuarios WHERE CorreoElectronico = ? AND Contrasenia = ? AND Estado = 1";
        Map<String, Object> usuarioData = new HashMap<>();
        try (Connection conn = getConnection();
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

    //Metodo para obtener el proximo ID
    public int obtenerProximoId(){
        String query = "SELECT NVL(MAX(IdUsuario), 0) + 1 AS NEXT_ID FROM USUARIOS";
        try{
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                return rs.getInt("NEXT_ID");
            }
            rs.close();
            conn.close();
        } catch(SQLException e){
            e.printStackTrace();
        }
        return 1; //Si hay error retorna 1 como valor por defecto
    }

    //Funcion de crear (C) del CRUD
    public boolean createUsuario(Usuario u) {
        String query = "INSERT INTO USUARIOS (IDUSUARIO, NOMBRE, APELLIDOPATERNO, APELLIDOMATERNO, CORREOELECTRONICO, CONTRASENIA, FECHANACIMIENTO, ROL, ESTADO) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, u.getIdUsuario());
            ps.setString(2, u.getNombre());
            ps.setString(3, u.getApellidoPaterno());
            ps.setString(4, u.getApellidoMaterno());
            ps.setString(5, u.getCorreoElectronico());
            ps.setString(6, u.getContrasenia());
            ps.setDate(7, u.getFechaNacimiento());
            ps.setInt(8, u.getRol() ? 1 : 0);  //Convertir boolean a NUMBER
            ps.setInt(9, u.getEstado() ? 1 : 0); //Convertir boolean a NUMBER
            int resultado = ps.executeUpdate();
            return resultado > 0;
        } catch (SQLException e) {
            System.err.println("Error al crear usuario: " + e.getMessage());
            //Verificar si es error de correo duplicado
            if (e.getMessage().contains("unique constraint") || e.getErrorCode() == 1) {
                System.err.println("Correo electrónico ya está en uso");
            }
            return false;
        }
    }

    //Funcion de lectura (R) del CRUD
    public List<Usuario> readUsuarios(){
        String query = "SELECT IdUsuario, Nombre, ApellidoPaterno, ApellidoMaterno, Rol, Estado FROM Usuarios ORDER BY Nombre ASC";
        List<Usuario> lista = new ArrayList<>();
        try{
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                Usuario u = new Usuario();
                u.setIdUsuario(rs.getInt("IdUsuario"));
                u.setNombre(rs.getString("Nombre"));
                u.setApellidoPaterno(rs.getString("ApellidoPaterno"));
                u.setApellidoMaterno(rs.getString("ApellidoMaterno"));
                u.setRol(rs.getBoolean("Rol"));
                int estadoNum = rs.getInt("Estado");
                u.setEstado(estadoNum == 1);
                lista.add(u);
            }
            rs.close();
            conn.close();
        }
        catch(SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }


}
