package cl.dsy1102.ejemplos.lavanderia.controller;

import cl.dsy1102.ejemplos.lavanderia.dao.ClienteDao;
import cl.dsy1102.ejemplos.lavanderia.dao.DaoException;
import cl.dsy1102.ejemplos.lavanderia.model.Cliente;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador de la vista de clientes. Solo se ocupa de la interfaz: los
 * datos los pide y los guarda a través de ClienteDao, sin conocer SQL.
 */
public class ClienteController {

    private static final String PATRON_TELEFONO = "\\d{9}";
    private static final String PATRON_CORREO = "[^@\\s]+@[^@\\s]+\\.[A-Za-z]{2,}";

    @FXML private TableView<Cliente> tblClientes;
    @FXML private TableColumn<Cliente, Integer> colId;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, String> colTelefono;
    @FXML private TableColumn<Cliente, String> colCorreo;
    @FXML private TableColumn<Cliente, String> colComuna;
    @FXML private TextField txtBuscar;
    @FXML private TextField txtNombre;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtComuna;
    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;
    @FXML private Label lblEstado;

    private final ObservableList<Cliente> clientes = FXCollections.observableArrayList();
    private ClienteDao dao;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));
        colComuna.setCellValueFactory(new PropertyValueFactory<>("comuna"));
        tblClientes.setItems(clientes);

        // Al seleccionar una fila, sus datos pasan al formulario para editarlos.
        tblClientes.getSelectionModel().selectedItemProperty().addListener((obs, anterior, actual) -> {
            if (actual != null) {
                mostrarEnFormulario(actual);
            }
        });
        btnActualizar.disableProperty().bind(tblClientes.getSelectionModel().selectedItemProperty().isNull());
        btnEliminar.disableProperty().bind(tblClientes.getSelectionModel().selectedItemProperty().isNull());
    }

    /**
     * Recibe el DAO desde AppLavanderia (inyección de dependencias) y carga
     * los datos. Se llama después de initialize(), cuando el DAO ya existe.
     */
    public void inicializar(ClienteDao dao) {
        this.dao = dao;
        recargar();
    }

    /** Vuelve a leer desde la base de datos, respetando la búsqueda actual. */
    private void recargar() {
        String texto = txtBuscar.getText().trim();
        try {
            clientes.setAll(texto.isEmpty() ? dao.listarTodos() : dao.buscarPorNombre(texto));
            String cantidad = clientes.size() == 1 ? "1 cliente" : clientes.size() + " clientes";
            mostrarEstado(texto.isEmpty() ? cantidad : cantidad + " con \"" + texto + "\" en el nombre", false);
        } catch (DaoException e) {
            clientes.clear();
            mostrarEstado("Sin conexión con la base de datos. Pulsa Recargar para reintentar.", true);
            Alertas.error("No se pudieron cargar los clientes", e.getMessage());
        }
    }

    @FXML
    private void onRecargar() {
        txtBuscar.clear();
        recargar();
    }

    @FXML
    private void onBuscar() {
        recargar();
    }

    @FXML
    private void onGuardar() {
        if (!formularioValido()) {
            return;
        }
        Cliente nuevo = leerFormulario();
        try {
            dao.insertar(nuevo);
            recargar();
            seleccionar(nuevo.getId());
            mostrarEstado("Cliente " + nuevo.getNombre() + " registrado con el id " + nuevo.getId() + ".", false);
        } catch (DaoException e) {
            Alertas.error("No se pudo guardar el cliente", e.getMessage());
        }
    }

    @FXML
    private void onActualizar() {
        Cliente seleccionado = tblClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null || !formularioValido()) {
            return;
        }
        Cliente editado = leerFormulario();
        editado.setId(seleccionado.getId());
        try {
            boolean existia = dao.actualizar(editado);
            recargar();
            if (existia) {
                seleccionar(editado.getId());
                mostrarEstado("Cliente " + editado.getNombre() + " actualizado.", false);
            } else {
                Alertas.advertencia("El cliente ya no existe", "Otro usuario lo eliminó. La lista se recargó.");
            }
        } catch (DaoException e) {
            Alertas.error("No se pudo actualizar el cliente", e.getMessage());
        }
    }

    @FXML
    private void onEliminar() {
        Cliente seleccionado = tblClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null
                || !Alertas.confirmar("¿Eliminar a " + seleccionado.getNombre() + "?", "Esta acción no se puede deshacer.")) {
            return;
        }
        try {
            dao.eliminar(seleccionado.getId());
            onLimpiar();
            recargar();
            mostrarEstado("Cliente " + seleccionado.getNombre() + " eliminado.", false);
        } catch (DaoException e) {
            Alertas.error("No se pudo eliminar el cliente", e.getMessage());
        }
    }

    @FXML
    private void onLimpiar() {
        txtNombre.clear();
        txtTelefono.clear();
        txtCorreo.clear();
        txtComuna.clear();
        quitarMarcas();
        tblClientes.getSelectionModel().clearSelection();
    }

    private void mostrarEnFormulario(Cliente cliente) {
        txtNombre.setText(cliente.getNombre());
        txtTelefono.setText(cliente.getTelefono());
        txtCorreo.setText(cliente.getCorreo());
        txtComuna.setText(cliente.getComuna());
        quitarMarcas();
    }

    /** Crea un cliente NUEVO con los datos del formulario (no modifica el de la tabla). */
    private Cliente leerFormulario() {
        return new Cliente(0, txtNombre.getText().trim(), txtTelefono.getText().trim(),
                txtCorreo.getText().trim().toLowerCase(), txtComuna.getText().trim());
    }

    /** Valida antes de llamar al DAO y muestra todos los errores juntos. */
    private boolean formularioValido() {
        quitarMarcas();
        List<String> errores = new ArrayList<>();
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty() || nombre.length() > 60) {
            errores.add("El nombre es obligatorio (hasta 60 caracteres).");
            marcar(txtNombre);
        }
        if (!txtTelefono.getText().trim().matches(PATRON_TELEFONO)) {
            errores.add("El teléfono debe tener 9 dígitos, por ejemplo 912345678.");
            marcar(txtTelefono);
        }
        if (!txtCorreo.getText().trim().matches(PATRON_CORREO)) {
            errores.add("El correo no tiene un formato válido.");
            marcar(txtCorreo);
        }
        if (txtComuna.getText().isBlank()) {
            errores.add("La comuna es obligatoria.");
            marcar(txtComuna);
        }
        if (!errores.isEmpty()) {
            Alertas.advertencia("Revisa los datos del cliente", String.join("\n", errores));
        }
        return errores.isEmpty();
    }

    private void seleccionar(int id) {
        clientes.stream().filter(c -> c.getId() == id).findFirst().ifPresent(c -> {
            tblClientes.getSelectionModel().select(c);
            tblClientes.scrollTo(c);
        });
    }

    private void mostrarEstado(String texto, boolean error) {
        lblEstado.setText(texto);
        lblEstado.getStyleClass().remove("estado-error");
        if (error) {
            lblEstado.getStyleClass().add("estado-error");
        }
    }

    private void marcar(TextField campo) {
        campo.getStyleClass().add("campo-error");
    }

    private void quitarMarcas() {
        for (TextField campo : List.of(txtNombre, txtTelefono, txtCorreo, txtComuna)) {
            campo.getStyleClass().remove("campo-error");
        }
    }
}
