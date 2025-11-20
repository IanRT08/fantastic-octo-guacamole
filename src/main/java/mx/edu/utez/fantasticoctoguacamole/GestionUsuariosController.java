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
import mx.edu.utez.fantasticoctoguacamole.modelo.UsuarioDao;
import mx.edu.utez.fantasticoctoguacamole.modelo.dao.Usuario;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class GestionUsuariosController implements Initializable {
    @FXML
    private TextField buscador;
    @FXML
    private Button botonAgregar;
    @FXML
    private ChoiceBox<String> filtros;
    @FXML
    private TableView<Usuario> tablaUsuario;
    @FXML
    private TableColumn<Usuario,String> tablaNombre;
    @FXML
    private TableColumn<Usuario,String> tablaPaterno;
    @FXML
    private TableColumn<Usuario,String> tablaMaterno;
    @FXML
    private TableColumn<Usuario,Boolean> tablaRol;
    @FXML
    private TableColumn<Usuario,Boolean> tablaEstado;
    @FXML
    private TableColumn<Usuario, String> tablaAcciones;
    @FXML
    private Button volverMenu;

    private ObservableList<Usuario> listaOriginal;
    private FilteredList<Usuario> datosFiltrados;
    private SortedList<Usuario> datosOrdenados;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Configurar filtros
        filtros.setItems(FXCollections.observableArrayList("Todos", "Activo", "Inactivo"));
        filtros.getSelectionModel().selectFirst();
        //Cargar datos
        cargarDatos();
        //Configurar filtros y búsqueda
        configurarFiltrosYBusqueda();
        //Configurar columnas
        configurarColumnas();
        configurarColumnaRol();
        configurarColumnaEstado();
        configurarColumnaAcciones();
    }

    private void cargarDatos() {
        UsuarioDao dao = new UsuarioDao();
        List<Usuario> datos = dao.readUsuarios();
        listaOriginal = FXCollections.observableArrayList(datos);
        //Inicializar FilteredList con todos los datos
        datosFiltrados = new FilteredList<>(listaOriginal, p -> true);
        //Envolver FilteredList en SortedList
        datosOrdenados = new SortedList<>(datosFiltrados);
        //Vincular SortedList con la tabla
        datosOrdenados.comparatorProperty().bind(tablaUsuario.comparatorProperty());
        tablaUsuario.setItems(datosOrdenados);
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
        datosFiltrados.setPredicate(usuario -> {
            if ((filtros.getValue() == null || filtros.getValue().equals("Todos")) &&
                    (buscador.getText() == null || buscador.getText().isEmpty())) {
                return true;
            }
            boolean coincideEstado = true;
            boolean coincideBusqueda = true;
            //Filtrar por estado
            if (filtros.getValue() != null && !filtros.getValue().equals("Todos")) {
                if (filtros.getValue().equals("Activo")) {
                    coincideEstado = usuario.getEstado(); //true
                } else if (filtros.getValue().equals("Inactivo")) {
                    coincideEstado = !usuario.getEstado(); //false
                }
            }
            //Filtrar por busqueda de texto
            if (buscador.getText() != null && !buscador.getText().isEmpty()) {
                String textoBusqueda = buscador.getText().toLowerCase();
                //Convertir la fecha a String en formato dd/mm/yyyy
                String fechaFormatoLocal = "";
                if (usuario.getFechaNacimiento() != null) {
                    LocalDate fechaLocal = usuario.getFechaNacimiento().toLocalDate();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    fechaFormatoLocal = fechaLocal.format(formatter).toLowerCase();
                }
                coincideBusqueda = usuario.getNombre().toLowerCase().contains(textoBusqueda) ||
                        usuario.getApellidoPaterno().toLowerCase().contains(textoBusqueda) ||
                        fechaFormatoLocal.contains(textoBusqueda) ||
                        usuario.getApellidoMaterno().toLowerCase().contains(textoBusqueda);
            }
            return coincideEstado && coincideBusqueda;
        });
    }

    private void configurarColumnas() {
        tablaNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        tablaPaterno.setCellValueFactory(new PropertyValueFactory<>("apellidoPaterno"));
        tablaMaterno.setCellValueFactory(new PropertyValueFactory<>("apellidoMaterno"));
        tablaRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        tablaEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        configurarColumnaRol();
        configurarColumnaEstado();
        configurarColumnaAcciones();
    }

    private void configurarColumnaRol() {
        tablaRol.setCellFactory(new Callback<TableColumn<Usuario, Boolean>, TableCell<Usuario, Boolean>>() {
            @Override
            public TableCell<Usuario, Boolean> call(TableColumn<Usuario, Boolean> param) {
                return new TableCell<Usuario, Boolean>() {
                    private final Label rolLabel = new Label();
                    {
                        rolLabel.setStyle("-fx-padding: 5px 10px; -fx-background-radius: 10px; -fx-text-fill: white; -fx-font-weight: bold;");
                    }
                    @Override
                    protected void updateItem(Boolean rol, boolean empty) {
                        super.updateItem(rol, empty);
                        if (empty || rol == null) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            if (rol) {
                                rolLabel.setText("Administrador");
                                rolLabel.setStyle("-fx-background-color: #1f2dc1; -fx-padding: 5px 10px; -fx-background-radius: 10px; -fx-text-fill: white; -fx-font-weight: bold;");
                            } else {
                                rolLabel.setText("Cajero");
                                rolLabel.setStyle("-fx-background-color: #686666; -fx-padding: 5px 10px; -fx-background-radius: 10px; -fx-text-fill: white; -fx-font-weight: bold;");
                            }
                            setGraphic(rolLabel);
                        }
                    }
                };
            }
        });
    }

    private void configurarColumnaEstado() {
        tablaEstado.setCellFactory(new Callback<TableColumn<Usuario, Boolean>, TableCell<Usuario, Boolean>>() {
            @Override
            public TableCell<Usuario, Boolean> call(TableColumn<Usuario, Boolean> param) {
                return new TableCell<Usuario, Boolean>() {
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
        tablaAcciones.setCellFactory(new Callback<TableColumn<Usuario, String>, TableCell<Usuario, String>>() {
            @Override
            public TableCell<Usuario, String> call(TableColumn<Usuario, String> param) {
                return new TableCell<Usuario, String>() {
                    private final HBox botonesContainer = new HBox(5);
                    private final Button btnEditar = new Button("✏");
                    private final Button btnCambiar = new Button("🔁");
                    private final Button btnVer = new Button("[👁]");
                    {
                        botonesContainer.getChildren().addAll(btnEditar, btnCambiar, btnVer);
                        //Agregar eventos a los botones
                        btnEditar.setOnAction(event -> abrirEditarUsuario());
                        btnVer.setOnAction(event -> abrirVerUsuario());
                        btnCambiar.setOnAction(event -> cambiarEstadoUsuario());
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

    private void abrirEditarUsuario() {
        try {
            Usuario usuarioSeleccionado = tablaUsuario.getSelectionModel().getSelectedItem();
            if (usuarioSeleccionado != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("EditarUsuario.fxml"));
                Parent root = loader.load();
                //Pasar el usuario al controlador de edición
                EditarUsuariosController controller = loader.getController();
                controller.setUsuario(usuarioSeleccionado);
                Stage stage = new Stage();
                stage.setTitle("Editar Usuario");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();
                //Recargar datos después de cerrar la ventana de edición
                recargarDatos();
                aplicarFiltros();
            } else {
                mostrarAlerta("Error", "Selecciona un usuario para editar", Alert.AlertType.WARNING);
            }
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la ventana de edición", Alert.AlertType.ERROR);
        }
    }

    private void cambiarEstadoUsuario() {
        Usuario usuarioSeleccionado = tablaUsuario.getSelectionModel().getSelectedItem();
        if (usuarioSeleccionado != null) {
            String nuevoEstado = usuarioSeleccionado.getEstado() ? "inactivo" : "activo";
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Cambiar Estado");
            confirmacion.setHeaderText("¿Estás seguro?");
            confirmacion.setContentText("El usuario cambiará a estado: " + nuevoEstado);
            Optional<ButtonType> resultado = confirmacion.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                //Cambiar el estado
                Boolean nuevoEstadoBoolean = !usuarioSeleccionado.getEstado();
                usuarioSeleccionado.setEstado(nuevoEstadoBoolean);
                //Actualizar en la base de datos
                UsuarioDao dao = new UsuarioDao();
                if (dao.updateUsuario(usuarioSeleccionado)) {
                    //Forzar recreacion de la lista
                    ObservableList<Usuario> listaTemporal = FXCollections.observableArrayList(listaOriginal);
                    listaOriginal.setAll(listaTemporal);
                    aplicarFiltros();
                    mostrarAlerta("Éxito", "Estado cambiado correctamente", Alert.AlertType.INFORMATION);
                } else {
                    mostrarAlerta("Error", "No se pudo cambiar el estado", Alert.AlertType.ERROR);
                }
            }
        } else {
            mostrarAlerta("Error", "Selecciona un usuario para cambiar estado", Alert.AlertType.WARNING);
        }
    }

    private void recargarDatos() {
        try {
            UsuarioDao dao = new UsuarioDao();
            List<Usuario> nuevosDatos = dao.readUsuarios();
            String filtroEstado = filtros.getValue();
            String textoBusqueda = buscador.getText();
            Usuario seleccionado = tablaUsuario.getSelectionModel().getSelectedItem();
            int indiceSeleccionado = tablaUsuario.getSelectionModel().getSelectedIndex();
            listaOriginal.clear();
            listaOriginal.addAll(nuevosDatos);
            aplicarFiltros();
            if (seleccionado != null) {
                Optional<Usuario> usuarioActualizado = listaOriginal.stream()
                        .filter(u -> u.getIdUsuario() == seleccionado.getIdUsuario())
                        .findFirst();
                if (usuarioActualizado.isPresent()) {
                    tablaUsuario.getSelectionModel().select(usuarioActualizado.get());
                }
            }
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
    void agregarUsuario(ActionEvent event){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("RegistrarUsuario.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Registrar Usuarios");
            stage.initModality(Modality.APPLICATION_MODAL); //Espera a que la ventana se cierre
            stage.showAndWait();
            //Una vez cerrada la ventana de registro, actualizamos la tabla
            UsuarioDao dao = new UsuarioDao();
            List<Usuario> lista = dao.readUsuarios();
            tablaUsuario.setItems(FXCollections.observableArrayList(lista));
            tablaUsuario.refresh();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void regresarMenu(ActionEvent event) {
        Stage currentStage = (Stage) volverMenu.getScene().getWindow();
        currentStage.close();
    }

}
