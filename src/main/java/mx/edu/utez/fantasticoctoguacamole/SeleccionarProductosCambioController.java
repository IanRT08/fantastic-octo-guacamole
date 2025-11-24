package mx.edu.utez.fantasticoctoguacamole;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import mx.edu.utez.fantasticoctoguacamole.modelo.*;
import mx.edu.utez.fantasticoctoguacamole.modelo.dao.DevolucionDao;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class SeleccionarProductosCambioController implements Initializable {

    @FXML
    private Label labelFolio;
    @FXML
    private Label labelFecha;
    @FXML
    private TableView<DetalleVentaSeleccionable> tablaDetalles;
    @FXML
    private TableColumn<DetalleVentaSeleccionable, Boolean> columnaSeleccionar;
    @FXML
    private TableColumn<DetalleVentaSeleccionable, String> columnaProducto;
    @FXML
    private TableColumn<DetalleVentaSeleccionable, Integer> columnaCantidadOriginal;
    @FXML
    private TableColumn<DetalleVentaSeleccionable, Integer> columnaCantidadDisponible;
    @FXML
    private TableColumn<DetalleVentaSeleccionable, Integer> columnaCantidadCambiar;
    @FXML
    private TableColumn<DetalleVentaSeleccionable, Double> columnaPrecioUnitario;
    @FXML
    private Button btnContinuar;
    @FXML
    private Button btnCancelar;

    private Venta venta;
    private DevolucionDao devolucionDao;
    private ObservableList<DetalleVentaSeleccionable> detallesObservable;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        devolucionDao = new DevolucionDao();
        configurarTablaDetalles();
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
        mostrarDatosVenta();
        cargarDetalles();
    }

    private void configurarTablaDetalles() {
        columnaSeleccionar.setCellValueFactory(new PropertyValueFactory<>("seleccionado"));
        columnaSeleccionar.setCellFactory(new Callback<TableColumn<DetalleVentaSeleccionable, Boolean>, TableCell<DetalleVentaSeleccionable, Boolean>>() {
            @Override
            public TableCell<DetalleVentaSeleccionable, Boolean> call(TableColumn<DetalleVentaSeleccionable, Boolean> param) {
                return new TableCell<DetalleVentaSeleccionable, Boolean>() {
                    private final CheckBox checkBox = new CheckBox();
                    {
                        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                            DetalleVentaSeleccionable detalleSeleccionable = getTableView().getItems().get(getIndex());
                            detalleSeleccionable.setSeleccionado(newValue);
                            if (!newValue) {
                                detalleSeleccionable.setCantidadDevolver(0);
                            }
                            actualizarBotonContinuar();
                        });
                    }
                    @Override
                    protected void updateItem(Boolean selected, boolean empty) {
                        super.updateItem(selected, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            DetalleVentaSeleccionable detalleSeleccionable = getTableView().getItems().get(getIndex());
                            checkBox.setSelected(detalleSeleccionable.isSeleccionado());
                            setGraphic(checkBox);
                        }
                    }
                };
            }
        });
        columnaProducto.setCellFactory(new Callback<TableColumn<DetalleVentaSeleccionable, String>, TableCell<DetalleVentaSeleccionable, String>>() {
            @Override
            public TableCell<DetalleVentaSeleccionable, String> call(TableColumn<DetalleVentaSeleccionable, String> param) {
                return new TableCell<DetalleVentaSeleccionable, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            DetalleVentaSeleccionable detalleSeleccionable = getTableView().getItems().get(getIndex());
                            if (detalleSeleccionable.getProducto() != null) {
                                setText(detalleSeleccionable.getProducto().getNombre());
                            } else {
                                setText("N/A");
                            }
                        }
                    }
                };
            }
        });
        columnaCantidadOriginal.setCellFactory(new Callback<TableColumn<DetalleVentaSeleccionable, Integer>, TableCell<DetalleVentaSeleccionable, Integer>>() {
            @Override
            public TableCell<DetalleVentaSeleccionable, Integer> call(TableColumn<DetalleVentaSeleccionable, Integer> param) {
                return new TableCell<DetalleVentaSeleccionable, Integer>() {
                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            DetalleVentaSeleccionable detalleSeleccionable = getTableView().getItems().get(getIndex());
                            setText(String.valueOf(detalleSeleccionable.getCantidad()));
                        }
                    }
                };
            }
        });
        columnaCantidadDisponible.setCellFactory(new Callback<TableColumn<DetalleVentaSeleccionable, Integer>, TableCell<DetalleVentaSeleccionable, Integer>>() {
            @Override
            public TableCell<DetalleVentaSeleccionable, Integer> call(TableColumn<DetalleVentaSeleccionable, Integer> param) {
                return new TableCell<DetalleVentaSeleccionable, Integer>() {
                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            DetalleVentaSeleccionable detalleSeleccionable = getTableView().getItems().get(getIndex());
                            int cantidadDisponible = devolucionDao.obtenerCantidadMaximaDevolucion(detalleSeleccionable.getIdDetalle());
                            setText(String.valueOf(cantidadDisponible));
                        }
                    }
                };
            }
        });
        columnaCantidadCambiar.setCellValueFactory(new PropertyValueFactory<>("cantidadDevolver"));
        columnaCantidadCambiar.setCellFactory(new Callback<TableColumn<DetalleVentaSeleccionable, Integer>, TableCell<DetalleVentaSeleccionable, Integer>>() {
            @Override
            public TableCell<DetalleVentaSeleccionable, Integer> call(TableColumn<DetalleVentaSeleccionable, Integer> param) {
                return new TableCell<DetalleVentaSeleccionable, Integer>() {
                    private final Spinner<Integer> spinner = new Spinner<>(0, 0, 0);
                    {
                        spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
                            DetalleVentaSeleccionable detalleSeleccionable = getTableView().getItems().get(getIndex());
                            if (detalleSeleccionable != null) {
                                detalleSeleccionable.setCantidadDevolver(newValue);
                                actualizarBotonContinuar();
                            }
                        });
                    }
                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            DetalleVentaSeleccionable detalleSeleccionable = getTableView().getItems().get(getIndex());
                            int cantidadMaxima = devolucionDao.obtenerCantidadMaximaDevolucion(detalleSeleccionable.getIdDetalle());

                            SpinnerValueFactory<Integer> valueFactory =
                                    new SpinnerValueFactory.IntegerSpinnerValueFactory(0, cantidadMaxima, 0);
                            spinner.setValueFactory(valueFactory);

                            if (detalleSeleccionable.getCantidadDevolver() > 0) {
                                spinner.getValueFactory().setValue(detalleSeleccionable.getCantidadDevolver());
                            }

                            setGraphic(spinner);
                        }
                    }
                };
            }
        });
        columnaPrecioUnitario.setCellFactory(new Callback<TableColumn<DetalleVentaSeleccionable, Double>, TableCell<DetalleVentaSeleccionable, Double>>() {
            @Override
            public TableCell<DetalleVentaSeleccionable, Double> call(TableColumn<DetalleVentaSeleccionable, Double> param) {
                return new TableCell<DetalleVentaSeleccionable, Double>() {
                    @Override
                    protected void updateItem(Double precio, boolean empty) {
                        super.updateItem(precio, empty);
                        if (empty || precio == null) {
                            setText(null);
                        } else {
                            DetalleVentaSeleccionable detalleSeleccionable = getTableView().getItems().get(getIndex());
                            setText(String.format("$%.2f", detalleSeleccionable.getPrecioUnitario()));
                        }
                    }
                };
            }
        });
    }

    private void mostrarDatosVenta() {
        if (venta != null) {
            labelFolio.setText("#" + venta.getIdVenta());
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            labelFecha.setText(dateFormat.format(venta.getFecha()));
        }
    }

    private void cargarDetalles() {
        if (venta != null && venta.getDetalles() != null) {
            detallesObservable = FXCollections.observableArrayList();
            for (DetalleVenta detalle : venta.getDetalles()) {
                detallesObservable.add(new DetalleVentaSeleccionable(detalle));
            }
            tablaDetalles.setItems(detallesObservable);
        }
    }

    private void actualizarBotonContinuar() {
        boolean hayProductosSeleccionados = detallesObservable.stream()
                .anyMatch(d -> d.isSeleccionado() && d.getCantidadDevolver() > 0);
        btnContinuar.setDisable(!hayProductosSeleccionados);
    }

    @FXML
    private void continuar() {
        List<DetalleVentaSeleccionable> productosSeleccionados = detallesObservable.stream()
                .filter(DetalleVentaSeleccionable::isSeleccionado)
                .filter(d -> d.getCantidadDevolver() > 0)
                .collect(Collectors.toList());
        if (productosSeleccionados.isEmpty()) {
            mostrarAlerta("Error", "Seleccione al menos un producto y especifique la cantidad a cambiar", Alert.AlertType.ERROR);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("BuscarProductosNuevos.fxml"));
            Parent root = loader.load();
            BuscarProductosNuevosController controller = loader.getController();
            controller.setVentaYProductos(venta, productosSeleccionados);
            Stage stage = new Stage();
            stage.setTitle("ElectroStock - Buscar Productos de Reemplazo");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            cerrarVentana();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la búsqueda de productos", Alert.AlertType.ERROR);
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