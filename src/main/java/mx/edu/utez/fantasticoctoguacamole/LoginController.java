package mx.edu.utez.fantasticoctoguacamole;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.concurrent.Task;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mx.edu.utez.fantasticoctoguacamole.modelo.SesionUsuario;
import mx.edu.utez.fantasticoctoguacamole.modelo.UsuarioDao;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

public class LoginController {
    @FXML
    private TextField correo;
    @FXML
    private PasswordField contrasenia;
    @FXML
    private CheckBox recordarme;
    @FXML
    private Label olvide;
    @FXML
    private Button iniciar;
    @FXML
    private ProgressIndicator spinner;

    //Configuración de seguridad
    private static final int MAX_INTENTOS = 3;
    private static final int TIEMPO_BLOQUEO_MINUTOS = 15;
    //Preferences para persistencia
    private Preferences prefs;
    private static final String PREF_USERNAME = "saved_username";
    private static final String PREF_PASSWORD = "saved_password";
    private static final String PREF_REMEMBER = "remember_me";

    @FXML
    public void initialize() {
        prefs = Preferences.userNodeForPackage(LoginController.class);
        //Cargar credenciales guardadas
        boolean remember = prefs.getBoolean(PREF_REMEMBER, false);
        if (remember) {
            String savedUsername = prefs.get(PREF_USERNAME, "");
            String savedPassword = prefs.get(PREF_PASSWORD, "");
            correo.setText(savedUsername);
            contrasenia.setText(savedPassword);
            recordarme.setSelected(true);
        }

        //Configurar el label como clickeable
        olvide.setOnMouseClicked(event -> recuperarContrasenia());

        //Configurar spinner
        if (spinner != null) {
            spinner.setVisible(false);
        }

        //Limpiar bloqueos expirados y actualizar estado del boton
        limpiarBloqueosExpirados();
        actualizarEstadoBoton();

        //Listener para actualizar estado del boton cuando cambie el correo
        correo.textProperty().addListener((observable, oldValue, newValue) -> {
            actualizarEstadoBoton();
        });
    }

    @FXML
    void iniciarSesion(ActionEvent event) {
        String correoTxt = correo.getText().trim();
        String contraseniaTxt = contrasenia.getText();
        //Validar si el usuario está bloqueado
        if (estaUsuarioBloqueado(correoTxt)) {
            long tiempoRestante = obtenerTiempoRestanteBloqueo(correoTxt);
            mostrarAlerta(Alert.AlertType.WARNING, "Cuenta bloqueada",
                    "Demasiados intentos fallidos. Espere " + tiempoRestante + " minutos antes de intentar nuevamente.");
            return;
        }

        //Validaciones basicas
        if (correoTxt.isEmpty() || contraseniaTxt.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos vacíos",
                    "Por favor, ingresa tu correo y contraseña");
            return;
        }
        if (!validarFormatoCorreo(correoTxt)) {
            mostrarAlerta(Alert.AlertType.WARNING, "Formato incorrecto",
                    "Por favor, ingresa un correo electrónico válido");
            return;
        }

        setInterfazDeshabilitada(true);

        Task<Map<String, Object>> tareaLogin = new Task<>() {
            @Override
            protected Map<String, Object> call() {
                try {
                    Thread.sleep(500);
                    UsuarioDao dao = new UsuarioDao();
                    return dao.validarUsuarioYObternerDatos(correoTxt, contraseniaTxt);
                } catch (Exception e) {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("valido", false);
                    errorResult.put("error", e.getMessage());
                    return errorResult;
                }
            }
        };

        tareaLogin.setOnSucceeded(e -> {
            Map<String, Object> resultado = tareaLogin.getValue();
            boolean valido = (Boolean) resultado.get("valido");
            setInterfazDeshabilitada(false);
            if (valido) {
                //Login exitoso, resetear intentos
                resetearIntentos(correoTxt);
                //Guardar credenciales si Recordarme esta activado
                if (recordarme.isSelected()) {
                    guardarCredenciales(correoTxt, contraseniaTxt);
                } else {
                    limpiarCredencialesGuardadas();
                }
                guardarDatosSesion(resultado);
                redirigirSegunRol((Integer) resultado.get("rol"));
            } else {
                //Login fallido, incrementar intentos
                incrementarIntento(correoTxt);
                int intentosRestantes = MAX_INTENTOS - getIntentos(correoTxt);
                if (intentosRestantes <= 0) {
                    bloquearUsuario(correoTxt);
                    long tiempoRestante = obtenerTiempoRestanteBloqueo(correoTxt);
                    mostrarAlerta(Alert.AlertType.ERROR, "Cuenta bloqueada",
                            "Demasiados intentos fallidos. Su cuenta ha sido bloqueada por " +
                                    tiempoRestante + " minutos.");
                } else {
                    String error = (String) resultado.get("error");
                    if (error != null) {
                        mostrarAlerta(Alert.AlertType.ERROR, "Error de conexión",
                                "Problema al conectar con la base de datos: " + error);
                    } else {
                        mostrarAlerta(Alert.AlertType.ERROR, "Error de autenticación",
                                "Correo o contraseña incorrectos. " +
                                        "Le quedan " + intentosRestantes + " intentos.");
                    }
                }
            }
            actualizarEstadoBoton();
        });
        tareaLogin.setOnFailed(e -> {
            setInterfazDeshabilitada(false);
            mostrarAlerta(Alert.AlertType.ERROR, "Error",
                    "Ocurrió un error inesperado durante la autenticación");
        });
        new Thread(tareaLogin).start();
    }

    //Control de intentos y bloqueo
    private boolean estaUsuarioBloqueado(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            return false;
        }
        String correoNormalizado = correo.toLowerCase().trim();
        long tiempoBloqueo = prefs.getLong(correoNormalizado + "_bloqueo", 0);
        if (tiempoBloqueo > 0) {
            long tiempoTranscurrido = (System.currentTimeMillis() - tiempoBloqueo) / (60 * 1000);
            if (tiempoTranscurrido < TIEMPO_BLOQUEO_MINUTOS) {
                return true;
            } else {
                //Desbloquear automaticamente despues del tiempo
                prefs.remove(correoNormalizado + "_bloqueo");
                prefs.remove(correoNormalizado + "_intentos");
                return false;
            }
        }
        return false;
    }

    private long obtenerTiempoRestanteBloqueo(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            return 0;
        }
        String correoNormalizado = correo.toLowerCase().trim();
        long tiempoBloqueo = prefs.getLong(correoNormalizado + "_bloqueo", 0);
        if (tiempoBloqueo > 0) {
            long tiempoTranscurrido = (System.currentTimeMillis() - tiempoBloqueo) / (60 * 1000);
            long tiempoRestante = TIEMPO_BLOQUEO_MINUTOS - tiempoTranscurrido;
            return Math.max(1, tiempoRestante);
        }
        return 0;
    }

    private void incrementarIntento(String correo) {
        if (correo == null || correo.trim().isEmpty()) return;
        String correoNormalizado = correo.toLowerCase().trim();
        int intentos = prefs.getInt(correoNormalizado + "_intentos", 0) + 1;
        prefs.putInt(correoNormalizado + "_intentos", intentos);
        if (intentos >= MAX_INTENTOS) {
            prefs.putLong(correoNormalizado + "_bloqueo", System.currentTimeMillis());
        }
    }

    private int getIntentos(String correo) {
        if (correo == null || correo.trim().isEmpty()) return 0;
        return prefs.getInt(correo.toLowerCase().trim() + "_intentos", 0);
    }

    private void resetearIntentos(String correo) {
        if (correo == null || correo.trim().isEmpty()) return;
        String correoNormalizado = correo.toLowerCase().trim();
        prefs.remove(correoNormalizado + "_intentos");
        prefs.remove(correoNormalizado + "_bloqueo");
    }

    private void bloquearUsuario(String correo) {
        if (correo == null || correo.trim().isEmpty()) return;
        String correoNormalizado = correo.toLowerCase().trim();
        prefs.putLong(correoNormalizado + "_bloqueo", System.currentTimeMillis());
        prefs.putInt(correoNormalizado + "_intentos", MAX_INTENTOS);
    }

    private void limpiarBloqueosExpirados() {
        try {
            String[] keys = prefs.keys();
            if (keys == null) return;
            for (String key : keys) {
                if (key.endsWith("_bloqueo")) {
                    long tiempoBloqueo = prefs.getLong(key, 0);
                    if (tiempoBloqueo > 0) {
                        long tiempoTranscurrido = (System.currentTimeMillis() - tiempoBloqueo) / (60 * 1000);
                        if (tiempoTranscurrido >= TIEMPO_BLOQUEO_MINUTOS) {
                            String correo = key.replace("_bloqueo", "");
                            prefs.remove(key);
                            prefs.remove(correo + "_intentos");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al limpiar bloqueos expirados: " + e.getMessage());
        }
    }

    private void actualizarEstadoBoton() {
        String correoTxt = correo.getText().trim();
        if (!correoTxt.isEmpty() && estaUsuarioBloqueado(correoTxt)) {
            iniciar.setDisable(true);
            long tiempoRestante = obtenerTiempoRestanteBloqueo(correoTxt);
            iniciar.setText("Bloqueado (" + tiempoRestante + " min)");
        } else {
            iniciar.setDisable(false);
            iniciar.setText("Iniciar sesión");
        }
    }

    //Metodos de ayuda
    private void setInterfazDeshabilitada(boolean deshabilitado) {
        correo.setDisable(deshabilitado);
        contrasenia.setDisable(deshabilitado);
        iniciar.setDisable(deshabilitado);
        olvide.setDisable(deshabilitado);

        if (spinner != null) {
            spinner.setVisible(deshabilitado);
        }
    }

    private void guardarCredenciales(String usuario, String contrasenia) {
        prefs.putBoolean(PREF_REMEMBER, true);
        prefs.put(PREF_USERNAME, usuario);
        prefs.put(PREF_PASSWORD, contrasenia);
    }

    private void limpiarCredencialesGuardadas() {
        prefs.putBoolean(PREF_REMEMBER, false);
        prefs.remove(PREF_USERNAME);
        prefs.remove(PREF_PASSWORD);
    }

    private boolean validarFormatoCorreo(String correo) {
        if (correo == null || correo.trim().isEmpty()) return false;
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return correo.matches(emailRegex);
    }

    private void recuperarContrasenia() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("RecuperarContrasenia.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Recuperar Contraseña");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException ex) {
            ex.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error",
                    "No se pudo cargar la ventana de recuperación");
        }
    }

    private void guardarDatosSesion(Map<String, Object> usuarioData) {
        SesionUsuario.iniciarSesion(
        (Integer) usuarioData.get("idUsuario"),
        (String) usuarioData.get("nombre"),
        (String) usuarioData.get("apellidoPaterno"),
        (Integer) usuarioData.get("rol"),
        correo.getText().trim()
        );
    }

    private void redirigirSegunRol(int rol) {
        try {
            String fxmlFile = rol == 1 ? "AdminDashboard.fxml" : "UserDashboard.fxml";
            String titulo = rol == 1 ? "ElectroStock - Panel Administrador" : "ElectroStock - Panel Usuario";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.setScene(new Scene(root));
            stage.show();
            ((Stage) correo.getScene().getWindow()).close();
        } catch (IOException ex) {
            ex.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error",
                    "No se pudo cargar la pantalla principal");
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
