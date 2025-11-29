package mx.edu.utez.fantasticoctoguacamole;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import mx.edu.utez.fantasticoctoguacamole.modelo.SesionUsuario;
import mx.edu.utez.fantasticoctoguacamole.modelo.Usuario;
import mx.edu.utez.fantasticoctoguacamole.modelo.dao.UsuarioDao;

import java.io.IOException;

public class UserDashboardController {

    @FXML
    private Rectangle tarjeta; //Gestion de productos
    @FXML
    private Rectangle tarjeta2; //Punto de venta
    @FXML
    private Label cerrarSesion;
    @FXML
    private Button btnEditarPerfil;

    private Usuario usuarioEnSesion;

    @FXML
    public void initialize() {
        //Cargar el usuario desde la sesión
        cargarUsuarioDesdeSesion();
        //Hacer las tarjetas clickeables
        configurarTarjetasClickeables();
    }

    private void cargarUsuarioDesdeSesion() {
        int idUsuario = SesionUsuario.getIdUsuario();
        if (idUsuario > 0) {
            UsuarioDao dao = new UsuarioDao();
            this.usuarioEnSesion = dao.obtenerUsuarioPorId(idUsuario);
            if (usuarioEnSesion != null) {
                System.out.println("Usuario cargado desde sesión: " + usuarioEnSesion.getNombre());
            } else {
                System.out.println("No se pudo cargar el usuario desde la BD");
            }
        } else {
            System.out.println("No hay ID de usuario en la sesión");
        }
    }

    public void setUsuarioEnSesion(Usuario usuario) {
        this.usuarioEnSesion = usuario;
        System.out.println("Usuario en sesión establecido: " +
                (usuario != null ? usuario.getNombre() : "null"));
    }

    private void configurarTarjetasClickeables() {
        //Tarjeta 1 - Gestion de productos
        tarjeta.setOnMouseClicked(this::manejarClickGestionProductos);
        //Tarjeta 2 - Punto de venta
        tarjeta2.setOnMouseClicked(this::manejarClickPuntoVenta);
        //Cerrar sesion
        cerrarSesion.setOnMouseClicked(this::manejarCerrarSesion);
    }

    @FXML
    private void manejarClickGestionProductos(MouseEvent event) {
        cargarPantalla("GestionProductos.fxml", "Gestión de Productos", (controller -> {}));
    }

    @FXML
    private void manejarClickPuntoVenta(MouseEvent event) {
        cargarPantalla("PuntoVenta.fxml", "Punto de Venta", (controller) -> {
            if (controller instanceof PuntoVentaController) {
                ((PuntoVentaController) controller).setIdUsuarioActual(SesionUsuario.getIdUsuario());
            }
        });
    }

    @FXML
    private void manejarCerrarSesion(MouseEvent event) {
        try {
            //Confirmar cierre de sesión
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Cerrar Sesión");
            confirmacion.setHeaderText("¿Estás seguro de que quieres cerrar sesión?");
            confirmacion.setContentText("Serás redirigido al login.");
            if (confirmacion.showAndWait().get().getText().equals("Aceptar")) {
                //Cerrar sesion
                SesionUsuario.cerrarSesion();
                //Cargar ventana de login
                FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
                Parent root = loader.load();
                Stage stage = new Stage();
                stage.setTitle("ElectroStock - Login");
                stage.setScene(new Scene(root));
                stage.show();
                //Cerrar dashboard actual
                Stage currentStage = (Stage) cerrarSesion.getScene().getWindow();
                currentStage.close();
                System.out.println("Sesión cerrada exitosamente");
            }
        } catch (IOException e) {
            mostrarError("Error al cerrar sesión: " + e.getMessage());
        }
    }

    private void cargarPantalla(String fxmlFile, String titulo, java.util.function.Consumer<Object> configuradorController) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            //Configurar el controlador si se proporciona un configurador
            if (configuradorController != null) {
                configuradorController.accept(loader.getController());
            }
            //Obtener la ventana actual y reemplazar la escena
            Stage currentStage = (Stage) tarjeta.getScene().getWindow(); //Usar cualquier nodo de la escena actual
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("ElectroStock - " + titulo);
        } catch (IOException e) {
            mostrarError("Error al cargar " + titulo + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void abrirEditarUsuario() {
        try {
            // Asegurarse de que el usuario esté cargado
            if (usuarioEnSesion == null) {
                cargarUsuarioDesdeSesion();
            }

            if (usuarioEnSesion != null) {
                System.out.println("Abriendo edición para usuario: " + usuarioEnSesion.getNombre());

                UsuarioDao dao = new UsuarioDao();
                Usuario usuarioCompleto = dao.obtenerUsuarioPorId(usuarioEnSesion.getIdUsuario());

                if (usuarioCompleto != null) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("EditarPerfil.fxml"));
                    Parent root = loader.load();
                    EditarPerfilController controller = loader.getController();
                    controller.setUsuario(usuarioCompleto);
                    controller.setOnUsuarioActualizado(() -> {
                        // Recargar datos del usuario después de editar
                        cargarUsuarioDesdeSesion();
                    });
                    Stage stage = new Stage();
                    stage.setTitle("ElectroStock - Editar Perfil");
                    stage.setScene(new Scene(root));
                    stage.show();
                } else {
                    mostrarAlerta("Error", "No se pudieron cargar los datos del usuario", Alert.AlertType.ERROR);
                }
            } else {
                mostrarAlerta("Error", "No se pudo obtener la información del usuario en sesión", Alert.AlertType.WARNING);
            }
        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo abrir la edición del perfil", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}