package mx.edu.utez.fantasticoctoguacamole;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import mx.edu.utez.fantasticoctoguacamole.modelo.Producto;

import java.net.URL;
import java.util.ResourceBundle;

public class VerProductosController implements Initializable {

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
    private Button volverBoton;

    private Producto productoObj;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Configurar el boton volver
        volverBoton.setOnAction(event -> volverMenu());
    }

    public void setProducto(Producto producto) {
        this.productoObj = producto;
        cargarDatosProducto();
    }

    private void cargarDatosProducto() {
        if (productoObj != null) {
            // Cargar datos básicos
            nombre.setText(productoObj.getNombre());
            producto.setText(productoObj.getCodigo());
            //Cargar descripcion
            if (productoObj.getDescripcion() != null) {
                descripcion.setText(productoObj.getDescripcion());
            } else {
                descripcion.setText("");
            }
            //Cargar precio formateado
            precio.setText(String.format("$%.2f", productoObj.getPrecio()));
            //Cargar stock
            stock.setText(String.valueOf(productoObj.getStock()));
            System.out.println("Producto cargado: " + productoObj.getNombre());
            System.out.println("Código: " + productoObj.getCodigo());
            System.out.println("Precio: " + productoObj.getPrecio());
            System.out.println("Stock: " + productoObj.getStock());
        } else {
            System.out.println("Producto es null en cargarDatosProducto");
        }
    }

    @FXML
    private void volverMenu() {
        Stage stage = (Stage) volverBoton.getScene().getWindow();
        stage.close();
    }
}