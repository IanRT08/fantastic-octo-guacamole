package mx.edu.utez.fantasticoctoguacamole;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import mx.edu.utez.fantasticoctoguacamole.modelo.VentaCancelada;
import mx.edu.utez.fantasticoctoguacamole.modelo.DetalleVenta;
import mx.edu.utez.fantasticoctoguacamole.modelo.dao.ProductoDao;
import mx.edu.utez.fantasticoctoguacamole.modelo.Producto;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

public class VerDetallesCancelacionController implements Initializable {

    @FXML
    private Label lblFolioOriginal;
    @FXML
    private Label lblFechaVenta;
    @FXML
    private Label lblFechaCancelacion;
    @FXML
    private Label lblTotalVenta;
    @FXML
    private Label lblVendedor;
    @FXML
    private Label lblUsuarioCancelacion;
    @FXML
    private Label lblResumen;
    @FXML
    private TextArea txtMotivo;
    @FXML
    private TableView<DetalleVenta> tablaProductos;
    @FXML
    private TableColumn<DetalleVenta, String> colProducto;
    @FXML
    private TableColumn<DetalleVenta, Integer> colCantidad;
    @FXML
    private TableColumn<DetalleVenta, Double> colPrecioUnitario;
    @FXML
    private TableColumn<DetalleVenta, Double> colSubtotal;
    @FXML
    private Button btnCerrar;

    private VentaCancelada ventaCancelada;
    private ProductoDao productoDao;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        productoDao = new ProductoDao();
        configurarTabla();
    }

    private void configurarTabla() {
        // Configurar columnas
        colProducto.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecioUnitario.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        // Formatear precios
        colPrecioUnitario.setCellFactory(column -> new TableCell<DetalleVenta, Double>() {
            @Override
            protected void updateItem(Double precio, boolean empty) {
                super.updateItem(precio, empty);
                if (empty || precio == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", precio));
                }
            }
        });

        colSubtotal.setCellFactory(column -> new TableCell<DetalleVenta, Double>() {
            @Override
            protected void updateItem(Double subtotal, boolean empty) {
                super.updateItem(subtotal, empty);
                if (empty || subtotal == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", subtotal));
                }
            }
        });

        // Configurar columna de cantidad
        colCantidad.setCellFactory(column -> new TableCell<DetalleVenta, Integer>() {
            @Override
            protected void updateItem(Integer cantidad, boolean empty) {
                super.updateItem(cantidad, empty);
                if (empty || cantidad == null) {
                    setText(null);
                } else {
                    setText(String.valueOf(cantidad));
                }
            }
        });
    }

    public void setVentaCancelada(VentaCancelada ventaCancelada) {
        this.ventaCancelada = ventaCancelada;
        cargarDatos();
    }

    private void cargarDatos() {
        if (ventaCancelada != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            // Información básica
            lblFolioOriginal.setText(String.valueOf(ventaCancelada.getIdVentaOriginal()));
            lblFechaVenta.setText(dateFormat.format(ventaCancelada.getFechaVentaOriginal()));
            lblFechaCancelacion.setText(dateFormat.format(ventaCancelada.getFechaCancelacion()));
            lblTotalVenta.setText(String.format("$%.2f", ventaCancelada.getTotalVenta()));

            // Información de usuarios
            lblVendedor.setText(ventaCancelada.getNombreUsuarioVenta() != null ?
                    ventaCancelada.getNombreUsuarioVenta() : "Usuario ID: " + ventaCancelada.getIdUsuarioVenta());
            lblUsuarioCancelacion.setText(ventaCancelada.getNombreUsuarioCancelacion() != null ?
                    ventaCancelada.getNombreUsuarioCancelacion() : "Usuario ID: " + ventaCancelada.getIdUsuarioCancelacion());

            // Motivo
            txtMotivo.setText(ventaCancelada.getMotivoCancelacion());

            // Cargar productos en la tabla
            cargarProductos();

            // Resumen
            lblResumen.setText(String.format("Stock restaurado - %d productos afectados",
                    ventaCancelada.getDetalles() != null ? ventaCancelada.getDetalles().size() : 0));
        }
    }

    private void cargarProductos() {
        if (ventaCancelada.getDetalles() != null) {
            // Para cada detalle, obtener el nombre del producto
            for (DetalleVenta detalle : ventaCancelada.getDetalles()) {
                if (detalle.getIdProducto() > 0) {
                    Producto producto = productoDao.obtenerProductoPorId(detalle.getIdProducto());
                    if (producto != null) {
                        detalle.setNombreProducto(producto.getNombre());
                    } else {
                        detalle.setNombreProducto("Producto no encontrado");
                    }
                } else {
                    detalle.setNombreProducto("N/A");
                }
            }

            // Cargar en la tabla
            tablaProductos.getItems().setAll(ventaCancelada.getDetalles());
        }
    }

    @FXML
    private void cerrarVentana() {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }
}