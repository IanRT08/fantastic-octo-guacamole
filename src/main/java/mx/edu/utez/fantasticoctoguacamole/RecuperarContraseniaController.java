package mx.edu.utez.fantasticoctoguacamole;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import mx.edu.utez.fantasticoctoguacamole.modelo.GestorTokens;
import mx.edu.utez.fantasticoctoguacamole.modelo.dao.UsuarioDao;
import mx.edu.utez.fantasticoctoguacamole.servicios.ServicioEmail;

public class RecuperarContraseniaController {

    @FXML
    private TextField correoField;
    @FXML
    private Button enviarTokenBtn;
    @FXML
    private Button volverBtn;

    @FXML
    private void solicitarToken() {
        String correo = correoField.getText().trim();
        if (correo.isEmpty()) {
            mostrarAlerta("Error", "El correo electrónico es obligatorio", Alert.AlertType.ERROR);
            return;
        }
        if (!validarFormatoCorreo(correo)) {
            mostrarAlerta("Error", "El formato del correo electrónico no es válido", Alert.AlertType.ERROR);
            return;
        }
        try {
            //Verificar si el correo existe
            UsuarioDao usuarioDao = new UsuarioDao();
            var usuarioData = usuarioDao.obtenerUsuarioPorCorreo(correo);
            if (usuarioData == null) {
                mostrarAlerta("Error", "No se encontró ningún usuario con ese correo electrónico", Alert.AlertType.ERROR);
                return;
            }
            //Generar token
            GestorTokens gestorTokens = GestorTokens.getInstancia();
            String token = gestorTokens.generarToken(usuarioData.getIdUsuario(), correo);
            //Enviar token por email
            ServicioEmail servicioEmail = new ServicioEmail();
            boolean emailEnviado = servicioEmail.enviarTokenRecuperacion(correo, token);
            if (emailEnviado) {
                // Abrir pantalla de validación de token
                abrirValidacionToken(correo);
            } else {
                mostrarAlerta("Error", "No se pudo enviar el token. Intenta nuevamente.", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            mostrarAlerta("Error", "Ocurrió un error inesperado: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void abrirValidacionToken(String correo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ValidarTokenContrasenia.fxml"));
            Parent root = loader.load();
            ValidarTokenContraseniaController controller = loader.getController();
            controller.setCorreo(correo);
            Stage stage = new Stage();
            stage.setTitle("Restablecer Contraseña");
            stage.setScene(new Scene(root));
            stage.show();
            //Cerrar ventana actual
            Stage currentStage = (Stage) enviarTokenBtn.getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo cargar la siguiente pantalla", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void volverALogin() {
        Stage currentStage = (Stage) volverBtn.getScene().getWindow();
        currentStage.close();
    }

    private boolean validarFormatoCorreo(String correo) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return correo.matches(emailRegex);
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}