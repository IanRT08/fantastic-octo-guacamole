package mx.edu.utez.fantasticoctoguacamole;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import mx.edu.utez.fantasticoctoguacamole.modelo.dao.ProductoDao;
import mx.edu.utez.fantasticoctoguacamole.modelo.Producto;

public class RegistrarProductoController {

    @FXML
    private TextField nombre;
    @FXML
    private TextField codigo;
    @FXML
    private TextArea descripcion;
    @FXML
    private TextField precio;
    @FXML
    private TextField stock;
    @FXML
    private Button agregarBoton;
    @FXML
    private Button regresarBoton;


    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    public void agregarProducto(ActionEvent event) {
        //Recortar espacios sobrantes
        nombre.setText(nombre.getText().trim());
        codigo.setText(codigo.getText().trim());
        descripcion.setText(descripcion.getText().trim());
        precio.setText(precio.getText().trim());
        stock.setText(stock.getText().trim());
        //Validar campos obligatorios
        if (nombre.getText().isEmpty() ||
                codigo.getText().isEmpty() ||
                precio.getText().isEmpty() ||
                stock.getText().isEmpty()) {
            mostrarAlerta("Error!", "Todos los campos marcados con * son obligatorios", Alert.AlertType.ERROR);
            return;
        }
        //Validar formato del codigo
        if (!codigo.getText().matches("^[A-Za-z0-9-]+$")) {
            mostrarAlerta("Error!", "El código solo puede contener letras, números y guiones", Alert.AlertType.ERROR);
            return;
        }
        //Validar que el precio sea un numero valido y positivo
        double precioValue;
        try {
            precioValue = Double.parseDouble(precio.getText());
            if (precioValue <= 0) {
                mostrarAlerta("Error!", "El precio debe ser mayor a 0", Alert.AlertType.ERROR);
                return;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error!", "El precio debe ser un número válido", Alert.AlertType.ERROR);
            return;
        }
        //Validar que el stock sea un numero entero valido y no negativo
        int stockValue;
        try {
            stockValue = Integer.parseInt(stock.getText());
            if (stockValue < 0) {
                mostrarAlerta("Error!", "El stock no puede ser negativo", Alert.AlertType.ERROR);
                return;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error!", "El stock debe ser un número entero válido", Alert.AlertType.ERROR);
            return;
        }
        //Validar longitud del codigo
        if (codigo.getText().length() > 15) {
            mostrarAlerta("Error!", "El código no puede tener más de 15 caracteres", Alert.AlertType.ERROR);
            return;
        }
        //Validar longitud del nombre
        if (nombre.getText().length() > 50) {
            mostrarAlerta("Error!", "El nombre no puede tener más de 50 caracteres", Alert.AlertType.ERROR);
            return;
        }
        //Validar longitud de la descripción
        if (descripcion.getText().length() > 255) {
            mostrarAlerta("Error!", "La descripción no puede tener más de 255 caracteres", Alert.AlertType.ERROR);
            return;
        }
        try {
            //Obtener el proximo ID
            ProductoDao dao = new ProductoDao();
            int proximoId = dao.obtenerProximoId();
            //Verificar si el codigo ya existe
            if (dao.existeCodigo(codigo.getText())) {
                mostrarAlerta("Error!", "El código del producto ya está en uso", Alert.AlertType.ERROR);
                return;
            }
            //Crear objeto Producto
            Producto productoObj = new Producto(
                    proximoId,
                    codigo.getText(),
                    nombre.getText(),
                    descripcion.getText(),
                    precioValue,
                    stockValue
            );
            //Establecer estado activo por defecto
            productoObj.setEstado(true);
            //Intentar guardar en la base de datos
            if (dao.createProducto(productoObj)) {
                //Limpiar campos
                limpiarCampos();
                mostrarAlerta("Éxito", "Producto registrado correctamente", Alert.AlertType.INFORMATION);
            } else {
                mostrarAlerta("Error!", "No se pudo registrar el producto", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            mostrarAlerta("Error!", "Ocurrió un error inesperado: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void limpiarCampos() {
        nombre.clear();
        codigo.clear();
        descripcion.clear();
        precio.clear();
        stock.clear();
    }

    @FXML
    void regresarMenu(ActionEvent event) {
        Stage currentStage = (Stage) regresarBoton.getScene().getWindow();
        currentStage.close();
    }
}