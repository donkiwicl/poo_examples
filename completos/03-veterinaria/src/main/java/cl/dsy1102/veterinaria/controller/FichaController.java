package cl.dsy1102.veterinaria.controller;

import cl.dsy1102.veterinaria.Navegador;
import cl.dsy1102.veterinaria.dao.PersistenciaException;
import cl.dsy1102.veterinaria.model.Atencion;
import cl.dsy1102.veterinaria.model.AtencionRechazadaException;
import cl.dsy1102.veterinaria.model.Paciente;
import cl.dsy1102.veterinaria.model.TipoAtencion;
import cl.dsy1102.veterinaria.repository.Repository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.converter.LocalDateStringConverter;

import java.time.LocalDate;

/**
 * Ficha clinica (maestro-detalle): datos del paciente arriba, su historial
 * en una tabla y un formulario para registrar una nueva atencion.
 */
public class FichaController {

    @FXML private Label lblTitulo;
    @FXML private Label lblDetalle;
    @FXML private TableView<Atencion> tblAtenciones;
    @FXML private TableColumn<Atencion, LocalDate> colFecha;
    @FXML private TableColumn<Atencion, TipoAtencion> colTipo;
    @FXML private TableColumn<Atencion, Double> colPeso;
    @FXML private TableColumn<Atencion, Integer> colCosto;
    @FXML private TableColumn<Atencion, String> colObservacion;
    @FXML private Label lblResumen;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<TipoAtencion> cmbTipoAtencion;
    @FXML private TextField txtPeso;
    @FXML private TextField txtObservacion;
    @FXML private Label lblCosto;
    @FXML private Label lblResultado;

    private final ObservableList<Atencion> historial = FXCollections.observableArrayList();
    private Repository<Paciente> repositorio;
    private Paciente paciente;

    @FXML
    private void initialize() {
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colPeso.setCellValueFactory(new PropertyValueFactory<>("pesoKg"));
        colCosto.setCellValueFactory(new PropertyValueFactory<>("costo"));
        colObservacion.setCellValueFactory(new PropertyValueFactory<>("observacion"));
        colFecha.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate fecha, boolean vacia) {
                super.updateItem(fecha, vacia);
                setText(vacia || fecha == null ? null : fecha.format(Atencion.FORMATO_FECHA));
            }
        });
        colCosto.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(Integer costo, boolean vacia) {
                super.updateItem(costo, vacia);
                setText(vacia || costo == null ? null : Paciente.pesos(costo));
            }
        });
        tblAtenciones.setItems(historial);
        // Lo mas reciente primero.
        colFecha.setSortType(TableColumn.SortType.DESCENDING);
        tblAtenciones.getSortOrder().add(colFecha);

        cmbTipoAtencion.getItems().setAll(TipoAtencion.values());
        dpFecha.setValue(LocalDate.now());
        // Mismo formato dd-MM-yyyy que el resto de la aplicacion (por defecto depende del sistema).
        dpFecha.setConverter(new LocalDateStringConverter(Atencion.FORMATO_FECHA, Atencion.FORMATO_FECHA));
        dpFecha.setDayCellFactory(selector -> new DateCell() {
            @Override
            public void updateItem(LocalDate fecha, boolean vacia) {
                super.updateItem(fecha, vacia);
                setDisable(vacia || fecha.isAfter(LocalDate.now()));
            }
        });
        // Costo estimado segun el tipo elegido (lo calcula el paciente: polimorfismo).
        cmbTipoAtencion.valueProperty().addListener((obs, antes, tipo) -> lblCosto.setText(
                tipo == null || paciente == null ? "" : "Costo: " + Paciente.pesos(paciente.calcularCosto(tipo))));
    }

    public void inicializar(Repository<Paciente> repositorio, Paciente paciente) {
        this.repositorio = repositorio;
        mostrar(paciente);
        // Sugerencia: el ultimo peso registrado.
        if (paciente.getPesoActual() > 0) {
            txtPeso.setText(String.valueOf(paciente.getPesoActual()));
        }
    }

    private void mostrar(Paciente paciente) {
        this.paciente = paciente;
        lblTitulo.setText("Ficha clínica de " + paciente.getNombre());
        lblDetalle.setText(paciente.obtenerDetalle());
        historial.setAll(paciente.getAtenciones());
        tblAtenciones.sort();
        lblResumen.setText("Atenciones: " + paciente.getAtenciones().size() + " · Total: "
                + Paciente.pesos(paciente.getTotalAtenciones()));
    }

    @FXML
    private void onRegistrar() {
        double peso;
        try {
            peso = Double.parseDouble(txtPeso.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            mostrarResultado("Ingresa el peso en kg (ejemplo: 4,2).", false);
            return;
        }
        // Se registra en una copia; el original se reemplaza solo si el cambio se guardo.
        Paciente copia = paciente.copiar();
        try {
            String mensaje = copia.registrarAtencion(dpFecha.getValue(), cmbTipoAtencion.getValue(), peso, txtObservacion.getText());
            repositorio.actualizar(paciente, copia);
            mostrar(copia);
            txtObservacion.clear();
            mostrarResultado(mensaje, true);
        } catch (AtencionRechazadaException e) {
            mostrarResultado("Atención rechazada: " + e.getMessage(), false);
        } catch (IllegalArgumentException e) {
            mostrarResultado(e.getMessage(), false);
        } catch (PersistenciaException e) {
            Alertas.error("No se pudo registrar la atención", e.getMessage());
        }
    }

    @FXML
    private void onVolver() {
        PrincipalController principal = Navegador.navegar("principal-view.fxml", "Clínica Veterinaria Patitas del Sur");
        principal.inicializar(repositorio);
    }

    private void mostrarResultado(String texto, boolean exito) {
        lblResultado.setText(texto);
        lblResultado.getStyleClass().setAll("label", "resultado", exito ? "resultado-ok" : "resultado-error");
    }
}
