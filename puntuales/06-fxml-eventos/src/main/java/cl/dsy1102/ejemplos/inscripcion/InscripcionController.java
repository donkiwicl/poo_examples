package cl.dsy1102.ejemplos.inscripcion;

import javafx.fxml.FXML;

/**
 * Controlador de inscripcion-view.fxml.
 *
 * TODO R1: declara un atributo privado @FXML por cada fx:id de la vista,
 *  con el mismo nombre y el tipo del control (TextField txtNombre, ...).
 */
public class InscripcionController {

    /**
     * FXMLLoader lo llama despues de inyectar los @FXML.
     *
     * TODO R2:
     *  - Carga los niveles "Principiante", "Intermedio" y "Avanzado" en cmbNivel.
     *  - txtPareja solo se habilita cuando chkPareja esta marcado (usa un binding).
     *  - Al seleccionar un elemento de lstInscritos, lblDetalle muestra su obtenerDetalle().
     *  - lblTotal muestra "Inscritos: N" y se actualiza solo.
     */
    @FXML
    private void initialize() {
    }

    /** TODO R3: valida, crea la Inscripcion, agregala a la lista y limpia el formulario. */
    @FXML
    private void onInscribir() {
    }

    /** TODO R4: elimina la inscripcion seleccionada, previa confirmacion con un Alert. */
    @FXML
    private void onEliminar() {
    }
}
