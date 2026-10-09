package cl.dsy1102.ejemplos.inscripcion;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador de inscripcion-view.fxml.
 */
public class InscripcionController {

    private static final String CAMPO_ERROR = "campo-error";

    // R1: un atributo por fx:id. El nombre debe coincidir exactamente.
    @FXML private TextField txtNombre;
    @FXML private TextField txtEdad;
    @FXML private ComboBox<String> cmbNivel;
    @FXML private ToggleGroup grpHorario;
    @FXML private RadioButton rbManana;
    @FXML private RadioButton rbTarde;
    @FXML private CheckBox chkPareja;
    @FXML private TextField txtPareja;
    @FXML private Label lblMensaje;
    @FXML private Label lblTotal;
    @FXML private ListView<Inscripcion> lstInscritos;
    @FXML private Label lblDetalle;

    private final ObservableList<Inscripcion> inscritos = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        cmbNivel.getItems().setAll("Principiante", "Intermedio", "Avanzado");

        // Binding: txtPareja queda deshabilitado mientras chkPareja NO este marcado.
        txtPareja.disableProperty().bind(chkPareja.selectedProperty().not());

        // El ListView muestra la lista observable: agregar o quitar de la lista actualiza la vista.
        lstInscritos.setItems(inscritos);
        lblTotal.textProperty().bind(Bindings.format("Inscritos: %d", Bindings.size(inscritos)));

        // Evento de seleccion: un listener sobre la propiedad selectedItem.
        lstInscritos.getSelectionModel().selectedItemProperty().addListener((obs, anterior, actual) ->
                lblDetalle.setText(actual == null
                        ? "Selecciona una inscripción para ver su detalle."
                        : actual.obtenerDetalle()));
    }

    @FXML
    private void onInscribir() {
        limpiarErrores();
        List<String> errores = new ArrayList<>();

        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            marcar(txtNombre, "El nombre es obligatorio.", errores);
        } else if (estaInscrito(nombre)) {
            marcar(txtNombre, nombre + " ya está inscrito.", errores);
        }

        Integer edad = null;
        try {
            edad = Integer.parseInt(txtEdad.getText().trim());
        } catch (NumberFormatException e) {
            marcar(txtEdad, "La edad debe ser un número entero.", errores);
        }

        if (cmbNivel.getValue() == null) {
            marcar(cmbNivel, "Selecciona un nivel.", errores);
        }

        RadioButton horario = (RadioButton) grpHorario.getSelectedToggle();
        if (horario == null) {
            errores.add("Selecciona un horario.");
        }

        String pareja = chkPareja.isSelected() ? txtPareja.getText() : null;

        if (!errores.isEmpty()) {
            mostrarError(String.join("\n", errores));
            return;
        }

        try {
            // El modelo aplica las reglas de rango (edad, pareja vacia) y avisa con una excepcion.
            Inscripcion inscripcion = new Inscripcion(nombre, edad, cmbNivel.getValue(), horario.getText(), pareja);
            inscritos.add(inscripcion);
            limpiarFormulario();
            mostrarOk("Se inscribió a " + inscripcion.getNombre() + ".");
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void onEliminar() {
        Inscripcion seleccionada = lstInscritos.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            new Alert(Alert.AlertType.WARNING, "Selecciona una inscripción de la lista.").showAndWait();
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar la inscripción de " + seleccionada.getNombre() + "?");
        confirmacion.setHeaderText("Eliminar inscripción");
        if (confirmacion.showAndWait().filter(ButtonType.OK::equals).isPresent()) {
            inscritos.remove(seleccionada);
        }
    }

    /** R6: boton agregado en Scene Builder. */
    @FXML
    private void onLimpiar() {
        limpiarFormulario();
        lblMensaje.setText("");
    }

    private boolean estaInscrito(String nombre) {
        return inscritos.stream().anyMatch(i -> i.getNombre().equalsIgnoreCase(nombre));
    }

    private void limpiarFormulario() {
        limpiarErrores();
        txtNombre.clear();
        txtEdad.clear();
        cmbNivel.getSelectionModel().clearSelection();
        grpHorario.selectToggle(null);
        chkPareja.setSelected(false);
        txtPareja.clear();
        txtNombre.requestFocus();
    }

    private void marcar(Control campo, String mensaje, List<String> errores) {
        errores.add(mensaje);
        campo.getStyleClass().add(CAMPO_ERROR);
    }

    private void limpiarErrores() {
        List.of(txtNombre, txtEdad, cmbNivel).forEach(c -> c.getStyleClass().remove(CAMPO_ERROR));
    }

    private void mostrarOk(String texto) {
        lblMensaje.getStyleClass().setAll("label", "mensaje-ok");
        lblMensaje.setText(texto);
    }

    private void mostrarError(String texto) {
        lblMensaje.getStyleClass().setAll("label", "mensaje-error");
        lblMensaje.setText(texto);
    }
}
