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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import mx.edu.utez.fantasticoctoguacamole.modelo.*;
import mx.edu.utez.fantasticoctoguacamole.modelo.dao.ProductoDao;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class BuscarProductosNuevosController implements Initializable {

    @FXML
    private Label labelProductosSeleccionados;
    @FXML
    private TextField buscador;
    @FXML
    private TableView<Producto> tablaProductos;
    @FXML
    private TableColumn<Producto, String> columnaCodigo;
    @FXML
    private TableColumn<Producto, String> columnaNombre;
    @FXML
    private TableColumn<Producto, Double> columnaPrecio;
    @FXML
    private TableColumn<Producto, Integer> columnaStock;
    @FXML
    private TableColumn<Producto, String> columnaSeleccionar;
    @FXML
    private Button btnContinuar;
    @FXML
    private Button btnCancelar;

    private Venta venta;
    private List<DetalleVentaSeleccionable> productosACambiar;
    private ProductoDao productoDao;
    private ObservableList<Producto> listaProductos;
    private FilteredList<Producto> datosFiltrados;
    private Map<Integer, ProductoSeleccionado> productosSeleccionados;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        productoDao = new ProductoDao();
        productosSeleccionados = new HashMap<>();
        configurarTablaProductos();
        cargarProductos();
        configurarBusqueda();
    }

    public void setVentaYProductos(Venta venta, List<DetalleVentaSeleccionable> productosACambiar) {
        this.venta = venta;
        this.productosACambiar = productosACambiar;
        actualizarLabelProductos();
    }

    private void configurarTablaProductos() {
        columnaCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        columnaNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        columnaPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        columnaPrecio.setCellFactory(new Callback<TableColumn<Producto, Double>, TableCell<Producto, Double>>() {
            @Override
            public TableCell<Producto, Double> call(TableColumn<Producto, Double> param) {
                return new TableCell<Producto, Double>() {
                    @Override
                    protected void updateItem(Double precio, boolean empty) {
                        super.updateItem(precio, empty);
                        if (empty || precio == null) {
                            setText(null);
                        } else {
                            setText(String.format("$%.2f", precio));
                        }
                    }
                };
            }
        });
        columnaStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        columnaStock.setCellFactory(new Callback<TableColumn<Producto, Integer>, TableCell<Producto, Integer>>() {
            @Override
            public TableCell<Producto, Integer> call(TableColumn<Producto, Integer> param) {
                return new TableCell<Producto, Integer>() {
                    private final Label stockLabel = new Label();
                    {
                        stockLabel.setStyle("-fx-padding: 5px 10px; -fx-background-radius: 10px; -fx-text-fill: white; -fx-font-weight: bold;");
                    }
                    @Override
                    protected void updateItem(Integer stock, boolean empty) {
                        super.updateItem(stock, empty);
                        if (empty || stock == null) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            stockLabel.setText(stock.toString());
                            if (stock == 0) {
                                stockLabel.setStyle("-fx-background-color: #F44336; -fx-padding: 5px 10px; -fx-background-radius: 10px; -fx-text-fill: white; -fx-font-weight: bold;");
                            } else if (stock < 5) {
                                stockLabel.setStyle("-fx-background-color: #F44336; -fx-padding: 5px 10px; -fx-background-radius: 10px; -fx-text-fill: white; -fx-font-weight: bold;");
                            } else if (stock < 10) {
                                stockLabel.setStyle("-fx-background-color: #FF9800; -fx-padding: 5px 10px; -fx-background-radius: 10px; -fx-text-fill: white; -fx-font-weight: bold;");
                            } else {
                                stockLabel.setStyle("-fx-background-color: #4CAF50; -fx-padding: 5px 10px; -fx-background-radius: 10px; -fx-text-fill: white; -fx-font-weight: bold;");
                            }
                            setGraphic(stockLabel);
                        }
                    }
                };
            }
        });

        columnaSeleccionar.setCellFactory(new Callback<TableColumn<Producto, String>, TableCell<Producto, String>>() {
            @Override
            public TableCell<Producto, String> call(TableColumn<Producto, String> param) {
                return new TableCell<Producto, String>() {
                    private final Button btnSeleccionar = new Button("Seleccionar");
                    {
                        btnSeleccionar.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                        btnSeleccionar.setOnAction(event -> {
                            Producto producto = getTableView().getItems().get(getIndex());
                            seleccionarProducto(producto);
                        });
                    }
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Producto producto = getTableView().getItems().get(getIndex());
                            // Deshabilitar si ya está seleccionado o no hay stock
                            boolean yaSeleccionado = productosSeleccionados.containsKey(producto.getIdProducto());
                            boolean sinStock = producto.getStock() <= 0;
                            btnSeleccionar.setDisable(yaSeleccionado || sinStock);
                            if (yaSeleccionado) {
                                btnSeleccionar.setText("✓ Seleccionado");
                                btnSeleccionar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                            } else if (sinStock) {
                                btnSeleccionar.setText("Sin Stock");
                                btnSeleccionar.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white;");
                            } else {
                                btnSeleccionar.setText("Seleccionar");
                                btnSeleccionar.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                            }
                            setGraphic(btnSeleccionar);
                        }
                    }
                };
            }
        });
    }

    private void cargarProductos() {
        List<Producto> productos = productoDao.readProductos();
        //Filtrar solo productos activos y con stock
        List<Producto> productosDisponibles = new ArrayList<>();
        for (Producto producto : productos) {
            if (producto.getEstado() && producto.getStock() > 0) {
                productosDisponibles.add(producto);
            }
        }
        listaProductos = FXCollections.observableArrayList(productosDisponibles);
        datosFiltrados = new FilteredList<>(listaProductos, p -> true);
        SortedList<Producto> datosOrdenados = new SortedList<>(datosFiltrados);
        datosOrdenados.comparatorProperty().bind(tablaProductos.comparatorProperty());
        tablaProductos.setItems(datosOrdenados);
    }

    private void configurarBusqueda() {
        buscador.textProperty().addListener((observable, oldValue, nuevoTexto) -> {
            datosFiltrados.setPredicate(producto -> {
                if (nuevoTexto == null || nuevoTexto.isEmpty()) {
                    return true;
                }
                String textoBusqueda = nuevoTexto.toLowerCase();
                return producto.getNombre().toLowerCase().contains(textoBusqueda) ||
                        producto.getCodigo().toLowerCase().contains(textoBusqueda) ||
                        String.valueOf(producto.getPrecio()).contains(textoBusqueda);
            });
        });
    }

    private void actualizarLabelProductos() {
        if (productosACambiar != null && !productosACambiar.isEmpty()) {
            StringBuilder sb = new StringBuilder("Productos a cambiar: ");
            for (DetalleVentaSeleccionable detalle : productosACambiar) {
                sb.append(detalle.getProducto().getNombre())
                        .append(" (")
                        .append(detalle.getCantidadDevolver())
                        .append("), ");
            }
            //Eliminar la ultima coma
            if (sb.length() > 2) {
                sb.setLength(sb.length() - 2);
            }
            labelProductosSeleccionados.setText(sb.toString());
        }
    }

    private void seleccionarProducto(Producto producto) {
        ProductoSeleccionado productoSeleccionado = new ProductoSeleccionado(producto);
        productosSeleccionados.put(producto.getIdProducto(), productoSeleccionado);
        tablaProductos.refresh();
        actualizarBotonContinuar();
    }

    private void actualizarBotonContinuar() {
        //Verificar que tengamos al menos un producto seleccionado
        btnContinuar.setDisable(productosSeleccionados.isEmpty());
    }

    @FXML
    private void continuar() {
        if (productosSeleccionados.isEmpty()) {
            mostrarAlerta("Error", "Seleccione al menos un producto de reemplazo", Alert.AlertType.ERROR);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ConfirmarCambio.fxml"));
            Parent root = loader.load();
            ConfirmarCambioController controller = loader.getController();
            controller.setDatosCambio(venta, productosACambiar, new ArrayList<>(productosSeleccionados.values()));
            Stage stage = new Stage();
            stage.setTitle("ElectroStock - Confirmar Cambio");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            cerrarVentana();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la confirmación del cambio", Alert.AlertType.ERROR);
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

    //Clase interna para manejar productos seleccionados con cantidad
    public static class ProductoSeleccionado {
        private Producto producto;
        private int cantidad;
        public ProductoSeleccionado(Producto producto) {
            this.producto = producto;
            this.cantidad = 1; // Por defecto 1
        }
        public Producto getProducto() { return producto; }
        public void setProducto(Producto producto) { this.producto = producto; }
        public int getCantidad() { return cantidad; }
        public void setCantidad(int cantidad) { this.cantidad = cantidad; }
        public double getSubtotal() {
            return producto.getPrecio() * cantidad;
        }
    }
}