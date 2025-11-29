package mx.edu.utez.fantasticoctoguacamole;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
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
import mx.edu.utez.fantasticoctoguacamole.modelo.dao.*;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class HistorialTransaccionesController implements Initializable {

    @FXML
    private TableView<Transaccion> tablaTransacciones;
    @FXML
    private TableColumn<Transaccion, String> tablaCajero;
    @FXML
    private TableColumn<Transaccion, String> tablaTipo;
    @FXML
    private TableColumn<Transaccion, Date> tablaFecha;
    @FXML
    private TableColumn<Transaccion, String> tablaVer;
    @FXML
    private TextField buscador;
    @FXML
    private ChoiceBox<String> filtros;
    @FXML
    private Label totalVentas;
    @FXML
    private Label totalDevoluciones;
    @FXML
    private Label totalCambios;
    @FXML
    private Label totalTransacciones;
    @FXML
    private Button botonRegresar;

    private ObservableList<Transaccion> listaTransacciones;
    private FilteredList<Transaccion> datosFiltrados;
    private VentaDao ventaDao;
    private DevolucionDao devolucionDao;
    private CambioDao cambioDao;
    private UsuarioDao usuarioDao;
    private VentaCanceladaDao ventaCanceladaDao;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ventaDao = new VentaDao();
        devolucionDao = new DevolucionDao();
        cambioDao = new CambioDao();
        usuarioDao = new UsuarioDao();
        ventaCanceladaDao = new VentaCanceladaDao();
        configurarFiltros();
        configurarColumnas();
        cargarTransacciones();
        configurarBusqueda();
        actualizarTotales();
    }

    private void configurarFiltros() {
        filtros.setItems(FXCollections.observableArrayList(
                "Todas", "Ventas", "Devoluciones", "Cambios", "Canceladas"
        ));
        filtros.getSelectionModel().selectFirst();
        filtros.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, nuevoFiltro) -> aplicarFiltros()
        );
    }

    private void configurarColumnas() {
        tablaCajero.setCellValueFactory(new PropertyValueFactory<>("nombreCajero"));
        tablaTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        tablaTipo.setCellFactory(new Callback<TableColumn<Transaccion, String>, TableCell<Transaccion, String>>() {
            @Override
            public TableCell<Transaccion, String> call(TableColumn<Transaccion, String> param) {
                return new TableCell<Transaccion, String>() {
                    private final Label tipoLabel = new Label();
                    {
                        tipoLabel.setStyle("-fx-padding: 5px 10px; -fx-background-radius: 10px; -fx-text-fill: white; -fx-font-weight: bold;");
                    }
                    @Override
                    protected void updateItem(String tipo, boolean empty) {
                        super.updateItem(tipo, empty);
                        if (empty || tipo == null) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            tipoLabel.setText(tipo);
                            switch (tipo) {
                                case "VENTA":
                                    tipoLabel.setStyle("-fx-background-color: #4CAF50; -fx-padding: 5px 10px; -fx-background-radius: 10px; -fx-text-fill: white; -fx-font-weight: bold;");
                                    break;
                                case "CAMBIO":
                                    tipoLabel.setStyle("-fx-background-color: #9E9E9E; -fx-padding: 5px 10px; -fx-background-radius: 10px; -fx-text-fill: white; -fx-font-weight: bold;");
                                    break;
                                case "DEVOLUCION":
                                    tipoLabel.setStyle("-fx-background-color: #F44336; -fx-padding: 5px 10px; -fx-background-radius: 10px; -fx-text-fill: white; -fx-font-weight: bold;");
                                    break;
                                case "CANCELADA":
                                    tipoLabel.setStyle("-fx-background-color: #FF9800; -fx-padding: 5px 10px; -fx-background-radius: 10px; -fx-text-fill: white; -fx-font-weight: bold;");
                                    break;
                                default:
                                    tipoLabel.setStyle("-fx-background-color: #2196F3; -fx-padding: 5px 10px; -fx-background-radius: 10px; -fx-text-fill: white; -fx-font-weight: bold;");
                            }
                            setGraphic(tipoLabel);
                        }
                    }
                };
            }
        });
        tablaFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        tablaFecha.setCellFactory(new Callback<TableColumn<Transaccion, Date>, TableCell<Transaccion, Date>>() {
            @Override
            public TableCell<Transaccion, Date> call(TableColumn<Transaccion, Date> param) {
                return new TableCell<Transaccion, Date>() {
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
        tablaVer.setCellFactory(new Callback<TableColumn<Transaccion, String>, TableCell<Transaccion, String>>() {
            @Override
            public TableCell<Transaccion, String> call(TableColumn<Transaccion, String> param) {
                return new TableCell<Transaccion, String>() {
                    private final Button btnVerDetalles = new Button("Ver detalles");
                    {
                        btnVerDetalles.setOnAction(event -> {
                            Transaccion transaccion = getTableView().getItems().get(getIndex());
                            verDetallesTransaccion(transaccion);
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

    private void cargarTransacciones() {
        listaTransacciones = FXCollections.observableArrayList();
        //Cargar ventas activas
        List<Venta> ventas = ventaDao.obtenerTodasLasVentas();
        for (Venta venta : ventas) {
            Transaccion transaccion = new Transaccion();
            transaccion.setTipo("VENTA");
            transaccion.setFecha(venta.getFecha());
            transaccion.setIdTransaccion(venta.getIdVenta());
            transaccion.setMonto(venta.getTotal());
            transaccion.setIdUsuario(venta.getIdUsuario());
            //Obtener nombre del cajero
            String nombreCajero = "N/A";
            if (venta.getIdUsuario() > 0) {
                Usuario usuario = usuarioDao.obtenerUsuarioPorId(venta.getIdUsuario());
                if (usuario != null) {
                    nombreCajero = usuario.getNombre() + " " +
                            (usuario.getApellidoPaterno() != null ? usuario.getApellidoPaterno() : "");
                }
            }
            transaccion.setNombreCajero(nombreCajero.trim());
            transaccion.setObjetoTransaccion(venta);
            listaTransacciones.add(transaccion);
        }
        //Cargar cambios
        List<Cambio> cambios = cambioDao.obtenerTodosLosCambios();
        for (Cambio cambio : cambios) {
            Transaccion transaccion = new Transaccion();
            transaccion.setTipo("CAMBIO");
            transaccion.setFecha(cambio.getFechaCambio());
            transaccion.setIdTransaccion(cambio.getIdCambio());
            transaccion.setMonto(cambio.getDiferenciaPago());
            //Cargar informacion completa del cambio
            Cambio cambioCompleto = cambioDao.obtenerCambioPorId(cambio.getIdCambio());
            if (cambioCompleto != null) {
                transaccion.setObjetoTransaccion(cambioCompleto);
                //Obtener nombre del cajero
                String nombreCajero = "N/A";
                Venta ventaOrigen = ventaDao.obtenerVentaDesdeDetalle(cambioCompleto.getIdDetalleVentaOrigen());
                if (ventaOrigen != null) {
                    Usuario usuario = usuarioDao.obtenerUsuarioPorId(ventaOrigen.getIdUsuario());
                    if (usuario != null) {
                        nombreCajero = usuario.getNombre() + " " +
                                (usuario.getApellidoPaterno() != null ? usuario.getApellidoPaterno() : "");
                    }
                }
                transaccion.setNombreCajero(nombreCajero.trim());
            }
            listaTransacciones.add(transaccion);
        }
        //Cargar devoluciones
        List<Devolucion> devoluciones = devolucionDao.obtenerTodasLasDevoluciones();
        for (Devolucion devolucion : devoluciones) {
            Transaccion transaccion = new Transaccion();
            transaccion.setTipo("DEVOLUCION");
            transaccion.setFecha(devolucion.getFechaDevolucion());
            transaccion.setIdTransaccion(devolucion.getIdDevolucion());
            transaccion.setMonto(devolucion.getMontoReembolsado());
            //Cargar informacion completa de la devolución
            Devolucion devolucionCompleta = devolucionDao.obtenerDevolucionPorId(devolucion.getIdDevolucion());
            if (devolucionCompleta != null) {
                transaccion.setObjetoTransaccion(devolucionCompleta);
                //Obtener nombre del cajero
                String nombreCajero = "N/A";
                Venta ventaOrigen = ventaDao.obtenerVentaDesdeDetalle(devolucionCompleta.getIdDetalleVenta());
                if (ventaOrigen != null) {
                    Usuario usuario = usuarioDao.obtenerUsuarioPorId(ventaOrigen.getIdUsuario());
                    if (usuario != null) {
                        nombreCajero = usuario.getNombre() + " " +
                                (usuario.getApellidoPaterno() != null ? usuario.getApellidoPaterno() : "");
                    }
                }
                transaccion.setNombreCajero(nombreCajero.trim());
            }
            listaTransacciones.add(transaccion);
        }
        List<VentaCancelada> ventasCanceladas = ventaCanceladaDao.obtenerTodasLasVentasCanceladas();
        for (VentaCancelada ventaCancelada : ventasCanceladas) {
            Transaccion transaccion = new Transaccion();
            transaccion.setTipo("CANCELADA");
            transaccion.setFecha(ventaCancelada.getFechaCancelacion());
            transaccion.setIdTransaccion(ventaCancelada.getIdVentaCancelada());
            transaccion.setMonto(ventaCancelada.getTotalVenta());
            transaccion.setIdUsuario(ventaCancelada.getIdUsuarioCancelacion());
            transaccion.setNombreCajero(ventaCancelada.getNombreUsuarioCancelacion() != null ?
                    ventaCancelada.getNombreUsuarioCancelacion() : "N/A");
            transaccion.setObjetoTransaccion(ventaCancelada);
            listaTransacciones.add(transaccion);
        }
        //Ordenar por fecha
        listaTransacciones.sort((t1, t2) -> t2.getFecha().compareTo(t1.getFecha()));
        datosFiltrados = new FilteredList<>(listaTransacciones, p -> true);
        SortedList<Transaccion> datosOrdenados = new SortedList<>(datosFiltrados);
        datosOrdenados.comparatorProperty().bind(tablaTransacciones.comparatorProperty());
        tablaTransacciones.setItems(datosOrdenados);
        //Mostrar cuantas transacciones se cargaron
        System.out.println("Transacciones cargadas: " + listaTransacciones.size() +
                " (Ventas: " + ventas.size() +
                ", Cambios: " + cambios.size() +
                ", Devoluciones: " + devoluciones.size() + ")");
    }

    private void configurarBusqueda() {
        buscador.textProperty().addListener((observable, oldValue, nuevoTexto) -> {
            aplicarFiltros();
        });
    }

    private void aplicarFiltros() {
        datosFiltrados.setPredicate(transaccion -> {
            String filtroTipo = filtros.getValue();
            String textoBusqueda = buscador.getText();
            //Filtrar por tipo
            if (filtroTipo != null && !filtroTipo.equals("Todas")) {
                String tipoEsperado = "";
                switch (filtroTipo) {
                    case "Ventas": tipoEsperado = "VENTA"; break;
                    case "Devoluciones": tipoEsperado = "DEVOLUCION"; break;
                    case "Cambios": tipoEsperado = "CAMBIO"; break;
                    case "Canceladas": tipoEsperado = "CANCELADA"; break;
                }
                if (!transaccion.getTipo().equals(tipoEsperado)) {
                    return false;
                }
            }
            //Filtrar por busqueda
            if (textoBusqueda == null || textoBusqueda.isEmpty()) {
                return true;
            }
            String textoBusquedaLower = textoBusqueda.toLowerCase();
            //Buscar por nombre de cajero
            if (transaccion.getNombreCajero().toLowerCase().contains(textoBusquedaLower)) {
                return true;
            }
            //Buscar por fecha
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            if (dateFormat.format(transaccion.getFecha()).toLowerCase().contains(textoBusquedaLower)) {
                return true;
            }
            return false;
        });
        actualizarTotales();
    }

    private void actualizarTotales() {
        Calendar cal = Calendar.getInstance();
        int mesActual = cal.get(Calendar.MONTH) + 1;
        int aniooActual = cal.get(Calendar.YEAR);
        double totalVentasMes = 0;
        double totalDevolucionesMes = 0;
        double totalCambiosMes = 0;
        int countTransaccionesMes = 0;
        for (Transaccion transaccion : datosFiltrados) {
            cal.setTime(transaccion.getFecha());
            int mesTransaccion = cal.get(Calendar.MONTH) + 1;
            int anioTransaccion = cal.get(Calendar.YEAR);

            if (mesTransaccion == mesActual && anioTransaccion == aniooActual) {
                countTransaccionesMes++;
                switch (transaccion.getTipo()) {
                    case "VENTA":
                        totalVentasMes += transaccion.getMonto();
                        break;
                    case "DEVOLUCION":
                        totalDevolucionesMes += transaccion.getMonto();
                        break;
                    case "CAMBIO":
                        totalCambiosMes += transaccion.getMonto();
                        break;
                }
            }
        }
        totalVentas.setText(String.format("$%.2f", totalVentasMes));
        totalDevoluciones.setText(String.format("$%.2f", totalDevolucionesMes));
        totalCambios.setText(String.format("$%.2f", totalCambiosMes));
        totalTransacciones.setText(String.valueOf(countTransaccionesMes));
    }

    private void verDetallesTransaccion(Transaccion transaccion) {
        try {
            String fxmlFile = "";
            String titulo = "";
            if ("CANCELADA".equals(transaccion.getTipo())) {
                fxmlFile = "VerDetallesCancelacion.fxml";
                titulo = "Detalles de Venta Cancelada #" + transaccion.getIdTransaccion();
            } else {
                switch (transaccion.getTipo()) {
                    case "VENTA":
                        fxmlFile = "VerDetallesVenta.fxml";
                        titulo = "Comprobante de Venta #" + transaccion.getIdTransaccion();
                        break;
                    case "CAMBIO":
                        fxmlFile = "VerDetallesCambio.fxml";
                        titulo = "Detalles de Cambio #" + transaccion.getIdTransaccion();
                        break;
                    case "DEVOLUCION":
                        fxmlFile = "VerDetallesDevolucion.fxml";
                        titulo = "Detalles de Devolución #" + transaccion.getIdTransaccion();
                        break;
                }
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            if ("CANCELADA".equals(transaccion.getTipo())) {
                VerDetallesCancelacionController cancelacionController = loader.getController();
                VentaCancelada ventaCancelada = (VentaCancelada) transaccion.getObjetoTransaccion();
                cancelacionController.setVentaCancelada(ventaCancelada);
            } else {
                // Configurar según el tipo de transacción
                switch (transaccion.getTipo()) {
                    case "VENTA":
                        VerDetallesVentaController ventaController = loader.getController();
                        Venta venta = (Venta) transaccion.getObjetoTransaccion();
                        // Cargar detalles completos de la venta
                        Venta ventaCompleta = ventaDao.obtenerVentaPorId(venta.getIdVenta());
                        ventaController.setVenta(ventaCompleta);
                        break;
                    case "CAMBIO":
                        VerDetallesCambioController cambioController = loader.getController();
                        Cambio cambio = (Cambio) transaccion.getObjetoTransaccion();
                        cambioController.setCambio(cambio);
                        break;
                    case "DEVOLUCION":
                        VerDetallesDevolucionController devolucionController = loader.getController();
                        Devolucion devolucion = (Devolucion) transaccion.getObjetoTransaccion();
                        devolucionController.setDevolucion(devolucion);
                        break;
                }
            }
            Stage stage = new Stage();
            stage.setTitle("ElectroStock - " + titulo);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudieron cargar los detalles de la transacción", Alert.AlertType.ERROR);
        }
    }

    @FXML
    void regresar(ActionEvent event) {
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