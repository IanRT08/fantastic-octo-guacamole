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

public class VerDetallesCambioController implements Initializable {

    @FXML
    private Label labelFolio;
    @FXML
    private Label labelFecha;
    @FXML
    private Label labelCajero;
    @FXML
    private Label labelDiferencia;
    @FXML
    private Label labelProductoOrigen;
    @FXML
    private Label labelProductoNuevo;
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

    private Cambio cambio;
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


    public void setCambio(Cambio cambio) {
        this.cambio = cambio;
        mostrarDatosCambio();
        cargarDetallesOrigen();
    }

    private void configurarTablaDetalles() {
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

    private void mostrarDatosCambio() {
        if (cambio != null) {
            labelFolio.setText("#" + cambio.getIdCambio());
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            labelFecha.setText(dateFormat.format(cambio.getFechaCambio()));
            labelCantidad.setText(String.valueOf(cambio.getCantidadCambiada()));
            labelDiferencia.setText(String.format("$%.2f", cambio.getDiferenciaPago()));
            labelMotivo.setText(cambio.getMotivo() != null ? cambio.getMotivo() : "No especificado");
            //Obtener informacion de productos
            try {
                //Producto origen
                DetalleVenta detalleOrigen = obtenerDetalleVenta(cambio.getIdDetalleVentaOrigen());
                if (detalleOrigen != null && detalleOrigen.getProducto() != null) {
                    labelProductoOrigen.setText(detalleOrigen.getProducto().getNombre());
                } else {
                    labelProductoOrigen.setText("N/A");
                }
                //Producto nuevo
                Producto productoNuevo = productoDao.obtenerProductoPorId(cambio.getIdProductoNuevo());
                if (productoNuevo != null) {
                    labelProductoNuevo.setText(productoNuevo.getNombre());
                } else {
                    labelProductoNuevo.setText("N/A");
                }
                //Cajero
                Venta ventaOrigen = obtenerVentaDesdeDetalle(cambio.getIdDetalleVentaOrigen());
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
                System.err.println("Error al cargar detalles del cambio: " + e.getMessage());
            }
        }
    }

    private void cargarDetallesOrigen() {
        if (cambio != null) {
            try {
                DetalleVenta detalleOrigen = obtenerDetalleVenta(cambio.getIdDetalleVentaOrigen());
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
