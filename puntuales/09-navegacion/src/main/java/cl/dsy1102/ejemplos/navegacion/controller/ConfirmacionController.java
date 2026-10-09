package cl.dsy1102.ejemplos.navegacion.controller;

import cl.dsy1102.ejemplos.navegacion.model.Pedido;
import cl.dsy1102.ejemplos.navegacion.model.Plato;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Contenido de la ventana modal de confirmacion.
 */
public class ConfirmacionController {

    @FXML private Label lblTotal;
    @FXML private TextField txtNombre;
    @FXML private Label lblError;

    private Pedido pedido;
    private boolean confirmado;

    public void inicializar(Pedido pedido) {
        this.pedido = pedido;
        lblTotal.setText("Total a pagar: " + Plato.pesos(pedido.getTotal()));
    }

    @FXML
    private void onAceptar() {
        try {
            pedido.confirmar(txtNombre.getText());
        } catch (IllegalArgumentException | IllegalStateException e) {
            lblError.setText(e.getMessage());
            return;
        }
        confirmado = true;
        cerrar();
    }

    @FXML
    private void onCancelar() {
        cerrar();
    }

    /** Se obtiene la ventana desde cualquier nodo de la vista: asi se cierra solo la modal. */
    private void cerrar() {
        ((Stage) txtNombre.getScene().getWindow()).close();
    }

    /** Lo consulta quien abrio la modal, despues de que se cierra. */
    public boolean isConfirmado() {
        return confirmado;
    }
}
