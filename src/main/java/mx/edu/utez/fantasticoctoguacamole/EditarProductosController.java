package mx.edu.utez.fantasticoctoguacamole;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import mx.edu.utez.fantasticoctoguacamole.modelo.dao.ProductoDao;
import mx.edu.utez.fantasticoctoguacamole.modelo.Producto;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class EditarProductosController implements Initializable {
    @FXML
    private TextField nombre;
    @FXML
    private TextField producto;
    @FXML
    private TextArea descripcion;
    @FXML
    private TextField precio;
    @FXML
    private TextField stock;
    @FXML
    private Button editarBoton;
    @FXML
    private Button eliminarBoton;
    @FXML
    private Button regresarBoton;

    private Producto productoObj;
    private Runnable onProductoActualizado;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Configurar botones
        editarBoton.setOnAction(event -> editarProducto());
        eliminarBoton.setOnAction(event -> borrarProducto());
        regresarBoton.setOnAction(event -> regresarMenu());
    }

    public void setProducto(Producto producto) {
        this.productoObj = producto;
        if (producto != null) {
            System.out.println("Producto recibido: " + producto.getNombre());
            cargarDatosProducto();
        } else {
            System.out.println("Producto es NULL!");
        }
    }

    private void cargarDatosProducto() {
        try {
            if (productoObj != null) {
                //Cargar datos en los campos
                nombre.setText(productoObj.getNombre());
                producto.setText(productoObj.getCodigo());
                descripcion.setText(productoObj.getDescripcion());
                precio.setText(String.valueOf(productoObj.getPrecio()));
                stock.setText(String.valueOf(productoObj.getStock()));
            } else {
                System.out.println("Producto es null en cargarDatosProducto");
            }
        } catch (Exception e) {
            System.out.println("Error en cargarDatosProducto: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void editarProducto() {
        //Recortar espacios sobrantes
        nombre.setText(nombre.getText().trim());
        producto.setText(producto.getText().trim());
        descripcion.setText(descripcion.getText().trim());
        precio.setText(precio.getText().trim());
        stock.setText(stock.getText().trim());
        //Validar campos obligatorios
        if (nombre.getText().isEmpty() ||
                producto.getText().isEmpty() ||
                precio.getText().isEmpty() ||
                stock.getText().isEmpty()) {
            mostrarAlerta("Error!", "Todos los campos marcados con * son obligatorios", Alert.AlertType.ERROR);
            return;
        }
        //Validar formato del código (solo letras, números y guiones)
        if (!producto.getText().matches("^[A-Za-z0-9-]+$")) {
            mostrarAlerta("Error!", "El código solo puede contener letras, números y guiones", Alert.AlertType.ERROR);
            return;
        }
        //Validar que el precio sea un número valido y positivo
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
        //Validar que el stock sea un numero entero válido y no negativo
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
        if (producto.getText().length() > 15) {
            mostrarAlerta("Error!", "El código no puede tener más de 15 caracteres", Alert.AlertType.ERROR);
            return;
        }
        //Validar longitud del nombre
        if (nombre.getText().length() > 50) {
            mostrarAlerta("Error!", "El nombre no puede tener más de 50 caracteres", Alert.AlertType.ERROR);
            return;
        }
        //Validar longitud de la descripcion
        if (descripcion.getText().length() > 255) {
            mostrarAlerta("Error!", "La descripción no puede tener más de 255 caracteres", Alert.AlertType.ERROR);
            return;
        }
        try {
            //Verificar si el codigo ya existe
            ProductoDao dao = new ProductoDao();
            if (!productoObj.getCodigo().equals(producto.getText()) && dao.existeCodigo(producto.getText())) {
                mostrarAlerta("Error!", "El código del producto ya está en uso", Alert.AlertType.ERROR);
                return;
            }
            //Actualizar datos del producto
            productoObj.setNombre(nombre.getText());
            productoObj.setCodigo(producto.getText());
            productoObj.setDescripcion(descripcion.getText());
            productoObj.setPrecio(precioValue);
            productoObj.setStock(stockValue);
            //Guardar en la base de datos
            if (dao.updateProducto(productoObj)) {
                mostrarAlerta("Éxito", "Producto actualizado correctamente", Alert.AlertType.INFORMATION);
                if (onProductoActualizado != null) {
                    onProductoActualizado.run();
                }
                Stage stage = (Stage) editarBoton.getScene().getWindow();
                stage.close();
            } else {
                mostrarAlerta("Error", "No se pudo actualizar el producto", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al actualizar: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void borrarProducto() {
        if (confirmDelete()) {
            try {
                ProductoDao dao = new ProductoDao();
                if (dao.deleteProducto(productoObj.getIdProducto())) {
                    mostrarAlerta("Éxito", "Producto eliminado correctamente", Alert.AlertType.INFORMATION);
                    if (onProductoActualizado != null) {
                        onProductoActualizado.run();
                    }
                    Stage stage = (Stage) eliminarBoton.getScene().getWindow();
                    stage.close();
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar el producto", Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                mostrarAlerta("Error", "Error al eliminar: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    public void setOnProductoActualizado(Runnable callback) {
        this.onProductoActualizado = callback;
    }

    private boolean confirmDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación de Eliminación");
        alert.setHeaderText("¿Estás seguro de eliminar este producto?");
        alert.setContentText("Esta acción no se puede deshacer. Se eliminarán todos los datos del producto: " + productoObj.getNombre());
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    @FXML
    private void regresarMenu() {
        // Simplemente cerrar esta ventana
        Stage currentStage = (Stage) regresarBoton.getScene().getWindow();
        currentStage.close();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}