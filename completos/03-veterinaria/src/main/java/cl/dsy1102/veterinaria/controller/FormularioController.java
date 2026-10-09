package cl.dsy1102.veterinaria.controller;

import cl.dsy1102.veterinaria.Navegador;
import cl.dsy1102.veterinaria.dao.PersistenciaException;
import cl.dsy1102.veterinaria.model.Atencion;
import cl.dsy1102.veterinaria.model.Exotico;
import cl.dsy1102.veterinaria.model.Gato;
import cl.dsy1102.veterinaria.model.Paciente;
import cl.dsy1102.veterinaria.model.Perro;
import cl.dsy1102.veterinaria.model.Tamano;
import cl.dsy1102.veterinaria.repository.Repository;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.converter.LocalDateStringConverter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Formulario para crear o editar un paciente.
 */
public class FormularioController {

    private static final String PERRO = "Perro";
    private static final String GATO = "Gato";
    private static final String EXOTICO = "Exótico";
    private static final String CLASE_ERROR = "campo-error";

    @FXML private Label lblTitulo;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtTutor;
    @FXML private TextField txtTelefono;
    @FXML private DatePicker dpNacimiento;
    @FXML private VBox boxPerro;
    @FXML private TextField txtRaza;
    @FXML private ComboBox<Tamano> cmbTamano;
    @FXML private VBox boxGato;
    @FXML private CheckBox chkInterior;
    @FXML private VBox boxExotico;
    @FXML private ComboBox<String> cmbEspecieExotica;

    private Repository<Paciente> repositorio;
    private Paciente original;

    @FXML
    private void initialize() {
        cmbTipo.getItems().setAll(PERRO, GATO, EXOTICO);
        // Un ComboBox de enum: values() entrega todas las constantes y se muestra su toString().
        cmbTamano.getItems().setAll(Tamano.values());
        cmbEspecieExotica.getItems().setAll(Exotico.ESPECIES);

        enlazarVisibilidad(boxPerro, PERRO);
        enlazarVisibilidad(boxGato, GATO);
        enlazarVisibilidad(boxExotico, EXOTICO);

        // El calendario no permite elegir fechas futuras.
        // Mismo formato dd-MM-yyyy que el resto de la aplicacion (por defecto depende del sistema).
        dpNacimiento.setConverter(new LocalDateStringConverter(Atencion.FORMATO_FECHA, Atencion.FORMATO_FECHA));
        dpNacimiento.setDayCellFactory(selector -> new DateCell() {
            @Override
            public void updateItem(LocalDate fecha, boolean vacia) {
                super.updateItem(fecha, vacia);
                setDisable(vacia || fecha.isAfter(LocalDate.now()));
            }
        });
    }

    private void enlazarVisibilidad(VBox caja, String tipo) {
        caja.visibleProperty().bind(cmbTipo.valueProperty().isEqualTo(tipo));
        caja.managedProperty().bind(caja.visibleProperty());
    }

    /**
     * @param paciente el paciente a editar, o null para registrar uno nuevo
     */
    public void inicializar(Repository<Paciente> repositorio, Paciente paciente) {
        this.repositorio = repositorio;
        this.original = paciente;
        if (paciente == null) {
            lblTitulo.setText("Nuevo paciente");
            return;
        }
        lblTitulo.setText("Editar " + paciente.getNombre());
        cmbTipo.setValue(paciente instanceof Exotico ? EXOTICO : paciente.obtenerEspecie());
        cmbTipo.setDisable(true);
        txtNombre.setText(paciente.getNombre());
        txtTutor.setText(paciente.getTutor());
        txtTelefono.setText(paciente.getTelefonoTutor());
        dpNacimiento.setValue(paciente.getFechaNacimiento());
        if (paciente instanceof Perro perro) {
            txtRaza.setText(perro.getRaza());
            cmbTamano.setValue(perro.getTamano());
        } else if (paciente instanceof Gato gato) {
            chkInterior.setSelected(gato.isInterior());
        } else if (paciente instanceof Exotico exotico) {
            cmbEspecieExotica.setValue(exotico.getEspecie());
        }
    }

    @FXML
    private void onGuardar() {
        limpiarErrores();
        List<String> errores = new ArrayList<>();

        String tipo = cmbTipo.getValue();
        if (tipo == null) {
            marcar(cmbTipo, "Selecciona el tipo de paciente.", errores);
        }
        String nombre = leerTexto(txtNombre, "Nombre", errores);
        String tutor = leerTexto(txtTutor, "Tutor", errores);
        String telefono = leerTexto(txtTelefono, "Teléfono", errores);
        // Si el texto escrito no es una fecha valida, el DatePicker deja el valor en null.
        LocalDate nacimiento = dpNacimiento.getValue();
        if (nacimiento == null) {
            marcar(dpNacimiento, "Selecciona la fecha de nacimiento en el calendario.", errores);
        }
        if (PERRO.equals(tipo) && cmbTamano.getValue() == null) {
            marcar(cmbTamano, "Selecciona el tamaño del perro.", errores);
        } else if (EXOTICO.equals(tipo) && cmbEspecieExotica.getValue() == null) {
            marcar(cmbEspecieExotica, "Selecciona la especie.", errores);
        }

        if (!errores.isEmpty()) {
            Alertas.advertencia("Revisa los datos del formulario", String.join("\n", errores));
            return;
        }

        try {
            if (original == null) {
                repositorio.agregar(crear(tipo, nombre, tutor, telefono, nacimiento));
            } else {
                // Se edita una copia: conserva el historial y si algo falla el original no cambia.
                Paciente editado = original.copiar();
                editado.setNombre(nombre);
                editado.setTutor(tutor);
                editado.setTelefonoTutor(telefono);
                editado.setFechaNacimiento(nacimiento);
                if (editado instanceof Perro perro) {
                    perro.setRaza(txtRaza.getText());
                    perro.setTamano(cmbTamano.getValue());
                } else if (editado instanceof Gato gato) {
                    gato.setInterior(chkInterior.isSelected());
                } else if (editado instanceof Exotico exotico) {
                    exotico.setEspecie(cmbEspecieExotica.getValue());
                }
                repositorio.actualizar(original, editado);
            }
            volver();
        } catch (IllegalArgumentException e) {
            Alertas.advertencia("Dato no válido", e.getMessage());
        } catch (PersistenciaException e) {
            Alertas.error("No se pudo guardar el paciente", e.getMessage());
        }
    }

    private Paciente crear(String tipo, String nombre, String tutor, String telefono, LocalDate nacimiento) {
        return switch (tipo) {
            case PERRO -> new Perro(nombre, tutor, telefono, nacimiento, txtRaza.getText(), cmbTamano.getValue());
            case GATO -> new Gato(nombre, tutor, telefono, nacimiento, chkInterior.isSelected());
            default -> new Exotico(nombre, tutor, telefono, nacimiento, cmbEspecieExotica.getValue());
        };
    }

    @FXML
    private void onVolver() {
        volver();
    }

    private void volver() {
        PrincipalController principal = Navegador.navegar("principal-view.fxml", "Clínica Veterinaria Patitas del Sur");
        principal.inicializar(repositorio);
    }

    private String leerTexto(TextField campo, String nombreCampo, List<String> errores) {
        String texto = campo.getText().trim();
        if (texto.isEmpty()) {
            marcar(campo, "El campo " + nombreCampo + " es obligatorio.", errores);
            return null;
        }
        return texto;
    }

    private void marcar(Control campo, String mensaje, List<String> errores) {
        errores.add(mensaje);
        campo.getStyleClass().add(CLASE_ERROR);
    }

    private void limpiarErrores() {
        List.of(cmbTipo, txtNombre, txtTutor, txtTelefono, dpNacimiento, cmbTamano, cmbEspecieExotica)
                .forEach(control -> control.getStyleClass().remove(CLASE_ERROR));
    }
}
