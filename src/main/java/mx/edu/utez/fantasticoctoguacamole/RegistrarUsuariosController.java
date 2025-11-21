package mx.edu.utez.fantasticoctoguacamole;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import mx.edu.utez.fantasticoctoguacamole.modelo.dao.UsuarioDao;
import mx.edu.utez.fantasticoctoguacamole.modelo.Usuario;
import java.sql.Date;
import java.time.LocalDate;

public class RegistrarUsuariosController {

    @FXML
    private TextField nombre;
    @FXML
    private TextField aPaterno;
    @FXML
    private TextField aMaterno;
    @FXML
    private TextField correo;
    @FXML
    private PasswordField contrasenia;
    @FXML
    private PasswordField confirmarContra;
    @FXML
    private DatePicker fechaNac;
    @FXML
    private ChoiceBox<String> roles;
    @FXML
    private Button agregarBoton;
    @FXML
    private ImageView verContrasenia1;
    @FXML
    private ImageView verContrasenia2;
    @FXML
    private Button regresarBoton;

    private TextField contraseniaVisible1;
    private TextField contraseniaVisible2;
    private boolean contrasenia1Visible = false;
    private boolean contrasenia2Visible = false;

    @FXML
    public void initialize() {
        //Inicializar ChoiceBox con roles
        roles.getItems().addAll("Administrador", "Cajero");
        roles.setValue("Cajero"); // Valor por defecto
        //Configurar funcionalidad de mostrar/ocultar contraseña
        configurarVisualizacionContrasenia();
    }

    private void configurarVisualizacionContrasenia() {
        //Crear TextFields para contraseñas visibles
        contraseniaVisible1 = new TextField();
        contraseniaVisible1.setPromptText("Ingrese contraseña");
        contraseniaVisible1.setManaged(false);
        contraseniaVisible1.setVisible(false);
        contraseniaVisible1.layoutXProperty().bind(contrasenia.layoutXProperty());
        contraseniaVisible1.layoutYProperty().bind(contrasenia.layoutYProperty());
        contraseniaVisible1.prefWidthProperty().bind(contrasenia.prefWidthProperty());
        contraseniaVisible1.prefHeightProperty().bind(contrasenia.prefHeightProperty());
        contraseniaVisible2 = new TextField();
        contraseniaVisible2.setPromptText("Ingrese contraseña");
        contraseniaVisible2.setManaged(false);
        contraseniaVisible2.setVisible(false);
        contraseniaVisible2.layoutXProperty().bind(confirmarContra.layoutXProperty());
        contraseniaVisible2.layoutYProperty().bind(confirmarContra.layoutYProperty());
        contraseniaVisible2.prefWidthProperty().bind(confirmarContra.prefWidthProperty());
        contraseniaVisible2.prefHeightProperty().bind(confirmarContra.prefHeightProperty());
        //Agregar al AnchorPane
        AnchorPane parent = (AnchorPane) contrasenia.getParent();
        parent.getChildren().addAll(contraseniaVisible1, contraseniaVisible2);
        //Sincronizar texto entre PasswordField y TextField
        contrasenia.textProperty().bindBidirectional(contraseniaVisible1.textProperty());
        confirmarContra.textProperty().bindBidirectional(contraseniaVisible2.textProperty());
        //Configurar eventos de los ImageView
        verContrasenia1.setOnMouseClicked(this::toggleContrasenia1);
        verContrasenia2.setOnMouseClicked(this::toggleContrasenia2);
    }

    @FXML
    private void toggleContrasenia1(MouseEvent event) {
        if (contrasenia1Visible) {
            // Ocultar contraseña
            contrasenia.setVisible(true);
            contrasenia.setManaged(true);
            contraseniaVisible1.setVisible(false);
            contraseniaVisible1.setManaged(false);
            contrasenia1Visible = false;
        } else {
            // Mostrar contraseña
            contrasenia.setVisible(false);
            contrasenia.setManaged(false);
            contraseniaVisible1.setVisible(true);
            contraseniaVisible1.setManaged(true);
            contrasenia1Visible = true;
        }
    }

    @FXML
    private void toggleContrasenia2(MouseEvent event) {
        if (contrasenia2Visible) {
            //Ocultar contraseña
            confirmarContra.setVisible(true);
            confirmarContra.setManaged(true);
            contraseniaVisible2.setVisible(false);
            contraseniaVisible2.setManaged(false);
            contrasenia2Visible = false;
        } else {
            //Mostrar contraseña
            confirmarContra.setVisible(false);
            confirmarContra.setManaged(false);
            contraseniaVisible2.setVisible(true);
            contraseniaVisible2.setManaged(true);
            contrasenia2Visible = true;
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    public void agregarUsuario(ActionEvent event) {
        //Recortar espacios sobrantes
        nombre.setText(nombre.getText().trim().replaceAll("\\s{2,}", " "));
        aPaterno.setText(aPaterno.getText().trim().replaceAll("\\s{2,}", " "));
        aMaterno.setText(aMaterno.getText().trim().replaceAll("\\s{2,}", " "));
        correo.setText(correo.getText().trim().replaceAll("\\s{2,}", " "));
        //Validar campos obligatorios
        if (nombre.getText().isEmpty() ||
                aPaterno.getText().isEmpty() ||
                correo.getText().isEmpty() ||
                contrasenia.getText().isEmpty() ||
                confirmarContra.getText().isEmpty() ||
                fechaNac.getValue() == null ||
                roles.getValue() == null) {
            mostrarAlerta("Error!", "Todos los campos marcados con * son obligatorios", Alert.AlertType.ERROR);
            return;
        }
        //Validar que las contraseñas coincidan
        if (!contrasenia.getText().equals(confirmarContra.getText())) {
            mostrarAlerta("Error!", "Las contraseñas no coinciden", Alert.AlertType.ERROR);
            return;
        }
        //Validar formato de nombre y apellidos
        if (!nombre.getText().matches("^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s]+$")) {
            mostrarAlerta("Error!", "El nombre solo debe contener letras y espacios", Alert.AlertType.ERROR);
            return;
        }
        if (!aPaterno.getText().matches("^[A-Za-zÁÉÍÓÚáéíóúÑñ]+$")) {
            mostrarAlerta("Error!", "El apellido paterno solo debe contener letras", Alert.AlertType.ERROR);
            return;
        }
        if (!aMaterno.getText().matches("^[A-Za-zÁÉÍÓÚáéíóúÑñ]+$")) {
            mostrarAlerta("Error!", "El apellido materno solo debe contener letras", Alert.AlertType.ERROR);
            return;
        }
        //Validar formato de correo
        if (!correo.getText().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            mostrarAlerta("Error!", "El formato del correo electrónico no es válido", Alert.AlertType.ERROR);
            return;
        }
        //Validar que la fecha de nacimiento sea en el pasado
        if (fechaNac.getValue().isAfter(LocalDate.now())) {
            mostrarAlerta("Error!", "La fecha de nacimiento no puede ser futura", Alert.AlertType.ERROR);
            return;
        }
        //Validar que el usuario sea mayor de edad
        if (fechaNac.getValue().plusYears(18).isAfter(LocalDate.now())) {
            mostrarAlerta("Error!", "El usuario debe ser mayor de edad (18+ años)", Alert.AlertType.ERROR);
            return;
        }
        //Validar contraseña
        if (!validarFortalezaContrasenia(contrasenia.getText())) {
            mostrarAlerta("Error!",
                    "La contraseña debe tener:\n- Mínimo 8 caracteres\n- Al menos una mayúscula\n- Al menos una minúscula\n- Al menos un número",
                    Alert.AlertType.ERROR);
            return;
        }
        try {
            //Obtener el proximo ID
            UsuarioDao dao = new UsuarioDao();
            int proximoId = dao.obtenerProximoId();
            //Convertir rol a boolean
            boolean rol = roles.getValue().equals("Administrador");
            //Crear objeto Usuario con booleanos
            Usuario usuario = new Usuario(
                    proximoId,
                    nombre.getText(),
                    aPaterno.getText(),
                    aMaterno.getText(),
                    correo.getText(),
                    contrasenia.getText(),
                    Date.valueOf(fechaNac.getValue()),
                    rol,           //boolean
                    true           //Estado activo por defecto (boolean)
            );
            //Intentar guardar en la base de datos
            if (dao.createUsuario(usuario)) {
                //Limpiar campos
                limpiarCampos();
                mostrarAlerta("Éxito", "Usuario registrado correctamente", Alert.AlertType.INFORMATION);
            } else {
                mostrarAlerta("Error!", "No se pudo registrar el usuario. El correo podría estar en uso.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            mostrarAlerta("Error!", "Ocurrió un error inesperado: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private boolean validarFortalezaContrasenia(String contrasenia) {
        //Minimo 8 caracteres, al menos una mayuscula, una minuscula y un numero
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";
        return contrasenia.matches(regex);
    }

    private void limpiarCampos() {
        nombre.clear();
        aPaterno.clear();
        aMaterno.clear();
        correo.clear();
        contrasenia.clear();
        confirmarContra.clear();
        fechaNac.setValue(null);
        roles.setValue("Cajero");
        //Asegurarse de que los PasswordField esten visibles
        if (contrasenia1Visible) toggleContrasenia1(null);
        if (contrasenia2Visible) toggleContrasenia2(null);
    }

    @FXML
    void regresarMenu(ActionEvent event) {
        Stage currentStage = (Stage) regresarBoton.getScene().getWindow();
        currentStage.close();
    }
}