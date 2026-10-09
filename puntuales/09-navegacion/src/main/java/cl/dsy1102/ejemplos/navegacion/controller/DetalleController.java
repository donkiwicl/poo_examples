package cl.dsy1102.ejemplos.navegacion.controller;

import cl.dsy1102.ejemplos.navegacion.model.Pedido;
import cl.dsy1102.ejemplos.navegacion.model.Plato;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;

/**
 * Detalle de un plato con su cantidad.
 */
public class DetalleController {

    @FXML private Label lblNombre;
    @FXML private Label lblDescripcion;
    @FXML private Label lblPrecio;
    @FXML private Spinner<Integer> spnCantidad;
    @FXML private Label lblSubtotal;

    private Pedido pedido;
    private Plato plato;

    /**
     * TODO R1: recibe el pedido y el plato desde la carta.
     *  - Muestra nombre, descripcion y precio.
     *  - spnCantidad acepta de 1 a Pedido.MAXIMO_POR_PLATO (SpinnerValueFactory.IntegerSpinnerValueFactory).
     *  - lblSubtotal muestra "Subtotal: $..." y se actualiza al cambiar la cantidad.
     */
    public void inicializar(Pedido pedido, Plato plato) {
    }

    /**
     * TODO R1: agrega al pedido y vuelve a la carta con el mensaje
     *  "Agregaste 2 x Pastel de choclo.". Si el modelo rechaza la cantidad, Alert.
     */
    @FXML
    private void onAgregar() {
    }

    /** TODO R1: vuelve a la carta sin agregar nada (el pedido se conserva). */
    @FXML
    private void onVolver() {
    }
}
