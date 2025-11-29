package mx.edu.utez.fantasticoctoguacamole;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import mx.edu.utez.fantasticoctoguacamole.modelo.dao.UsuarioDao;
import mx.edu.utez.fantasticoctoguacamole.modelo.Usuario;
import java.sql.Date;
import java.time.LocalDate;
import java.net.URL;
import java.util.ResourceBundle;

public class RegistrarUsuariosController implements Initializable {

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
    private ImageView ocultarContra1;
    @FXML
    private ImageView ocultarContra2;
    @FXML
    private Button regresarBoton;

    private TextField contraseniaVisible1;
    private TextField contraseniaVisible2;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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
        contraseniaVisible1.getStyleClass().add("text-field");
        contraseniaVisible2 = new TextField();
        contraseniaVisible2.setPromptText("Confirme la contraseña");
        contraseniaVisible2.getStyleClass().add("text-field");
        //Posicionar los TextFields exactamente sobre los PasswordFields
        AnchorPane parent1 = (AnchorPane) contrasenia.getParent();
        parent1.getChildren().add(contraseniaVisible1);
        AnchorPane parent2 = (AnchorPane) confirmarContra.getParent();
        parent2.getChildren().add(contraseniaVisible2);
        //Configurar layout de los TextFields
        contraseniaVisible1.setLayoutX(contrasenia.getLayoutX());
        contraseniaVisible1.setLayoutY(contrasenia.getLayoutY());
        contraseniaVisible1.setPrefWidth(contrasenia.getPrefWidth());
        contraseniaVisible1.setPrefHeight(contrasenia.getPrefHeight());
        contraseniaVisible2.setLayoutX(confirmarContra.getLayoutX());
        contraseniaVisible2.setLayoutY(confirmarContra.getLayoutY());
        contraseniaVisible2.setPrefWidth(confirmarContra.getPrefWidth());
        contraseniaVisible2.setPrefHeight(confirmarContra.getPrefHeight());
        //Inicialmente ocultar los TextFields
        contraseniaVisible1.setVisible(false);
        contraseniaVisible2.setVisible(false);
        //Sincronizar texto entre PasswordField y TextField
        contrasenia.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!contraseniaVisible1.isFocused()) {
                contraseniaVisible1.setText(newValue);
            }
        });
        contraseniaVisible1.textProperty().addListener((observable, oldValue, newValue) -> {
            if (contraseniaVisible1.isFocused()) {
                contrasenia.setText(newValue);
            }
        });
        confirmarContra.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!contraseniaVisible2.isFocused()) {
                contraseniaVisible2.setText(newValue);
            }
        });
        contraseniaVisible2.textProperty().addListener((observable, oldValue, newValue) -> {
            if (contraseniaVisible2.isFocused()) {
                confirmarContra.setText(newValue);
            }
        });
        //Configurar eventos de los ImageView
        verContrasenia1.setOnMouseClicked(this::mostrarContrasenia1);
        verContrasenia2.setOnMouseClicked(this::mostrarContrasenia2);
        ocultarContra1.setOnMouseClicked(this::ocultarContrasenia1);
        ocultarContra2.setOnMouseClicked(this::ocultarContrasenia2);
    }

    @FXML
    private void mostrarContrasenia1(MouseEvent event) {
        contrasenia.setVisible(false);
        contraseniaVisible1.setVisible(true);
        verContrasenia1.setVisible(false);
        ocultarContra1.setVisible(true);
        //Enfocar el campo visible
        contraseniaVisible1.requestFocus();
        contraseniaVisible1.positionCaret(contraseniaVisible1.getText().length());
    }

    @FXML
    private void ocultarContrasenia1(MouseEvent event) {
        contrasenia.setVisible(true);
        contraseniaVisible1.setVisible(false);
        verContrasenia1.setVisible(true);
        ocultarContra1.setVisible(false);
        //Enfocar el campo de contraseña
        contrasenia.requestFocus();
        contrasenia.positionCaret(contrasenia.getText().length());
    }

    @FXML
    private void mostrarContrasenia2(MouseEvent event) {
        confirmarContra.setVisible(false);
        contraseniaVisible2.setVisible(true);
        verContrasenia2.setVisible(false);
        ocultarContra2.setVisible(true);
        //Enfocar el campo visible
        contraseniaVisible2.requestFocus();
        contraseniaVisible2.positionCaret(contraseniaVisible2.getText().length());
    }

    @FXML
    private void ocultarContrasenia2(MouseEvent event) {
        confirmarContra.setVisible(true);
        contraseniaVisible2.setVisible(false);
        verContrasenia2.setVisible(true);
        ocultarContra2.setVisible(false);
        //Enfocar el campo de contraseña
        confirmarContra.requestFocus();
        confirmarContra.positionCaret(confirmarContra.getText().length());
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
        //Validar apellido materno (no obligatorio)
        String aMaternoText = aMaterno.getText().trim();
        if (!aMaternoText.isEmpty() && !aMaternoText.matches("^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s]+$")) {
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
        contraseniaVisible1.clear();
        contraseniaVisible2.clear();
        fechaNac.setValue(null);
        roles.setValue("Cajero");
        //Asegurarse de que los PasswordField estén visibles
        if (!contrasenia.isVisible()) {
            ocultarContrasenia1(null);
        }
        if (!confirmarContra.isVisible()) {
            ocultarContrasenia2(null);
        }
    }

    @FXML
    void regresarMenu(ActionEvent event) {
        Stage currentStage = (Stage) regresarBoton.getScene().getWindow();
        currentStage.close();
    }
}