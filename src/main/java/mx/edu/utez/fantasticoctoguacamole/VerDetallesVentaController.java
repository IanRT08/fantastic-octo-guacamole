package mx.edu.utez.fantasticoctoguacamole;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import mx.edu.utez.fantasticoctoguacamole.modelo.Venta;
import mx.edu.utez.fantasticoctoguacamole.modelo.DetalleVenta;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

public class VerDetallesVentaController implements Initializable {

    @FXML
    private Label labelFolio;
    @FXML
    private Label labelFecha;
    @FXML
    private Label labelTotal;
    @FXML
    private Label labelItems;
    @FXML
    private TableView<DetalleVenta> tablaDetalles;
    @FXML
    private TableColumn<DetalleVenta, String> columnaProducto;
    @FXML
    private TableColumn<DetalleVenta, String> columnaCodigo;
    @FXML
    private TableColumn<DetalleVenta, Integer> columnaCantidad;
    @FXML
    private TableColumn<DetalleVenta, Double> columnaPrecio;
    @FXML
    private TableColumn<DetalleVenta, Double> columnaSubtotal;

    private Venta venta;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarTablaDetalles();
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
        mostrarDatosVenta();
    }

    private void configurarTablaDetalles() {
        columnaProducto.setCellValueFactory(cellData -> {
            if (cellData.getValue().getProducto() != null) {
                return javafx.beans.binding.Bindings.createStringBinding(
                        () -> cellData.getValue().getProducto().getNombre()
                );
            }
            return javafx.beans.binding.Bindings.createStringBinding(() -> "N/A");
        });
        columnaCodigo.setCellValueFactory(cellData -> {
            if (cellData.getValue().getProducto() != null) {
                return javafx.beans.binding.Bindings.createStringBinding(
                        () -> cellData.getValue().getProducto().getCodigo()
                );
            }
            return javafx.beans.binding.Bindings.createStringBinding(() -> "N/A");
        });
        columnaCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        columnaPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        columnaPrecio.setCellFactory(column -> new javafx.scene.control.TableCell<DetalleVenta, Double>() {
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
        columnaSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        columnaSubtotal.setCellFactory(column -> new javafx.scene.control.TableCell<DetalleVenta, Double>() {
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
    }

    private void mostrarDatosVenta() {
        if (venta != null) {
            //Mostrar información general
            labelFolio.setText("#" + venta.getIdVenta());
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            labelFecha.setText(dateFormat.format(venta.getFecha()));
            labelTotal.setText(String.format("$%.2f", venta.getTotal()));
            if (venta.getDetalles() != null) {
                int totalItems = venta.getDetalles().stream()
                        .mapToInt(DetalleVenta::getCantidad)
                        .sum();
                labelItems.setText(String.valueOf(totalItems));
                //Cargar detalles en la tabla
                tablaDetalles.getItems().setAll(venta.getDetalles());
            }
        }
    }

    @FXML
    private void cerrarVentana() {
        Stage stage = (Stage) labelFolio.getScene().getWindow();
        stage.close();
    }
}