package mx.edu.utez.fantasticoctoguacamole;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import mx.edu.utez.fantasticoctoguacamole.modelo.SesionUsuario;

import java.io.IOException;

public class AdminDashboardController {

    @FXML
    private Rectangle tarjeta; //Gestion de usuarios
    @FXML
    private Rectangle tarjeta2; //Historial de transacciones
    @FXML
    private Label cerrarSesion;

    @FXML
    public void initialize() {
        //Hacer las tarjetas clickeables
        configurarTarjetasClickeables();
    }

    private void configurarTarjetasClickeables() {
        //Tarjeta 1 - Gestion de usuarios
        tarjeta.setOnMouseClicked(this::manejarClickGestionUsuarios);
        //Tarjeta 2 - Historial de transacciones
        tarjeta2.setOnMouseClicked(this::manejarClickHistorialTransacciones);
        //Cerrar sesion
        cerrarSesion.setOnMouseClicked(this::manejarCerrarSesion);
    }

    @FXML
    private void manejarClickGestionUsuarios(MouseEvent event) {
        cargarPantalla("GestionUsuarios.fxml", "Gestión de Usuarios");
    }

    @FXML
    private void manejarClickHistorialTransacciones(MouseEvent event) {
        System.out.println("Navegando a Historial de Transacciones...");
        //Mostrar mensaje de que la funcionalidad está en desarrollo
        mostrarMensajeDesarrollo("Historial de Transacciones");
        //cargarPantalla("HistorialTransacciones.fxml", "Historial de Transacciones");
    }

    @FXML
    private void manejarCerrarSesion(MouseEvent event) {
        try {
            //Confirmar cierre de sesion
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

    private void cargarPantalla(String fxmlFile, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("ElectroStock - " + titulo);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            mostrarError("Error al cargar " + titulo + ": " + e.getMessage());
        }
    }

    private void mostrarMensajeDesarrollo(String funcionalidad) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("En Desarrollo");
        alert.setHeaderText(funcionalidad);
        alert.setContentText("Esta funcionalidad está actualmente en desarrollo.\n¡Próximamente disponible!");
        alert.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}