package mx.edu.utez.fantasticoctoguacamole;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import mx.edu.utez.fantasticoctoguacamole.modelo.*;
import mx.edu.utez.fantasticoctoguacamole.modelo.dao.CambioDao;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class ConfirmarCambioController implements Initializable {

    @FXML
    private Label labelFolio;
    @FXML
    private Label labelFecha;
    @FXML
    private Label labelTotalOrigen;
    @FXML
    private Label labelTotalNuevo;
    @FXML
    private Label labelDiferencia;
    @FXML
    private TextArea textAreaMotivo;
    @FXML
    private Button btnConfirmar;
    @FXML
    private Button btnCancelar;

    private Venta venta;
    private List<DetalleVentaSeleccionable> productosOrigen;
    private List<BuscarProductosNuevosController.ProductoSeleccionado> productosNuevos;
    private CambioDao cambioDao;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cambioDao = new CambioDao();
    }

    public void setDatosCambio(Venta venta,
                               List<DetalleVentaSeleccionable> productosOrigen,
                               List<BuscarProductosNuevosController.ProductoSeleccionado> productosNuevos) {
        this.venta = venta;
        this.productosOrigen = productosOrigen;
        this.productosNuevos = productosNuevos;
        mostrarDatos();
        calcularDiferencias();
    }

    private void mostrarDatos() {
        if (venta != null) {
            labelFolio.setText("#" + venta.getIdVenta());
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            labelFecha.setText(dateFormat.format(venta.getFecha()));
        }
    }

    private void calcularDiferencias() {
        double totalOrigen = 0;
        double totalNuevo = 0;
        //Calcular total de productos a cambiar
        for (DetalleVentaSeleccionable detalle : productosOrigen) {
            double precioUnitario = detalle.getPrecioUnitario();
            totalOrigen += precioUnitario * detalle.getCantidadDevolver();
        }
        //Calcular total de productos nuevos
        for (BuscarProductosNuevosController.ProductoSeleccionado producto : productosNuevos) {
            totalNuevo += producto.getSubtotal();
        }
        double diferencia = totalNuevo - totalOrigen;
        labelTotalOrigen.setText(String.format("$%.2f", totalOrigen));
        labelTotalNuevo.setText(String.format("$%.2f", totalNuevo));
        labelDiferencia.setText(String.format("$%.2f", diferencia));
        //Colorear la diferencia
        if (diferencia > 0) {
            labelDiferencia.setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
        } else if (diferencia < 0) {
            labelDiferencia.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
        } else {
            labelDiferencia.setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void confirmar() {
        if (textAreaMotivo.getText().trim().isEmpty()) {
            mostrarAlerta("Error", "Ingrese el motivo del cambio", Alert.AlertType.ERROR);
            return;
        }
        //Mostrar resumen de confirmacipn
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("¿Confirmar el siguiente cambio?\n\n");
        mensaje.append("PRODUCTOS A CAMBIAR:\n");
        for (DetalleVentaSeleccionable detalle : productosOrigen) {
            mensaje.append(String.format("- %s: %d unidades x $%.2f = $%.2f\n",
                    detalle.getProducto().getNombre(),
                    detalle.getCantidadDevolver(),
                    detalle.getPrecioUnitario(),
                    detalle.getPrecioUnitario() * detalle.getCantidadDevolver()));
        }
        mensaje.append("\nPRODUCTOS NUEVOS:\n");
        for (BuscarProductosNuevosController.ProductoSeleccionado producto : productosNuevos) {
            mensaje.append(String.format("- %s: %d unidades x $%.2f = $%.2f\n",
                    producto.getProducto().getNombre(),
                    producto.getCantidad(),
                    producto.getProducto().getPrecio(),
                    producto.getSubtotal()));
        }
        double totalOrigen = Double.parseDouble(labelTotalOrigen.getText().replace("$", ""));
        double totalNuevo = Double.parseDouble(labelTotalNuevo.getText().replace("$", ""));
        double diferencia = Double.parseDouble(labelDiferencia.getText().replace("$", ""));
        mensaje.append(String.format("\nRESUMEN:\n"));
        mensaje.append(String.format("Total productos a cambiar: $%.2f\n", totalOrigen));
        mensaje.append(String.format("Total productos nuevos: $%.2f\n", totalNuevo));
        mensaje.append(String.format("Diferencia: $%.2f\n", diferencia));
        if (diferencia > 0) {
            mensaje.append("El cliente debe pagar: $").append(String.format("%.2f", diferencia));
        } else if (diferencia < 0) {
            mensaje.append("Se reembolsará al cliente: $").append(String.format("%.2f", Math.abs(diferencia)));
        } else {
            mensaje.append("Sin transacción adicional");
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Cambio");
        confirmacion.setHeaderText("¿Está seguro de proceder con el cambio?");
        confirmacion.setContentText(mensaje.toString());
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            procesarCambio();
        }
    }

    private void procesarCambio() {
        boolean exito = true;
        String motivo = textAreaMotivo.getText().trim();
        //Para cada producto a cambiar, crear un registro en la tabla Cambios
        for (DetalleVentaSeleccionable productoOrigen : productosOrigen) {
            //Asignar productos nuevos a productos viejos
            //Por simplicidad, tomamos el primer producto nuevo
            if (!productosNuevos.isEmpty()) {
                BuscarProductosNuevosController.ProductoSeleccionado productoNuevo = productosNuevos.get(0);
                Cambio cambio = new Cambio();
                cambio.setIdDetalleVentaOrigen(productoOrigen.getIdDetalle());
                cambio.setIdProductoNuevo(productoNuevo.getProducto().getIdProducto());
                cambio.setFechaCambio(new Date());
                cambio.setCantidadCambiada(productoOrigen.getCantidadDevolver());
                cambio.setPrecioUnitarioOrigen(productoOrigen.getPrecioUnitario());
                cambio.setPrecioUnitarioNuevo(productoNuevo.getProducto().getPrecio());
                // Calcular diferencia
                double diferencia = (productoNuevo.getProducto().getPrecio() * productoOrigen.getCantidadDevolver()) -
                        (productoOrigen.getPrecioUnitario() * productoOrigen.getCantidadDevolver());
                cambio.setDiferenciaPago(diferencia);
                cambio.setMotivo(motivo);
                if (!cambioDao.registrarCambio(cambio)) {
                    exito = false;
                    break;
                }
            }
        }
        if (exito) {
            mostrarAlerta("Éxito", "Cambio procesado correctamente", Alert.AlertType.INFORMATION);
            cerrarVentana();
        } else {
            mostrarAlerta("Error", "Error al procesar el cambio", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void cancelar() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}