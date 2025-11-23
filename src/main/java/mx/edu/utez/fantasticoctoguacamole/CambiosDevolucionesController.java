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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import mx.edu.utez.fantasticoctoguacamole.modelo.Venta;
import mx.edu.utez.fantasticoctoguacamole.modelo.DetalleVenta;
import mx.edu.utez.fantasticoctoguacamole.modelo.SesionUsuario;
import mx.edu.utez.fantasticoctoguacamole.modelo.dao.VentaDao;
import mx.edu.utez.fantasticoctoguacamole.modelo.dao.DevolucionDao;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class CambiosDevolucionesController implements Initializable {

    @FXML
    private TableView<Venta> tablaVenta;
    @FXML
    private TableColumn<Venta, Integer> tablaFolio;
    @FXML
    private TableColumn<Venta, Date> tablaFecha;
    @FXML
    private TableColumn<Venta, Integer> tablaItems;
    @FXML
    private TableColumn<Venta, Double> tablaTotal;
    @FXML
    private TableColumn<Venta, String> tablaAccion;
    @FXML
    private TextField buscador;
    @FXML
    private Button botonRegresar;

    private ObservableList<Venta> listaVentas;
    private FilteredList<Venta> datosFiltrados;
    private SortedList<Venta> datosOrdenados;
    private VentaDao ventaDao;
    private DevolucionDao devolucionDao;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ventaDao = new VentaDao();
        devolucionDao = new DevolucionDao();
        configurarColumnas();
        cargarVentas();
        configurarBusqueda();
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
        tablaAccion.setCellFactory(new Callback<TableColumn<Venta, String>, TableCell<Venta, String>>() {
            @Override
            public TableCell<Venta, String> call(TableColumn<Venta, String> param) {
                return new TableCell<Venta, String>() {
                    private final HBox botonesContainer = new HBox(5);
                    private final Button btnDevolucion = new Button("Devolver");
                    private final Button btnCambio = new Button("Cambiar");
                    {
                        botonesContainer.getChildren().addAll(btnDevolucion, btnCambio);
                        btnDevolucion.setOnAction(event -> {
                            Venta venta = getTableView().getItems().get(getIndex());
                            if (esVentaElegible(venta)) {
                                abrirDevolucion(venta);
                            } else {
                                mostrarAlerta("No elegible", "Esta venta tiene más de 30 días y no es elegible para devolución", Alert.AlertType.WARNING);
                            }
                        });
                        btnCambio.setOnAction(event -> {
                            Venta venta = getTableView().getItems().get(getIndex());
                            if (esVentaElegible(venta)) {
                                abrirCambio(venta);
                            } else {
                                mostrarAlerta("No elegible", "Esta venta tiene más de 30 días y no es elegible para cambio", Alert.AlertType.WARNING);
                            }
                        });
                    }
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Venta venta = getTableView().getItems().get(getIndex());
                            boolean elegible = esVentaElegible(venta);
                            btnDevolucion.setDisable(!elegible);
                            btnCambio.setDisable(!elegible);
                            if (!elegible) {
                                btnDevolucion.setTooltip(new Tooltip("Venta con más de 30 días"));
                                btnCambio.setTooltip(new Tooltip("Venta con más de 30 días"));
                            }
                            setGraphic(botonesContainer);
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
        datosOrdenados.comparatorProperty().bind(tablaVenta.comparatorProperty());
        tablaVenta.setItems(datosOrdenados);
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
        });
    }

    private boolean esVentaElegible(Venta venta) {
        return devolucionDao.esVentaElegibleParaDevolucion((java.sql.Date) venta.getFecha());
    }

    private void abrirDevolucion(Venta venta) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ProcesarDevolucion.fxml"));
            Parent root = loader.load();
            ProcesarDevolucionController controller = loader.getController();
            controller.setVenta(venta);
            controller.setTipoProceso("DEVOLUCION");
            Stage stage = new Stage();
            stage.setTitle("ElectroStock - Procesar Devolución - Venta #" + venta.getIdVenta());
            stage.setScene(new Scene(root));
            stage.showAndWait();
            //Recargar ventas
            cargarVentas();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la ventana de devolución", Alert.AlertType.ERROR);
        }
    }

    private void abrirCambio(Venta venta) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ProcesarDevolucion.fxml"));
            Parent root = loader.load();
            ProcesarDevolucionController controller = loader.getController();
            controller.setVenta(venta);
            controller.setTipoProceso("CAMBIO");
            Stage stage = new Stage();
            stage.setTitle("ElectroStock - Procesar Cambio - Venta #" + venta.getIdVenta());
            stage.setScene(new Scene(root));
            stage.showAndWait();
            //Recargar ventas
            cargarVentas();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la ventana de cambio", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void regresar() {
        Stage currentStage = (Stage) botonRegresar.getScene().getWindow();
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