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
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import mx.edu.utez.fantasticoctoguacamole.modelo.dao.ProductoDao;
import mx.edu.utez.fantasticoctoguacamole.modelo.Producto;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class GestionProductosController implements Initializable {
    @FXML
    private TextField buscador;
    @FXML
    private Button botonAgregar;
    @FXML
    private ChoiceBox<String> filtros;
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
    private TableColumn<Producto, Boolean> tablaEstado;
    @FXML
    private TableColumn<Producto, String> tablaAcciones;
    @FXML
    private Button volverMenu;

    private ObservableList<Producto> listaOriginal;
    private FilteredList<Producto> datosFiltrados;
    private SortedList<Producto> datosOrdenados;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Configurar filtros (solo por estado como mencionaste)
        filtros.setItems(FXCollections.observableArrayList("Todos", "Activo", "Inactivo"));
        filtros.getSelectionModel().selectFirst();
        //Configurar columnas
        configurarColumnas();
        //Cargar datos
        cargarDatos();
        //Configurar filtros y búsqueda
        configurarFiltrosYBusqueda();
    }

    private void cargarDatos() {
        ProductoDao dao = new ProductoDao();
        List<Producto> datos = dao.readProductos();
        listaOriginal = FXCollections.observableArrayList(datos);
        //Inicializar FilteredList con todos los datos
        datosFiltrados = new FilteredList<>(listaOriginal, p -> true);
        //Envolver FilteredList en SortedList
        datosOrdenados = new SortedList<>(datosFiltrados);
        //Vincular SortedList con la tabla
        datosOrdenados.comparatorProperty().bind(tablaProducto.comparatorProperty());
        tablaProducto.setItems(datosOrdenados);
    }

    private void configurarFiltrosYBusqueda() {
        //Listener para el ChoiceBox
        filtros.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, nuevoFiltro) -> aplicarFiltros()
        );
        //Listener para el TextField
        buscador.textProperty().addListener((observable, oldValue, nuevoTexto) -> {
            aplicarFiltros();
        });
    }

    private void aplicarFiltros(){
        datosFiltrados.setPredicate(producto -> {
            if ((filtros.getValue() == null || filtros.getValue().equals("Todos")) &&
                    (buscador.getText() == null || buscador.getText().isEmpty())) {
                return true;
            }
            boolean coincideEstado = true;
            boolean coincideBusqueda = true;
            //Filtrar por estado
            if (filtros.getValue() != null && !filtros.getValue().equals("Todos")) {
                if (filtros.getValue().equals("Activo")) {
                    coincideEstado = producto.getEstado(); // true
                } else if (filtros.getValue().equals("Inactivo")) {
                    coincideEstado = !producto.getEstado(); // false
                }
            }
            //Filtrar por busqueda de texto (nombre, codigo o precio)
            if (buscador.getText() != null && !buscador.getText().isEmpty()) {
                String textoBusqueda = buscador.getText().toLowerCase();
                coincideBusqueda = producto.getNombre().toLowerCase().contains(textoBusqueda) ||
                        producto.getCodigo().toLowerCase().contains(textoBusqueda) ||
                        String.valueOf(producto.getPrecio()).contains(textoBusqueda);
            }
            return coincideEstado && coincideBusqueda;
        });
    }

    private void configurarColumnas() {
        tablaCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        tablaNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        tablaPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        tablaStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        tablaEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        configurarColumnaPrecio();
        configurarColumnaStock();
        configurarColumnaEstado();
        configurarColumnaAcciones();
    }

    private void configurarColumnaPrecio() {
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
    }

    private void configurarColumnaStock() {
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
                            //Definir colores por stock
                            if (stock < 5) {
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
    }

    private void configurarColumnaEstado() {
        tablaEstado.setCellFactory(new Callback<TableColumn<Producto, Boolean>, TableCell<Producto, Boolean>>() {
            @Override
            public TableCell<Producto, Boolean> call(TableColumn<Producto, Boolean> param) {
                return new TableCell<Producto, Boolean>() {
                    private final Label estadoLabel = new Label();
                    {
                        estadoLabel.setStyle("-fx-padding: 5px 10px; -fx-background-radius: 10px; -fx-text-fill: white; -fx-font-weight: bold;");
                    }
                    @Override
                    protected void updateItem(Boolean estado, boolean empty) {
                        super.updateItem(estado, empty);
                        if (empty || estado == null) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            if (estado) {
                                estadoLabel.setText("Activo");
                                estadoLabel.setStyle("-fx-background-color: #4CAF50; -fx-padding: 5px 10px; -fx-background-radius: 10px; -fx-text-fill: white; -fx-font-weight: bold;");
                            } else {
                                estadoLabel.setText("Inactivo");
                                estadoLabel.setStyle("-fx-background-color: #F44336; -fx-padding: 5px 10px; -fx-background-radius: 10px; -fx-text-fill: white; -fx-font-weight: bold;");
                            }
                            setGraphic(estadoLabel);
                        }
                    }
                };
            }
        });
    }

    private void configurarColumnaAcciones() {
        tablaAcciones.setCellFactory(new Callback<TableColumn<Producto, String>, TableCell<Producto, String>>() {
            @Override
            public TableCell<Producto, String> call(TableColumn<Producto, String> param) {
                return new TableCell<Producto, String>() {
                    private final HBox botonesContainer = new HBox(5);
                    private final Button btnEditar = new Button("✏");
                    private final Button btnCambiar = new Button("🔁");
                    private final Button btnVer = new Button("[👁]");
                    {
                        botonesContainer.getChildren().addAll(btnEditar, btnCambiar, btnVer);
                        //Agregar eventos a los botones
                        btnEditar.setOnAction(event -> abrirEditarProducto());
                        btnVer.setOnAction(event -> abrirVerProducto());
                        btnCambiar.setOnAction(event -> cambiarEstadoProducto());
                    }
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(botonesContainer);
                        }
                    }
                };
            }
        });
    }

    private void abrirVerProducto(){
        try {
            Producto productoSeleccionado = tablaProducto.getSelectionModel().getSelectedItem();
            if (productoSeleccionado != null) {
                //Obtener el producto COMPLETO desde la BD usando el ID
                ProductoDao dao = new ProductoDao();
                Producto productoCompleto = dao.obtenerProductoPorId(productoSeleccionado.getIdProducto());
                if (productoCompleto != null) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("VerProducto.fxml"));
                    Parent root = loader.load();
                    VerProductosController controller = loader.getController();
                    controller.setProducto(productoCompleto); //Pasar el producto completo
                    Stage stage = new Stage();
                    stage.setTitle("ElectroStock - Detalles de Producto");
                    stage.setScene(new Scene(root));
                    stage.show();
                } else {
                    mostrarAlerta("Error", "No se pudieron cargar los detalles del producto", Alert.AlertType.ERROR);
                }
            } else {
                mostrarAlerta("Error", "Selecciona un producto para ver", Alert.AlertType.WARNING);
            }
        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo cargar los detalles del producto", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void abrirEditarProducto(){
        try {
            Producto productoSeleccionado = tablaProducto.getSelectionModel().getSelectedItem();
            if (productoSeleccionado != null) {
                //Obtener el producto COMPLETO desde la BD
                ProductoDao dao = new ProductoDao();
                Producto productoCompleto = dao.obtenerProductoPorId(productoSeleccionado.getIdProducto());
                if (productoCompleto != null) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("EditarProducto.fxml"));
                    Parent root = loader.load();
                    EditarProductosController controller = loader.getController();
                    controller.setProducto(productoCompleto);
                    controller.setOnProductoActualizado(() -> {
                        System.out.println("Actualizando tabla después de edición...");
                        recargarDatos(); //Recarga los productos desde la BD
                    });
                    Stage stage = new Stage();
                    stage.setTitle("ElectroStock - Editar Producto");
                    stage.setScene(new Scene(root));
                    stage.show();
                } else {
                    mostrarAlerta("Error", "No se pudieron cargar los datos del producto", Alert.AlertType.ERROR);
                }
            } else {
                mostrarAlerta("Error", "Selecciona un producto para editar", Alert.AlertType.WARNING);
            }
        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo abrir la edición del producto", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void cambiarEstadoProducto(){
        Producto productoSeleccionado = tablaProducto.getSelectionModel().getSelectedItem();
        if (productoSeleccionado != null) {
            try {
                boolean nuevoEstado = !productoSeleccionado.getEstado();
                String mensaje = nuevoEstado ?
                        "¿Activar producto " + productoSeleccionado.getNombre() + "?" :
                        "¿Desactivar producto " + productoSeleccionado.getNombre() + "?";
                Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
                confirmacion.setTitle("Cambiar Estado");
                confirmacion.setHeaderText(mensaje);
                confirmacion.setContentText("Esta acción cambiará el estado del producto.");
                Optional<ButtonType> resultado = confirmacion.showAndWait();
                if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                    ProductoDao dao = new ProductoDao();
                    if (dao.cambiarEstadoProducto(productoSeleccionado.getIdProducto(), nuevoEstado)) {
                        //Actualizar el objeto en la lista original
                        for (Producto producto : listaOriginal) {
                            if (producto.getIdProducto() == productoSeleccionado.getIdProducto()) {
                                producto.setEstado(nuevoEstado);
                                break;
                            }
                        }
                        mostrarAlerta("Éxito", "Estado del producto actualizado correctamente", Alert.AlertType.INFORMATION);
                        tablaProducto.refresh();
                    } else {
                        mostrarAlerta("Error", "No se pudo cambiar el estado del producto", Alert.AlertType.ERROR);
                    }
                }
            } catch (Exception e) {
                mostrarAlerta("Error", "Error al cambiar estado: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        } else {
            mostrarAlerta("Error", "Selecciona un producto para cambiar estado", Alert.AlertType.WARNING);
        }
    }

    private void recargarDatos() {
        try {
            ProductoDao dao = new ProductoDao();
            List<Producto> nuevosDatos = dao.readProductos();
            //Guardar seleccion actual
            Producto seleccionado = tablaProducto.getSelectionModel().getSelectedItem();
            //Limpiar y actualizar la lista original
            listaOriginal.clear();
            listaOriginal.addAll(nuevosDatos);
            //Los filtros se aplicaran automaticamente por los listeners
            System.out.println("Datos recargados. Registros: " + listaOriginal.size());
        } catch (Exception e) {
            System.out.println("Error al recargar datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    void agregarProducto(ActionEvent event){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("RegistrarProducto.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Registrar Producto");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            //Recargar datos manteniendo la estructura de filtros
            recargarDatos();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la ventana de registro", Alert.AlertType.ERROR);
        }
    }

    @FXML
    void regresarMenu(ActionEvent event) {
        Stage currentStage = (Stage) volverMenu.getScene().getWindow();
        currentStage.close();
    }
}