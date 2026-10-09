package cl.dsy1102.ejemplos.navegacion.controller;

import cl.dsy1102.ejemplos.navegacion.Navegador;
import cl.dsy1102.ejemplos.navegacion.model.Pedido;
import cl.dsy1102.ejemplos.navegacion.model.Plato;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

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

    /** Recibe el pedido y el plato desde la carta. */
    public void inicializar(Pedido pedido, Plato plato) {
        this.pedido = pedido;
        this.plato = plato;
        lblNombre.setText(plato.getNombre());
        lblDescripcion.setText(plato.getDescripcion());
        lblPrecio.setText(Plato.pesos(plato.getPrecio()) + " c/u");

        spnCantidad.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Pedido.MAXIMO_POR_PLATO, 1));
        spnCantidad.valueProperty().addListener((obs, antes, cantidad) -> actualizarSubtotal());
        actualizarSubtotal();
    }

    private void actualizarSubtotal() {
        lblSubtotal.setText("Subtotal: " + Plato.pesos(plato.getPrecio() * spnCantidad.getValue()));
    }

    @FXML
    private void onAgregar() {
        int cantidad = spnCantidad.getValue();
        try {
            pedido.agregar(plato, cantidad);
        } catch (IllegalArgumentException e) {
            new Alert(Alert.AlertType.WARNING, e.getMessage()).showAndWait();
            return;
        }
        CartaController carta = volverACarta();
        carta.mostrarMensaje("Agregaste " + cantidad + " x " + plato.getNombre() + ".");
    }

    @FXML
    private void onVolver() {
        volverACarta();
    }

    /** La carta es una vista NUEVA: hay que volver a entregarle el pedido. */
    private CartaController volverACarta() {
        CartaController carta = Navegador.navegar("carta-view.fxml", "Picada La Tía Rosa");
        carta.inicializar(pedido);
        return carta;
    }
}
