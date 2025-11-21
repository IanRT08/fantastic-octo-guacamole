package mx.edu.utez.fantasticoctoguacamole.modelo;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GestorTokens {
    private static GestorTokens instancia;
    private Map<String, TokenInfo> tokens = new HashMap<>();

    private GestorTokens() {}

    public static GestorTokens getInstancia() {
        if (instancia == null) {
            instancia = new GestorTokens();
        }
        return instancia;
    }

    public String generarToken(int idUsuario, String email) {
        //Eliminar tokens existentes para este usuario
        tokens.entrySet().removeIf(entry -> entry.getValue().getIdUsuario() == idUsuario);
        //Generar nuevo token
        String token = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        LocalDateTime expiracion = LocalDateTime.now().plusMinutes(15);
        tokens.put(token, new TokenInfo(idUsuario, email, expiracion, false));
        System.out.println("Token generado: " + token + " para usuario: " + email);
        return token;
    }

    public boolean validarToken(String token, String email) {
        TokenInfo info = tokens.get(token);
        if (info == null) {
            return false;
        }
        //Verificar que coincida el email y no este expirado
        boolean valido = info.getEmail().equals(email) &&
                !info.isExpirado() &&
                !info.isUsado();
        if (!valido) {
            tokens.remove(token); //Limpiar token invalido
        }
        return valido;
    }

    public void marcarComoUsado(String token) {
        TokenInfo info = tokens.get(token);
        if (info != null) {
            info.setUsado(true);
        }
    }

    public int obtenerIdUsuario(String token) {
        TokenInfo info = tokens.get(token);
        return info != null ? info.getIdUsuario() : -1;
    }

    public void limpiarTokensExpirados() {
        tokens.entrySet().removeIf(entry -> entry.getValue().isExpirado());
    }

    //Clase interna para almacenar información del token
    private static class TokenInfo {
        private int idUsuario;
        private String email;
        private LocalDateTime expiracion;
        private boolean usado;
        public TokenInfo(int idUsuario, String email, LocalDateTime expiracion, boolean usado) {
            this.idUsuario = idUsuario;
            this.email = email;
            this.expiracion = expiracion;
            this.usado = usado;
        }
        public int getIdUsuario() { return idUsuario; }
        public String getEmail() { return email; }
        public boolean isExpirado() { return LocalDateTime.now().isAfter(expiracion); }
        public boolean isUsado() { return usado; }
        public void setUsado(boolean usado) { this.usado = usado; }
    }
}