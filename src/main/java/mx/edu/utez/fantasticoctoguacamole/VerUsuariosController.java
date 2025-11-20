package mx.edu.utez.fantasticoctoguacamole;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import mx.edu.utez.fantasticoctoguacamole.modelo.dao.Usuario;
import java.net.URL;
import java.util.ResourceBundle;

public class VerUsuariosController implements Initializable {

    @FXML
    private TextField nombre;
    @FXML
    private TextField aPaterno;
    @FXML
    private TextField aMaterno;
    @FXML
    private TextField correo;
    @FXML
    private DatePicker fechaNac;
    @FXML
    private TextField rol;
    @FXML
    private Button volver;

    private Usuario usuario;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Configurar el boton volver
        volver.setOnAction(event -> {
            Stage stage = (Stage) volver.getScene().getWindow();
            stage.close();
        });
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        cargarDatosUsuario();
    }

    private void cargarDatosUsuario() {
        if (usuario != null) {
            //Cargar datos básicos
            nombre.setText(usuario.getNombre());
            aPaterno.setText(usuario.getApellidoPaterno());
            aMaterno.setText(usuario.getApellidoMaterno());
            correo.setText(usuario.getCorreoElectronico());
            //Cargar fecha de nacimiento
            if (usuario.getFechaNacimiento() != null) {
                fechaNac.setValue(usuario.getFechaNacimiento().toLocalDate());
                System.out.println("Fecha cargada: " + usuario.getFechaNacimiento().toLocalDate());
            } else {
                System.out.println("Fecha es null");
            }
            //Convertir rol boolean a texto
            String rolTexto = usuario.getRol() ? "Administrador" : "Cajero";
            rol.setText(rolTexto);
            System.out.println("Rol: " + rolTexto);
        } else {
            System.out.println("Usuario es null en cargarDatosUsuario");
        }
    }
}