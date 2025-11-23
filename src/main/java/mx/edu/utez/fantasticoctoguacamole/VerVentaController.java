package mx.edu.utez.fantasticoctoguacamole;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;
import mx.edu.utez.fantasticoctoguacamole.modelo.Venta;
import mx.edu.utez.fantasticoctoguacamole.modelo.DetalleVenta;
import mx.edu.utez.fantasticoctoguacamole.modelo.SesionUsuario;
import mx.edu.utez.fantasticoctoguacamole.modelo.dao.VentaDao;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class VerVentaController implements Initializable {

    @FXML
    private TableView<Venta> tablaVentas;
    @FXML
    private TableColumn<Venta, Integer> tablaFolio;
    @FXML
    private TableColumn<Venta, Date> tablaFecha;
    @FXML
    private TableColumn<Venta, Integer> tablaItems;
    @FXML
    private TableColumn<Venta, Double> tablaTotal;
    @FXML
    private TableColumn<Venta, String> tablaDetalles;
    @FXML
    private TextField buscador;
    @FXML
    private Label totalVentas;
    @FXML
    private Button botonVolver;

    private ObservableList<Venta> listaVentas;
    private FilteredList<Venta> datosFiltrados;
    private SortedList<Venta> datosOrdenados;
    private VentaDao ventaDao;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ventaDao = new VentaDao();
        configurarColumnas();
        cargarVentas();
        configurarBusqueda();
        actualizarTotalVentas();
    }

    private void configurarColumnas() {
        tablaFolio.setCellValueFactory(new PropertyValueFactory<>("idVenta"));
        tablaFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        tablaFecha.setCellFactory(new Callback<TableColumn<Venta, Date>, TableCell<Venta, Date>>() {
            @Override
            public TableCell<Venta, Date> call(TableColumn<Venta, Date> param) {
                return new TableCell<Venta, Date>() {
                    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    @Override
                    protected void updateItem(Date fecha, boolean empty) {
                        super.updateItem(fecha, empty);
                        if (empty || fecha == null) {
                            setText(null);
                        } else {
                            setText(dateFormat.format(fecha));
                        }
                    }
                };
            }
        });
        tablaItems.setCellFactory(new Callback<TableColumn<Venta, Integer>, TableCell<Venta, Integer>>() {
            @Override
            public TableCell<Venta, Integer> call(TableColumn<Venta, Integer> param) {
                return new TableCell<Venta, Integer>() {
                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            Venta venta = getTableView().getItems().get(getIndex());
                            if (venta.getDetalles() != null) {
                                int totalItems = venta.getDetalles().stream()
                                        .mapToInt(DetalleVenta::getCantidad)
                                        .sum();
                                setText(String.valueOf(totalItems));
                            } else {
                                setText("0");
                            }
                        }
                    }
                };
            }
        });
        tablaTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        tablaTotal.setCellFactory(new Callback<TableColumn<Venta, Double>, TableCell<Venta, Double>>() {
            @Override
            public TableCell<Venta, Double> call(TableColumn<Venta, Double> param) {
                return new TableCell<Venta, Double>() {
                    @Override
                    protected void updateItem(Double total, boolean empty) {
                        super.updateItem(total, empty);
                        if (empty || total == null) {
                            setText(null);
                        } else {
                            setText(String.format("$%.2f", total));
                        }
                    }
                };
            }
        });
        tablaDetalles.setCellFactory(new Callback<TableColumn<Venta, String>, TableCell<Venta, String>>() {
            @Override
            public TableCell<Venta, String> call(TableColumn<Venta, String> param) {
                return new TableCell<Venta, String>() {
                    private final Button btnVerDetalles = new Button("Ver más");
                    {
                        btnVerDetalles.setOnAction(event -> {
                            Venta venta = getTableView().getItems().get(getIndex());
                            verDetallesVenta(venta);
                        });
                    }
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btnVerDetalles);
                        }
                    }
                };
            }
        });
    }

    private void cargarVentas() {
        int idUsuario = SesionUsuario.getIdUsuario();
        List<Venta> ventas = ventaDao.obtenerVentasPorUsuario(idUsuario);
        //Cargar detalles para cada venta
        for (Venta venta : ventas) {
            List<DetalleVenta> detalles = ventaDao.obtenerDetallesVenta(venta.getIdVenta());
            venta.setDetalles(detalles);
        }
        listaVentas = FXCollections.observableArrayList(ventas);
        datosFiltrados = new FilteredList<>(listaVentas, p -> true);
        datosOrdenados = new SortedList<>(datosFiltrados);
        datosOrdenados.comparatorProperty().bind(tablaVentas.comparatorProperty());
        tablaVentas.setItems(datosOrdenados);
    }

    private void configurarBusqueda() {
        buscador.textProperty().addListener((observable, oldValue, nuevoTexto) -> {
            datosFiltrados.setPredicate(venta -> {
                if (nuevoTexto == null || nuevoTexto.isEmpty()) {
                    return true;
                }
                String textoBusqueda = nuevoTexto.toLowerCase();
                //Buscar por folio
                if (String.valueOf(venta.getIdVenta()).contains(textoBusqueda)) {
                    return true;
                }
                //Buscar por fecha
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                if (dateFormat.format(venta.getFecha()).toLowerCase().contains(textoBusqueda)) {
                    return true;
                }
                //Buscar por total
                if (String.format("%.2f", venta.getTotal()).contains(textoBusqueda)) {
                    return true;
                }
                return false;
            });
            actualizarTotalVentas();
        });
    }

    private void actualizarTotalVentas() {
        double total = datosFiltrados.stream()
                .mapToDouble(Venta::getTotal)
                .sum();
        totalVentas.setText(String.format("$%.2f", total));
    }

    private void verDetallesVenta(Venta venta) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("VerDetallesVenta.fxml"));
            Parent root = loader.load();
            VerDetallesVentaController controller = loader.getController();
            controller.setVenta(venta);
            Stage stage = new Stage();
            stage.setTitle("ElectroStock - Detalles de Venta #" + venta.getIdVenta());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudieron cargar los detalles de la venta", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void volverAVentas() {
        try {
            //Cargar el punto de venta en la misma ventana
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PuntoVenta.fxml"));
            Parent root = loader.load();
            //Pasar el ID del usuario al controlador del punto de venta
            PuntoVentaController controller = loader.getController();
            controller.setIdUsuarioActual(SesionUsuario.getIdUsuario());
            Stage currentStage = (Stage) botonVolver.getScene().getWindow();
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("ElectroStock - Punto de Venta");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo regresar al punto de venta", Alert.AlertType.ERROR);
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}