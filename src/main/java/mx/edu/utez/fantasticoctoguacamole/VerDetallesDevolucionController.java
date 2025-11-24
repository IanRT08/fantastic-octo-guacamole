package mx.edu.utez.fantasticoctoguacamole;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import mx.edu.utez.fantasticoctoguacamole.modelo.*;
import mx.edu.utez.fantasticoctoguacamole.modelo.dao.*;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

public class VerDetallesDevolucionController implements Initializable {

    @FXML
    private Label labelFolio;
    @FXML
    private Label labelFecha;
    @FXML
    private Label labelCajero;
    @FXML
    private Label labelMontoReembolsado;
    @FXML
    private Label labelProducto;
    @FXML
    private Label labelCantidad;
    @FXML
    private Label labelMotivo;
    @FXML
    private TableView<DetalleVenta> tablaDetallesOrigen;
    @FXML
    private TableColumn<DetalleVenta, String> columnaProductoOrigen;
    @FXML
    private TableColumn<DetalleVenta, Integer> columnaCantidadOrigen;
    @FXML
    private TableColumn<DetalleVenta, Double> columnaPrecioOrigen;
    @FXML
    private TableColumn<DetalleVenta, Double> columnaSubtotalOrigen;

    private Devolucion devolucion;
    private VentaDao ventaDao;
    private ProductoDao productoDao;
    private UsuarioDao usuarioDao;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ventaDao = new VentaDao();
        productoDao = new ProductoDao();
        usuarioDao = new UsuarioDao();
        configurarTablaDetalles();
    }

    public void setDevolucion(Devolucion devolucion) {
        this.devolucion = devolucion;
        mostrarDatosDevolucion();
        cargarDetallesOrigen();
    }

    private void configurarTablaDetalles() {
        // Misma configuración que en VerDetallesCambioController
        columnaProductoOrigen.setCellValueFactory(cellData -> {
            if (cellData.getValue().getProducto() != null) {
                return javafx.beans.binding.Bindings.createStringBinding(
                        () -> cellData.getValue().getProducto().getNombre()
                );
            }
            return javafx.beans.binding.Bindings.createStringBinding(() -> "N/A");
        });
        columnaCantidadOrigen.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        columnaPrecioOrigen.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        columnaPrecioOrigen.setCellFactory(column -> new javafx.scene.control.TableCell<DetalleVenta, Double>() {
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
        columnaSubtotalOrigen.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        columnaSubtotalOrigen.setCellFactory(column -> new javafx.scene.control.TableCell<DetalleVenta, Double>() {
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

    private void mostrarDatosDevolucion() {
        if (devolucion != null) {
            labelFolio.setText("#" + devolucion.getIdDevolucion());
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            labelFecha.setText(dateFormat.format(devolucion.getFechaDevolucion()));
            labelCantidad.setText(String.valueOf(devolucion.getCantidadDevuelta()));
            labelMontoReembolsado.setText(String.format("$%.2f", devolucion.getMontoReembolsado()));
            labelMotivo.setText(devolucion.getMotivo() != null ? devolucion.getMotivo() : "No especificado");
            //Obtener informacion adicional
            try {
                //Producto devuelto
                DetalleVenta detalleOrigen = obtenerDetalleVenta(devolucion.getIdDetalleVenta());
                if (detalleOrigen != null && detalleOrigen.getProducto() != null) {
                    labelProducto.setText(detalleOrigen.getProducto().getNombre());
                } else {
                    labelProducto.setText("N/A");
                }
                //Cajero
                Venta ventaOrigen = obtenerVentaDesdeDetalle(devolucion.getIdDetalleVenta());
                if (ventaOrigen != null) {
                    Usuario usuario = usuarioDao.obtenerUsuarioPorId(ventaOrigen.getIdUsuario());
                    if (usuario != null) {
                        labelCajero.setText(usuario.getNombre() + " " +
                                (usuario.getApellidoPaterno() != null ? usuario.getApellidoPaterno() : ""));
                    } else {
                        labelCajero.setText("N/A");
                    }
                }

            } catch (Exception e) {
                System.err.println("Error al cargar detalles de la devolución: " + e.getMessage());
            }
        }
    }

    private void cargarDetallesOrigen() {
        if (devolucion != null) {
            try {
                DetalleVenta detalleOrigen = obtenerDetalleVenta(devolucion.getIdDetalleVenta());
                if (detalleOrigen != null) {
                    tablaDetallesOrigen.getItems().add(detalleOrigen);
                }
            } catch (Exception e) {
                System.err.println("Error al cargar detalles origen: " + e.getMessage());
            }
        }
    }

    private DetalleVenta obtenerDetalleVenta(int idDetalle) {
        return ventaDao.obtenerDetalleVentaPorId(idDetalle);
    }

    private Venta obtenerVentaDesdeDetalle(int idDetalle) {
        return ventaDao.obtenerVentaDesdeDetalle(idDetalle);
    }

    @FXML
    private void cerrarVentana() {
        Stage stage = (Stage) labelFolio.getScene().getWindow();
        stage.close();
    }
}
