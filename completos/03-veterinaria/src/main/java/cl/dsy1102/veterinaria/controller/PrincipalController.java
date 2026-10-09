package cl.dsy1102.veterinaria.controller;

import cl.dsy1102.veterinaria.Navegador;
import cl.dsy1102.veterinaria.dao.PersistenciaException;
import cl.dsy1102.veterinaria.model.Atencion;
import cl.dsy1102.veterinaria.model.Exotico;
import cl.dsy1102.veterinaria.model.Paciente;
import cl.dsy1102.veterinaria.repository.Repository;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;

/**
 * Vista principal: pacientes con busqueda, filtro por especie y acciones.
 */
public class PrincipalController {

    private static final String TODAS = "Todas";
    private static final String EXOTICOS = "Exóticos";

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbEspecie;
    @FXML private TableView<Paciente> tblPacientes;
    @FXML private TableColumn<Paciente, String> colEspecie;
    @FXML private TableColumn<Paciente, String> colNombre;
    @FXML private TableColumn<Paciente, String> colTutor;
    @FXML private TableColumn<Paciente, Integer> colEdad;
    @FXML private TableColumn<Paciente, LocalDate> colUltimaAtencion;
    @FXML private TableColumn<Paciente, Integer> colTotal;
    @FXML private Label lblTotal;

    private Repository<Paciente> repositorio;
    private FilteredList<Paciente> filtrados;

    @FXML
    private void initialize() {
        cmbEspecie.getItems().setAll(TODAS, "Perro", "Gato", EXOTICOS);
        cmbEspecie.setValue(TODAS);

        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTutor.setCellValueFactory(new PropertyValueFactory<>("tutor"));
        colEdad.setCellValueFactory(new PropertyValueFactory<>("edadAnios"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAtenciones"));
        colEspecie.setCellValueFactory(celda -> new ReadOnlyStringWrapper(celda.getValue().obtenerEspecie()));
        // El valor es la fecha (ordena cronologicamente); el texto se formatea en la celda.
        colUltimaAtencion.setCellValueFactory(celda -> {
            Atencion ultima = celda.getValue().getUltimaAtencion();
            return new ReadOnlyObjectWrapper<>(ultima == null ? null : ultima.getFecha());
        });
        colUltimaAtencion.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate fecha, boolean vacia) {
                super.updateItem(fecha, vacia);
                setText(vacia ? null : fecha == null ? "Sin atenciones" : fecha.format(Atencion.FORMATO_FECHA));
            }
        });
        colTotal.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(Integer total, boolean vacia) {
                super.updateItem(total, vacia);
                setText(vacia || total == null ? null : Paciente.pesos(total));
            }
        });

        // Doble clic abre la ficha clinica.
        tblPacientes.setRowFactory(tabla -> {
            TableRow<Paciente> fila = new TableRow<>();
            fila.setOnMouseClicked(evento -> {
                if (evento.getClickCount() == 2 && !fila.isEmpty()) {
                    abrirFicha(fila.getItem());
                }
            });
            return fila;
        });
    }

    public void inicializar(Repository<Paciente> repositorio) {
        this.repositorio = repositorio;

        filtrados = new FilteredList<>(repositorio.listar(), paciente -> true);
        txtBuscar.textProperty().addListener((obs, antes, texto) -> actualizarFiltro());
        cmbEspecie.valueProperty().addListener((obs, antes, especie) -> actualizarFiltro());

        SortedList<Paciente> ordenados = new SortedList<>(filtrados);
        ordenados.comparatorProperty().bind(tblPacientes.comparatorProperty());
        tblPacientes.setItems(ordenados);

        lblTotal.textProperty().bind(Bindings.format("%d de %d pacientes",
                Bindings.size(filtrados), Bindings.size(repositorio.listar())));
    }

    @FXML
    private void onNuevo() {
        abrirFormulario(null);
    }

    @FXML
    private void onEditar() {
        Paciente seleccionado = obtenerSeleccion("editar");
        if (seleccionado != null) {
            abrirFormulario(seleccionado);
        }
    }

    @FXML
    private void onEliminar() {
        Paciente seleccionado = obtenerSeleccion("eliminar");
        if (seleccionado == null) {
            return;
        }
        int atenciones = seleccionado.getAtenciones().size();
        String aviso = atenciones == 0 ? "" : "\nTambién se eliminará su historial (" + atenciones + " atenciones).";
        if (!Alertas.confirmar("Eliminar paciente", "Se eliminará a " + seleccionado.getNombre()
                + " (" + seleccionado.getTutor() + ")." + aviso + "\n¿Continuar?")) {
            return;
        }
        try {
            repositorio.eliminar(seleccionado);
        } catch (PersistenciaException e) {
            Alertas.error("No se pudo eliminar el paciente", e.getMessage());
        }
    }

    @FXML
    private void onFicha() {
        Paciente seleccionado = obtenerSeleccion("ver su ficha clínica");
        if (seleccionado != null) {
            abrirFicha(seleccionado);
        }
    }

    private void abrirFicha(Paciente paciente) {
        FichaController ficha = Navegador.navegar("ficha-view.fxml", "Ficha clínica - " + paciente.getNombre());
        ficha.inicializar(repositorio, paciente);
    }

    private void abrirFormulario(Paciente paciente) {
        String titulo = paciente == null ? "Nuevo paciente" : "Editar " + paciente.getNombre();
        FormularioController formulario = Navegador.navegar("formulario-view.fxml", titulo);
        formulario.inicializar(repositorio, paciente);
    }

    private Paciente obtenerSeleccion(String accion) {
        Paciente seleccionado = tblPacientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            Alertas.advertencia("Ningún paciente seleccionado", "Selecciona una fila de la tabla para " + accion + ".");
        }
        return seleccionado;
    }

    private void actualizarFiltro() {
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        String especie = cmbEspecie.getValue();
        filtrados.setPredicate(paciente ->
                (paciente.getNombre().toLowerCase().contains(texto) || paciente.getTutor().toLowerCase().contains(texto))
                        && coincideEspecie(paciente, especie));
    }

    private static boolean coincideEspecie(Paciente paciente, String especie) {
        if (especie == null || TODAS.equals(especie)) {
            return true;
        }
        // Los exoticos tienen varias especies (Conejo, Huron, Ave): se agrupan por su clase.
        return EXOTICOS.equals(especie) ? paciente instanceof Exotico : paciente.obtenerEspecie().equals(especie);
    }
}
