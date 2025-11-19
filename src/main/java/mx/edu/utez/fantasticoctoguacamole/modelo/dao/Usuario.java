package mx.edu.utez.fantasticoctoguacamole.modelo.dao;

import java.sql.Date;

public class Usuario {
    private int IdUsuario;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String correoElectronico;
    private String contrasenia;
    private Date fechaNacimiento;
    private boolean rol;
    private boolean estado;

    //Constructores
    public Usuario() {}

    //Con true por defecto
    public Usuario(String nombre, String apellidoPaterno, String apellidoMaterno, String correoElectronico, String contrasenia, Date fechaNacimiento, boolean rol, boolean estado) {
        this.nombre = nombre;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.correoElectronico = correoElectronico;
        this.contrasenia = contrasenia;
        this.fechaNacimiento = fechaNacimiento;
        this.rol = rol;
        this.estado = true;
    }

    //Constructor para cuando ya conoces el ID
    public Usuario(int idUsuario, String nombre, String apellidoPaterno, String apellidoMaterno, String correoElectronico, String contrasenia, Date fechaNacimiento, boolean rol, boolean estado) {
        IdUsuario = idUsuario;
        this.nombre = nombre;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.correoElectronico = correoElectronico;
        this.contrasenia = contrasenia;
        this.fechaNacimiento = fechaNacimiento;
        this.rol = rol;
        this.estado = estado;
    }

    //ID
    public int getIdUsuario() {
        return IdUsuario;
    }
    public void setIdUsuario(int idUsuario) {
        IdUsuario = idUsuario;
    }
    //Nombre
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    //Apellido paterno
    public String getApellidoPaterno() {
        return apellidoPaterno;
    }
    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }
    //Apellido materno
    public String getApellidoMaterno() {
        return apellidoMaterno;
    }
    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }
    //Correo electronico
    public String getCorreoElectronico() {
        return correoElectronico;
    }
    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }
    //Contrasenia
    public String getContrasenia() {
        return contrasenia;
    }
    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }
    //Fecha de nacimiento
    public Date getFechaNacimiento() {
        return fechaNacimiento;
    }
    public void setFechaNacimiento(Date fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }
    //Rol
    public boolean isRol() {
        return rol;
    }
    public void setRol(boolean rol) {
        this.rol = rol;
    }
    //Estado
    public boolean isEstado() {
        return estado;
    }
    public void setEstado(boolean estado) {
        this.estado = estado;
    }
}
