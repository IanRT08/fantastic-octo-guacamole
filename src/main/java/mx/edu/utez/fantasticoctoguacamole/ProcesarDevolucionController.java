package mx.edu.utez.fantasticoctoguacamole;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;
import mx.edu.utez.fantasticoctoguacamole.modelo.*;
import mx.edu.utez.fantasticoctoguacamole.modelo.dao.DevolucionDao;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class ProcesarDevolucionController implements Initializable {

    @FXML
    private Label labelTitulo;
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
    private TableColumn<DetalleVentaSeleccionable, Integer> columnaCantidadDevolver;
    @FXML
    private TableColumn<DetalleVentaSeleccionable, Double> columnaSubtotal;
    @FXML
    private TextArea textAreaMotivo;
    @FXML
    private Label labelTotalDevolucion;
    @FXML
    private Button btnProcesar;

    private Venta venta;
    private String tipoProceso;
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

    public void setTipoProceso(String tipoProceso) {
        this.tipoProceso = tipoProceso;
        actualizarInterfaz();
    }

    private void configurarTablaDetalles() {
        //Configurar columna de seleccion
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
                            tablaDetalles.refresh();
                            actualizarTotalDevolucion();
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
        //Configurar columna de producto
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
        //Configurar columna de cantidad original
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
        //Configurar columna de cantidad disponible
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
        //Configurar columna de cantidad a devolver
        columnaCantidadDevolver.setCellValueFactory(new PropertyValueFactory<>("cantidadDevolver"));
        columnaCantidadDevolver.setCellFactory(new Callback<TableColumn<DetalleVentaSeleccionable, Integer>, TableCell<DetalleVentaSeleccionable, Integer>>() {
            @Override
            public TableCell<DetalleVentaSeleccionable, Integer> call(TableColumn<DetalleVentaSeleccionable, Integer> param) {
                return new TableCell<DetalleVentaSeleccionable, Integer>() {
                    private final Spinner<Integer> spinner = new Spinner<>(0, 0, 0);
                    {
                        spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
                            DetalleVentaSeleccionable detalleSeleccionable = getTableView().getItems().get(getIndex());
                            if (detalleSeleccionable != null) {
                                detalleSeleccionable.setCantidadDevolver(newValue);
                                actualizarTotalDevolucion();
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
                            //Establecer el valor actual si existe
                            if (detalleSeleccionable.getCantidadDevolver() > 0) {
                                spinner.getValueFactory().setValue(detalleSeleccionable.getCantidadDevolver());
                            }

                            setGraphic(spinner);
                        }
                    }
                };
            }
        });
        //Configurar columna de subtotal
        columnaSubtotal.setCellFactory(new Callback<TableColumn<DetalleVentaSeleccionable, Double>, TableCell<DetalleVentaSeleccionable, Double>>() {
            @Override
            public TableCell<DetalleVentaSeleccionable, Double> call(TableColumn<DetalleVentaSeleccionable, Double> param) {
                return new TableCell<DetalleVentaSeleccionable, Double>() {
                    @Override
                    protected void updateItem(Double subtotal, boolean empty) {
                        super.updateItem(subtotal, empty);
                        if (empty || subtotal == null) {
                            setText(null);
                        } else {
                            DetalleVentaSeleccionable detalleSeleccionable = getTableView().getItems().get(getIndex());
                            setText(String.format("$%.2f", detalleSeleccionable.getSubtotal()));
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

    private void actualizarInterfaz() {
        if ("CAMBIO".equals(tipoProceso)) {
            labelTitulo.setText("Procesar Cambio");
            btnProcesar.setText("Procesar Cambio");
            labelTotalDevolucion.setText("Total a cambiar: $0.00");
        } else {
            labelTitulo.setText("Procesar Devolución");
            btnProcesar.setText("Procesar Devolución");
            labelTotalDevolucion.setText("Total a reembolsar: $0.00");
        }
    }

    private void actualizarTotalDevolucion() {
        double total = 0;
        if (detallesObservable != null) {
            for (DetalleVentaSeleccionable detalle : detallesObservable) {
                if (detalle.isSeleccionado() && detalle.getCantidadDevolver() > 0) {
                    double subtotalPorUnidad = detalle.getSubtotal() / detalle.getCantidad();
                    total += subtotalPorUnidad * detalle.getCantidadDevolver();
                }
            }
        }
        if ("CAMBIO".equals(tipoProceso)) {
            labelTotalDevolucion.setText(String.format("Total a cambiar: $%.2f", total));
        } else {
            labelTotalDevolucion.setText(String.format("Total a reembolsar: $%.2f", total));
        }
    }

    @FXML
    private void procesarDevolucion() {
        //Validar que haya productos seleccionados
        List<DetalleVentaSeleccionable> detallesSeleccionados = detallesObservable.stream()
                .filter(DetalleVentaSeleccionable::isSeleccionado)
                .filter(d -> d.getCantidadDevolver() > 0)
                .collect(Collectors.toList());
        if (detallesSeleccionados.isEmpty()) {
            mostrarAlerta("Error", "Seleccione al menos un producto y especifique la cantidad a devolver/cambiar", Alert.AlertType.ERROR);
            return;
        }
        if (textAreaMotivo.getText().trim().isEmpty()) {
            mostrarAlerta("Error", "Ingrese el motivo de la " + ("CAMBIO".equals(tipoProceso) ? "cambio" : "devolución"), Alert.AlertType.ERROR);
            return;
        }
        //Mostrar confirmacion
        double total = calcularTotalDevolucion(detallesSeleccionados);
        String mensaje = String.format("¿Está seguro de que desea procesar la %s por un total de $%.2f?\n\nProductos:\n",
                ("CAMBIO".equals(tipoProceso) ? "cambio" : "devolución"), total);
        for (DetalleVentaSeleccionable detalle : detallesSeleccionados) {
            mensaje += String.format("- %s: %d unidades\n",
                    detalle.getProducto().getNombre(), detalle.getCantidadDevolver());
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar " + ("CAMBIO".equals(tipoProceso) ? "Cambio" : "Devolución"));
        confirmacion.setHeaderText("¿Confirmar operación?");
        confirmacion.setContentText(mensaje);
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            //Procesar cada detalle seleccionado
            boolean exito = true;
            for (DetalleVentaSeleccionable detalleSeleccionable : detallesSeleccionados) {
                Devolucion devolucion = new Devolucion();
                devolucion.setIdDetalleVenta(detalleSeleccionable.getIdDetalle());
                devolucion.setFechaDevolucion(new Date());
                devolucion.setCantidadDevuelta(detalleSeleccionable.getCantidadDevolver());
                devolucion.setMontoReembolsado((detalleSeleccionable.getSubtotal() / detalleSeleccionable.getCantidad()) * detalleSeleccionable.getCantidadDevolver());
                devolucion.setMotivo(textAreaMotivo.getText().trim() + " (" + ("CAMBIO".equals(tipoProceso) ? "CAMBIO" : "DEVOLUCION") + ")");
                if (!devolucionDao.registrarDevolucion(devolucion)) {
                    exito = false;
                    break;
                }
            }
            if (exito) {
                mostrarAlerta("Éxito",
                        ("CAMBIO".equals(tipoProceso) ? "Cambio" : "Devolución") + " procesada correctamente",
                        Alert.AlertType.INFORMATION);
                cerrarVentana();
            } else {
                mostrarAlerta("Error",
                        "Error al procesar la " + ("CAMBIO".equals(tipoProceso) ? "cambio" : "devolución"),
                        Alert.AlertType.ERROR);
            }
        }
    }

    private double calcularTotalDevolucion(List<DetalleVentaSeleccionable> detalles) {
        return detalles.stream()
                .mapToDouble(d -> (d.getSubtotal() / d.getCantidad()) * d.getCantidadDevolver())
                .sum();
    }

    @FXML
    private void cancelar() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) labelTitulo.getScene().getWindow();
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