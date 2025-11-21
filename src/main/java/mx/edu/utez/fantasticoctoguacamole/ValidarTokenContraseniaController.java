package mx.edu.utez.fantasticoctoguacamole;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import mx.edu.utez.fantasticoctoguacamole.modelo.GestorTokens;
import mx.edu.utez.fantasticoctoguacamole.modelo.dao.UsuarioDao;

public class ValidarTokenContraseniaController {
    @FXML
    private TextField correoField;
    @FXML
    private TextField tokenField;
    @FXML
    private PasswordField nuevaContraseniaField;
    @FXML
    private PasswordField confirmarContraseniaField;
    @FXML
    private Button restablecerBtn;
    @FXML
    private Button volverBtn;

    private String correo;

    public void setCorreo(String correo) {
        this.correo = correo;
        correoField.setText(correo);
        correoField.setDisable(true); //No permitir editar el correo
    }

    @FXML
    private void restablecerContrasenia() {
        String token = tokenField.getText().trim();
        String nuevaContrasenia = nuevaContraseniaField.getText();
        String confirmarContrasenia = confirmarContraseniaField.getText();
        //Validaciones
        if (token.isEmpty() || nuevaContrasenia.isEmpty() || confirmarContrasenia.isEmpty()) {
            mostrarAlerta("Error", "Todos los campos son obligatorios", Alert.AlertType.ERROR);
            return;
        }
        if (!nuevaContrasenia.equals(confirmarContrasenia)) {
            mostrarAlerta("Error", "Las contraseñas no coinciden", Alert.AlertType.ERROR);
            return;
        }
        if (!validarFortalezaContrasenia(nuevaContrasenia)) {
            mostrarAlerta("Error",
                    "La contraseña debe tener:\n- Mínimo 8 caracteres\n- Al menos una mayúscula\n- Al menos una minúscula\n- Al menos un número",
                    Alert.AlertType.ERROR);
            return;
        }
        try {
            //Validar token
            GestorTokens gestorTokens = GestorTokens.getInstancia();
            gestorTokens.limpiarTokensExpirados(); // Limpiar tokens expirados primero
            if (!gestorTokens.validarToken(token, correo)) {
                mostrarAlerta("Error",
                        "Token inválido o expirado. Solicita un nuevo token.",
                        Alert.AlertType.ERROR);
                return;
            }
            //Obtener ID del usuario y actualizar contraseña
            int idUsuario = gestorTokens.obtenerIdUsuario(token);
            UsuarioDao usuarioDao = new UsuarioDao();
            boolean actualizado = usuarioDao.actualizarContrasenia(idUsuario, nuevaContrasenia);
            if (actualizado) {
                gestorTokens.marcarComoUsado(token); // Marcar token como usado
                mostrarAlerta("Éxito", "Contraseña actualizada correctamente", Alert.AlertType.INFORMATION);
                volverALogin();
            } else {
                mostrarAlerta("Error", "No se pudo actualizar la contraseña", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "Ocurrió un error inesperado: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void volverASolicitud() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("RecuperarContrasenia.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Recuperar Contraseña");
            stage.setScene(new Scene(root));
            stage.show();

            // Cerrar ventana actual
            Stage currentStage = (Stage) volverBtn.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void volverALogin() {
        Stage currentStage = (Stage) volverBtn.getScene().getWindow();
        currentStage.close();
    }

    private boolean validarFortalezaContrasenia(String contrasenia) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";
        return contrasenia.matches(regex);
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}