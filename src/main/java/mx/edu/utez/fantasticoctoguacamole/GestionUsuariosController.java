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
import mx.edu.utez.fantasticoctoguacamole.modelo.dao.UsuarioDao;
import mx.edu.utez.fantasticoctoguacamole.modelo.Usuario;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
        filtros.setItems(FXCollections.observableArrayList("Todos", "Activo", "Inactivo", "Administrador", "Cajero"));
        filtros.getSelectionModel().selectFirst();
        //Configurar columnas
        configurarColumnas();
        //Cargar datos
        cargarDatos();
        //Configurar filtros y búsqueda
        configurarFiltrosYBusqueda();
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
            boolean coincideRol = true;
            boolean coincideBusqueda = true;
            //Filtrar por estado o rol
            if (filtros.getValue() != null && !filtros.getValue().equals("Todos")) {
                String filtroSeleccionado = filtros.getValue();
                switch (filtroSeleccionado) {
                    case "Activo":
                        coincideEstado = usuario.getEstado();
                        break;
                    case "Inactivo":
                        coincideEstado = !usuario.getEstado();
                        break;
                    case "Administrador":
                        coincideRol = usuario.getRol();
                        break;
                    case "Cajero":
                        coincideRol = !usuario.getRol();
                        break;
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
            return coincideEstado && coincideRol && coincideBusqueda;
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

    private void abrirVerUsuario(){
        try {
            Usuario usuarioSeleccionado = tablaUsuario.getSelectionModel().getSelectedItem();
            if (usuarioSeleccionado != null) {
                //Obtener el usuario COMPLETO desde la BD usando el ID
                UsuarioDao dao = new UsuarioDao();
                Usuario usuarioCompleto = dao.obtenerUsuarioPorId(usuarioSeleccionado.getIdUsuario());
                if (usuarioCompleto != null) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("VerUsuarios.fxml"));
                    Parent root = loader.load();

                    VerUsuariosController controller = loader.getController();
                    controller.setUsuario(usuarioCompleto); // Pasar el usuario completo

                    Stage stage = new Stage();
                    stage.setTitle("ElectroStock - Detalles de Usuario");
                    stage.setScene(new Scene(root));
                    stage.show();
                } else {
                    mostrarAlerta("Error", "No se pudieron cargar los detalles del usuario", Alert.AlertType.ERROR);
                }
            } else {
                mostrarAlerta("Error", "Selecciona un usuario para ver", Alert.AlertType.WARNING);
            }
        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo cargar los detalles del usuario", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void abrirEditarUsuario(){
        try {
            Usuario usuarioSeleccionado = tablaUsuario.getSelectionModel().getSelectedItem();
            if (usuarioSeleccionado != null) {
                //Obtener el usuario COMPLETO desde la BD
                UsuarioDao dao = new UsuarioDao();
                Usuario usuarioCompleto = dao.obtenerUsuarioPorId(usuarioSeleccionado.getIdUsuario());
                if (usuarioCompleto != null) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("EditarUsuarios.fxml"));
                    Parent root = loader.load();
                    EditarUsuariosController controller = loader.getController();
                    controller.setUsuario(usuarioCompleto);
                    controller.setOnUsuarioActualizado(() -> {
                        System.out.println("Actualizando tabla después de edición...");
                        recargarDatos(); //Recarga los usuarios desde la BD
                    });
                    Stage stage = new Stage();
                    stage.setTitle("ElectroStock - Editar Usuario");
                    stage.setScene(new Scene(root));
                    stage.show();
                } else {
                    mostrarAlerta("Error", "No se pudieron cargar los datos del usuario", Alert.AlertType.ERROR);
                }
            } else {
                mostrarAlerta("Error", "Selecciona un usuario para editar", Alert.AlertType.WARNING);
            }
        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo abrir la edición del usuario", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }


    @FXML
    private void cambiarEstadoUsuario(){
        Usuario usuarioSeleccionado = tablaUsuario.getSelectionModel().getSelectedItem();
        if (usuarioSeleccionado != null) {
            try {
                boolean nuevoEstado = !usuarioSeleccionado.getEstado();
                String mensaje = nuevoEstado ?
                        "¿Activar usuario " + usuarioSeleccionado.getNombre() + "?" :
                        "¿Desactivar usuario " + usuarioSeleccionado.getNombre() + "?";
                Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
                confirmacion.setTitle("Cambiar Estado");
                confirmacion.setHeaderText(mensaje);
                confirmacion.setContentText("Esta acción cambiará el estado del usuario.");
                Optional<ButtonType> resultado = confirmacion.showAndWait();
                if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                    UsuarioDao dao = new UsuarioDao();
                    if (dao.cambiarEstadoUsuario(usuarioSeleccionado.getIdUsuario(), nuevoEstado)) {
                        //Actualizar el objeto en la lista original
                        for (Usuario usuario : listaOriginal) {
                            if (usuario.getIdUsuario() == usuarioSeleccionado.getIdUsuario()) {
                                usuario.setEstado(nuevoEstado);
                                break;
                            }
                        }
                        mostrarAlerta("Éxito", "Estado del usuario actualizado correctamente", Alert.AlertType.INFORMATION);
                        tablaUsuario.refresh();
                    } else {
                        mostrarAlerta("Error", "No se pudo cambiar el estado del usuario", Alert.AlertType.ERROR);
                    }
                }
            } catch (Exception e) {
                mostrarAlerta("Error", "Error al cambiar estado: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        } else {
            mostrarAlerta("Error", "Selecciona un usuario para cambiar estado", Alert.AlertType.WARNING);
        }
    }

    private void recargarDatos() {
        try {
            UsuarioDao dao = new UsuarioDao();
            List<Usuario> nuevosDatos = dao.readUsuarios();
            //Guardar seleccion actual
            Usuario seleccionado = tablaUsuario.getSelectionModel().getSelectedItem();
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
    void agregarUsuario(ActionEvent event){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("RegistrarUsuario.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Registrar Usuarios");
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
