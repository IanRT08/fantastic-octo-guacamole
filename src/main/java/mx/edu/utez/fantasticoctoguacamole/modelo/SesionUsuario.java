package mx.edu.utez.fantasticoctoguacamole.modelo;

public class SesionUsuario {
    private static int idUsuario;
    private static String nombre;
    private static String apellidoPaterno;
    private static String email;
    private static int rol;
    private static boolean sesionActiva = false;
    private static Usuario usuarioActual;

    //Getters y Setters
    public static int getIdUsuario() {
        return idUsuario;
    }
    public static void setIdUsuario(int idUsuario) {
        SesionUsuario.idUsuario = idUsuario;
    }

    public static String getNombre() {
        return nombre;
    }
    public static void setNombre(String nombre) {
        SesionUsuario.nombre = nombre;
    }

    public static String getApellidoPaterno() {
        return apellidoPaterno;
    }
    public static void setApellidoPaterno(String apellidoPaterno) {
        SesionUsuario.apellidoPaterno = apellidoPaterno;
    }

    public static String getEmail() {
        return email;
    }
    public static void setEmail(String email) {
        SesionUsuario.email = email;
    }

    public static int getRol() {
        return rol;
    }
    public static void setRol(int rol) {
        SesionUsuario.rol = rol;
    }

    public static boolean isSesionActiva() {
        return sesionActiva;
    }
    public static void setSesionActiva(boolean sesionActiva) {
        SesionUsuario.sesionActiva = sesionActiva;
    }

    public static String getNombreCompleto() {
        if (apellidoPaterno != null && !apellidoPaterno.isEmpty()) {
            return nombre + " " + apellidoPaterno;
        }
        return nombre;
    }
    public static boolean esAdministrador() {
        return rol == 1;
    }

    public static boolean esUsuarioNormal() {
        return rol == 0;
    }

    public static void iniciarSesion(int id, String nombre, String apellido, int rol, String email) {
        idUsuario = id;
        SesionUsuario.nombre = nombre;
        apellidoPaterno = apellido;
        SesionUsuario.rol = rol;
        SesionUsuario.email = email;
        sesionActiva = true;
    }

    public static void cerrarSesion() {
        idUsuario = 0;
        nombre = null;
        apellidoPaterno = null;
        email = null;
        rol = 0;
        sesionActiva = false;
    }

    public static boolean haySesionActiva() {
        return sesionActiva && idUsuario > 0;
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public static void setUsuarioActual(Usuario usuario) {
        usuarioActual = usuario;
    }

}