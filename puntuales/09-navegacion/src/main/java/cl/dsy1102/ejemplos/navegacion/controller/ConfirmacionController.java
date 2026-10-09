package cl.dsy1102.ejemplos.navegacion.controller;

import cl.dsy1102.ejemplos.navegacion.model.Pedido;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Contenido de la ventana modal de confirmacion.
 */
public class ConfirmacionController {

    @FXML private Label lblTotal;
    @FXML private TextField txtNombre;
    @FXML private Label lblError;

    private Pedido pedido;
    private boolean confirmado;

    /** TODO R4: guarda el pedido y muestra "Total a pagar: $..." en lblTotal. */
    public void inicializar(Pedido pedido) {
    }

    /**
     * TODO R4: confirma el pedido con el nombre (el modelo valida que no este vacio;
     *  si lo rechaza, muestra el mensaje en lblError). Si todo esta bien,
     *  marca confirmado = true y cierra SOLO esta ventana.
     */
    @FXML
    private void onAceptar() {
    }

    /** TODO R4: cierra esta ventana sin confirmar. */
    @FXML
    private void onCancelar() {
    }

    /** Lo consulta quien abrio la modal, despues de que se cierra. */
    public boolean isConfirmado() {
        return confirmado;
    }
}
