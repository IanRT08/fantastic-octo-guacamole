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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import mx.edu.utez.fantasticoctoguacamole.modelo.dao.ProductoDao;
import mx.edu.utez.fantasticoctoguacamole.modelo.dao.VentaDao;
import mx.edu.utez.fantasticoctoguacamole.modelo.*;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class PuntoVentaController implements Initializable {
    @FXML
    private TextField buscador;
    @FXML
    private Button botonRegresar;
    @FXML
    private Button botonVer;
    @FXML
    private Button botonCambios;
    @FXML
    private TableView<Producto> tablaProducto;
    @FXML
    private TableColumn<Producto, String> tablaCodigo;
    @FXML
    private TableColumn<Producto, String> tablaNombre;
    @FXML
    private TableColumn<Producto, Double> tablaPrecio;
    @FXML
    private TableColumn<Producto, Integer> tablaStock;
    @FXML
    private TableColumn<Producto, String> tablaAccion;
    @FXML
    private Button botonComprobante;
    @FXML
    private Button botonLimpiar;
    @FXML
    private Label contenido;
    @FXML
    private VBox contenidoCarrito;
    @FXML
    private ScrollBar deslizador;
    @FXML
    private Label totalLabel;

    private ObservableList<Producto> listaProductos;
    private FilteredList<Producto> datosFiltrados;
    private SortedList<Producto> datosOrdenados;
    private ObservableList<ItemCarrito> carrito;
    private int idUsuarioActual; //Setear con el ID del usuario logueado
    private final String buttonStyle = "-fx-background-color: transparent;" +
            "-fx-padding: 5;" +
            "-fx-cursor: hand;" +
            "-fx-text-fill: #4A5568;" +
            "-fx-font-size: 14px;";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Inicializar carrito
        carrito = FXCollections.observableArrayList();
        //Configurar columnas
        configurarColumnas();
        //Cargar datos
        cargarProductos();
        //Configurar busqueda
        configurarBusqueda();
        //Actualizar interfaz del carrito
        actualizarCarrito();
        //Deshabilitar botones inicialmente
        botonComprobante.setDisable(true);
        botonLimpiar.setDisable(true);
    }

    public void setIdUsuarioActual(int idUsuario) {
        this.idUsuarioActual = idUsuario;
    }

    private void cargarProductos() {
        ProductoDao dao = new ProductoDao();
        List<Producto> datos = dao.readProductos();
        //Filtrar solo productos activos
        List<Producto> productosActivos = new ArrayList<>();
        for (Producto producto : datos) {
            if (producto.getEstado()) {
                productosActivos.add(producto);
            }
        }
        listaProductos = FXCollections.observableArrayList(productosActivos);
        datosFiltrados = new FilteredList<>(listaProductos, p -> true);
        datosOrdenados = new SortedList<>(datosFiltrados);
        datosOrdenados.comparatorProperty().bind(tablaProducto.comparatorProperty());
        tablaProducto.setItems(datosOrdenados);
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

    private void configurarColumnas() {
        tablaCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        tablaNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        tablaPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        tablaStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        //Configurar columna de precio
        tablaPrecio.setCellFactory(new Callback<TableColumn<Producto, Double>, TableCell<Producto, Double>>() {
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
        //Configurar columna de stock
        tablaStock.setCellFactory(new Callback<TableColumn<Producto, Integer>, TableCell<Producto, Integer>>() {
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
        //Configurar columna de acciones
        tablaAccion.setCellFactory(new Callback<TableColumn<Producto, String>, TableCell<Producto, String>>() {
            @Override
            public TableCell<Producto, String> call(TableColumn<Producto, String> param) {
                return new TableCell<Producto, String>() {
                    private final HBox botonesContainer = new HBox(5);
                    private final Button btnAgregar = new Button("+");
                    private final Button btnQuitar = new Button("-");
                    {
                        botonesContainer.getChildren().addAll(btnAgregar, btnQuitar);
                        btnAgregar.setOnAction(event -> {
                            Producto producto = getTableView().getItems().get(getIndex());
                            agregarAlCarrito(producto);
                        });
                        btnQuitar.setOnAction(event -> {
                            Producto producto = getTableView().getItems().get(getIndex());
                            quitarDelCarrito(producto);
                        });
                        btnAgregar.setStyle(buttonStyle);
                        btnQuitar.setStyle(buttonStyle);
                    }
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Producto producto = getTableView().getItems().get(getIndex());
                            //Deshabilitar boton agregar si stock es 0
                            btnAgregar.setDisable(producto.getStock() == 0);
                            setGraphic(botonesContainer);
                        }
                    }
                };
            }
        });
    }

    private void agregarAlCarrito(Producto producto) {
        if (producto.getStock() <= 0) {
            mostrarAlerta("Error", "No hay stock disponible para " + producto.getNombre(), Alert.AlertType.WARNING);
            return;
        }
        //Buscar si el producto ya esta en el carrito
        for (ItemCarrito item : carrito) {
            if (item.getProducto().getIdProducto() == producto.getIdProducto()) {
                //Verificar que no exceda el stock disponible
                if (item.getCantidad() < producto.getStock()) {
                    item.setCantidad(item.getCantidad() + 1);
                    actualizarCarrito();
                } else {
                    mostrarAlerta("Error", "No hay suficiente stock para " + producto.getNombre(), Alert.AlertType.WARNING);
                }
                return;
            }
        }
        //Si no esta en el carrito, agregarlo
        if (producto.getStock() > 0) {
            carrito.add(new ItemCarrito(producto, 1));
            actualizarCarrito();
        }
    }

    private void quitarDelCarrito(Producto producto) {
        for (ItemCarrito item : carrito) {
            if (item.getProducto().getIdProducto() == producto.getIdProducto()) {
                if (item.getCantidad() > 1) {
                    item.setCantidad(item.getCantidad() - 1);
                } else {
                    carrito.remove(item);
                }
                actualizarCarrito();
                return;
            }
        }
    }

    private void actualizarCarrito() {
        contenidoCarrito.getChildren().clear();
        if (carrito.isEmpty()) {
            contenido.setVisible(true);
            contenido.setText("El carrito está vacío");
            botonComprobante.setDisable(true);
            botonLimpiar.setDisable(true);
        } else {
            contenido.setVisible(false);
            for (ItemCarrito item : carrito) {
                HBox itemBox = new HBox(10);
                Label label = new Label(item.getProducto().getNombre() + " x" + item.getCantidad() + " - $" + String.format("%.2f", item.getSubtotal()));
                itemBox.getChildren().add(label);
                contenidoCarrito.getChildren().add(itemBox);
            }
            botonComprobante.setDisable(false);
            botonLimpiar.setDisable(false);
        }
        //Actualizar total
        double total = carrito.stream().mapToDouble(ItemCarrito::getSubtotal).sum();
        totalLabel.setText(String.format("$%.2f", total));
        //Mostrar/ocultar scrollbar si hay muchos items
        if (carrito.size() > 5) {
            deslizador.setVisible(true);
        } else {
            deslizador.setVisible(false);
        }
    }

    @FXML
    private void generarComprobante(ActionEvent event) {
        if (carrito.isEmpty()) {
            mostrarAlerta("Error", "El carrito está vacío", Alert.AlertType.WARNING);
            return;
        }
        //Crear mensaje de confirmacion
        StringBuilder mensaje = new StringBuilder("¿Confirmar venta?\n\n");
        double total = 0;
        for (ItemCarrito item : carrito) {
            mensaje.append(item.getProducto().getNombre())
                    .append(" x").append(item.getCantidad())
                    .append(" - $").append(String.format("%.2f", item.getSubtotal()))
                    .append("\n");
            total += item.getSubtotal();
        }
        mensaje.append("\nTotal: $").append(String.format("%.2f", total));
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Venta");
        confirmacion.setHeaderText("Resumen de la venta");
        confirmacion.setContentText(mensaje.toString());
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            //Registrar venta en BD
            if (registrarVenta()) {
                mostrarAlerta("Éxito", "Venta registrada correctamente", Alert.AlertType.INFORMATION);
                limpiarCarrito(null);
                cargarProductos(); //Recargar productos para actualizar stocks
            } else {
                mostrarAlerta("Error", "No se pudo registrar la venta", Alert.AlertType.ERROR);
            }
        }
    }

    private boolean registrarVenta() {
        try {
            Venta venta = new Venta();
            venta.setFecha(new Date());
            venta.setIdUsuario(idUsuarioActual);
            System.out.println(idUsuarioActual);
            //Calcular total y crear detalles
            double total = 0;
            List<DetalleVenta> detalles = new ArrayList<>();
            for (ItemCarrito item : carrito) {
                DetalleVenta detalle = new DetalleVenta(
                        item.getProducto().getIdProducto(),
                        item.getCantidad(),
                        item.getProducto().getPrecio(),
                        item.getSubtotal()
                );
                detalles.add(detalle);
                total += item.getSubtotal();
            }
            venta.setTotal(total);
            venta.setDetalles(detalles);
            VentaDao ventaDao = new VentaDao();
            return ventaDao.registrarVenta(venta);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    private void limpiarCarrito(ActionEvent event) {
        carrito.clear();
        actualizarCarrito();
    }

    @FXML
    void regresarMenu(ActionEvent event) {
        try {
            //Cargar el Dashboard en lugar de cerrar la ventana
            FXMLLoader loader = new FXMLLoader(getClass().getResource("UserDashboard.fxml"));
            Parent root = loader.load();
            Stage currentStage = (Stage) botonRegresar.getScene().getWindow();
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("ElectroStock - Panel Administrador");
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo regresar al menú", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void verRegistrosVenta(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("VerVenta.fxml"));
            Parent root = loader.load();
            Stage currentStage = (Stage) botonVer.getScene().getWindow();
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("ElectroStock - Registro de ventas");
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la ventana de registro de ventas", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void cambiosDevoluciones(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CambiosDevoluciones.fxml"));
            Parent root = loader.load();
            Stage currentStage = (Stage) botonVer.getScene().getWindow();
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("ElectroStock - Cambios y devoluciones");
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la ventana de cambios y devoluciones", Alert.AlertType.ERROR);
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
