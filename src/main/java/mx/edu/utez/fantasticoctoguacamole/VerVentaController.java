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
import mx.edu.utez.fantasticoctoguacamole.modelo.dao.VentaCanceladaDao;
import mx.edu.utez.fantasticoctoguacamole.modelo.dao.VentaDao;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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
    @FXML
    private DatePicker datePickerDesde;
    @FXML
    private DatePicker datePickerHasta;
    @FXML
    private Button btnAplicarFiltro;
    @FXML
    private Button btnLimpiarFiltro;

    private ObservableList<Venta> listaVentas;
    private FilteredList<Venta> datosFiltrados;
    private SortedList<Venta> datosOrdenados;
    private VentaDao ventaDao;
    private LocalDate fechaMinima;
    private LocalDate fechaMaxima;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ventaDao = new VentaDao();
        configurarFiltrosFecha();
        configurarColumnas();
        cargarVentas();
        configurarBusqueda();
        actualizarTotalVentas();
    }

    private void configurarFiltrosFecha() {
        //Obtener fechas mínima y máxima de las ventas del usuario
        int idUsuario = SesionUsuario.getIdUsuario();
        Date fechaMasAntigua = ventaDao.obtenerFechaVentaMasAntigua(idUsuario);
        Date fechaMasReciente = ventaDao.obtenerFechaVentaMasReciente(idUsuario);
        //Convertir a LocalDate de manera segura
        if (fechaMasAntigua != null) {
            fechaMinima = convertToLocalDate(fechaMasAntigua);
        } else {
            fechaMinima = LocalDate.now(); // Si no hay ventas, usar fecha actual
        }
        if (fechaMasReciente != null) {
            fechaMaxima = convertToLocalDate(fechaMasReciente);
        } else {
            fechaMaxima = LocalDate.now();
        }
        //Configurar DatePickers con restricciones
        datePickerDesde.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.compareTo(fechaMinima) < 0 || date.compareTo(fechaMaxima) > 0);
            }
        });
        datePickerHasta.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.compareTo(fechaMinima) < 0 || date.compareTo(fechaMaxima) > 0);
            }
        });
        //Establecer tooltips informativos
        String rangoFechas = String.format("Rango disponible: %s - %s",
                formatLocalDate(fechaMinima),
                formatLocalDate(fechaMaxima));
        datePickerDesde.setTooltip(new Tooltip(rangoFechas));
        datePickerHasta.setTooltip(new Tooltip(rangoFechas));
    }

    private LocalDate convertToLocalDate(Date dateToConvert) {
        if (dateToConvert instanceof java.sql.Date) {
            // Para java.sql.Date, convertir a java.util.Date primero
            return new java.util.Date(dateToConvert.getTime())
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        } else {
            return dateToConvert.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }
    }

    private String formatLocalDate(LocalDate date) {
        return date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    @FXML
    private void aplicarFiltroFechas() {
        LocalDate fechaDesde = datePickerDesde.getValue();
        LocalDate fechaHasta = datePickerHasta.getValue();
        //Validaciones
        if (fechaDesde == null && fechaHasta == null) {
            mostrarAlerta("Advertencia", "Seleccione al menos una fecha para filtrar", Alert.AlertType.WARNING);
            return;
        }
        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            mostrarAlerta("Error", "La fecha 'Desde' no puede ser posterior a la fecha 'Hasta'", Alert.AlertType.ERROR);
            return;
        }
        if (fechaDesde != null && fechaDesde.isBefore(fechaMinima)) {
            mostrarAlerta("Error",
                    String.format("La fecha 'Desde' no puede ser anterior a %s", formatLocalDate(fechaMinima)),
                    Alert.AlertType.ERROR);
            return;
        }
        if (fechaHasta != null && fechaHasta.isAfter(fechaMaxima)) {
            mostrarAlerta("Error",
                    String.format("La fecha 'Hasta' no puede ser posterior a %s", formatLocalDate(fechaMaxima)),
                    Alert.AlertType.ERROR);
            return;
        }
        //Aplicar filtro
        aplicarFiltroCombinado();
    }

    @FXML
    private void limpiarFiltroFechas() {
        datePickerDesde.setValue(null);
        datePickerHasta.setValue(null);
        aplicarFiltroCombinado();
    }

    private void aplicarFiltroCombinado() {
        datosFiltrados.setPredicate(venta -> {
            //Filtro por fechas
            LocalDate fechaDesde = datePickerDesde.getValue();
            LocalDate fechaHasta = datePickerHasta.getValue();
            LocalDate fechaVenta = convertToLocalDate(venta.getFecha());
            boolean coincideFecha = true;
            if (fechaDesde != null && fechaHasta != null) {
                //Rango completo
                coincideFecha = (fechaVenta.isEqual(fechaDesde) || fechaVenta.isAfter(fechaDesde)) &&
                        (fechaVenta.isEqual(fechaHasta) || fechaVenta.isBefore(fechaHasta));
            } else if (fechaDesde != null) {
                //Solo fecha desde
                coincideFecha = fechaVenta.isEqual(fechaDesde) || fechaVenta.isAfter(fechaDesde);
            } else if (fechaHasta != null) {
                //Solo fecha hasta
                coincideFecha = fechaVenta.isEqual(fechaHasta) || fechaVenta.isBefore(fechaHasta);
            }
            //Filtro por busqueda de texto
            boolean coincideBusqueda = true;
            String textoBusqueda = buscador.getText();
            if (textoBusqueda != null && !textoBusqueda.isEmpty()) {
                textoBusqueda = textoBusqueda.toLowerCase();
                coincideBusqueda = String.valueOf(venta.getIdVenta()).contains(textoBusqueda) ||
                        new SimpleDateFormat("dd/MM/yyyy").format(venta.getFecha()).toLowerCase().contains(textoBusqueda) ||
                        String.format("%.2f", venta.getTotal()).contains(textoBusqueda);
            }
            return coincideFecha && coincideBusqueda;
        });
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
                    private final HBox botonesContainer = new HBox(5);
                    private final Button btnVerDetalles = new Button("Ver más");
                    private final Button btnCancelar = new Button("Cancelar venta");
                    {
                        botonesContainer.getChildren().addAll(btnVerDetalles, btnCancelar);
                        btnVerDetalles.setOnAction(event -> {
                            Venta venta = getTableView().getItems().get(getIndex());
                            verDetallesVenta(venta);
                        });
                        btnCancelar.setOnAction(event -> {
                            Venta venta = getTableView().getItems().get(getIndex());
                            cancelarVenta(venta);
                        });
                    }
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Venta venta = getTableView().getItems().get(getIndex());
                            //Verificar si la venta ya fue cancelada
                            VentaCanceladaDao ventaCanceladaDao = new VentaCanceladaDao();
                            boolean yaCancelada = ventaCanceladaDao.obtenerVentaCanceladaPorIdOriginal(venta.getIdVenta()) != null;
                            btnCancelar.setDisable(yaCancelada);
                            if (yaCancelada) {
                                btnCancelar.setTooltip(new Tooltip("Venta ya cancelada"));
                            } else {
                                btnCancelar.setTooltip(new Tooltip("Cancelar venta"));
                            }
                            setGraphic(botonesContainer);
                        }
                    }
                };
            }
        });
    }

    private void cancelarVenta(Venta venta) {
        if (venta == null) return;
        //Diálogo para ingresar motivo
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Cancelar Venta");
        dialog.setHeaderText("Cancelar Venta #" + venta.getIdVenta());
        dialog.setContentText("Motivo de cancelación:");
        Optional<String> resultado = dialog.showAndWait();
        if (resultado.isPresent() && !resultado.get().trim().isEmpty()) {
            String motivo = resultado.get().trim();
            //Confirmación
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar Cancelación");
            confirmacion.setHeaderText("¿Cancelar venta #" + venta.getIdVenta() + "?");
            confirmacion.setContentText("Monto a reembolsar: $" + String.format("%.2f", venta.getTotal()) +
                    "\nStock será restaurado.\nEsta acción no se puede deshacer.");
            Optional<ButtonType> resultadoConfirmacion = confirmacion.showAndWait();
            if (resultadoConfirmacion.isPresent() && resultadoConfirmacion.get() == ButtonType.OK) {
                VentaCanceladaDao ventaCanceladaDao = new VentaCanceladaDao();
                int idUsuarioActual = SesionUsuario.getIdUsuario();
                if (ventaCanceladaDao.cancelarVenta(venta.getIdVenta(), motivo, idUsuarioActual)) {
                    mostrarAlerta("Éxito", "Venta cancelada correctamente. Stock restaurado.", Alert.AlertType.INFORMATION);
                    cargarVentas(); // Recargar datos
                } else {
                    mostrarAlerta("Error", "No se pudo cancelar la venta", Alert.AlertType.ERROR);
                }
            }
        } else {
            mostrarAlerta("Error", "Debe ingresar un motivo para cancelar la venta", Alert.AlertType.WARNING);
        }
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
            aplicarFiltroCombinado();
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