package cl.dsy1102.ejemplos.navegacion.controller;

import cl.dsy1102.ejemplos.navegacion.model.Carta;
import cl.dsy1102.ejemplos.navegacion.model.Pedido;
import cl.dsy1102.ejemplos.navegacion.model.Plato;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

/**
 * Vista principal: carta del dia y resumen del pedido.
 */
public class CartaController {

    @FXML private Label lblPedido;
    @FXML private ListView<Plato> lstPlatos;
    @FXML private Label lblMensaje;

    private Pedido pedido;

    @FXML
    private void initialize() {
        lstPlatos.getItems().setAll(Carta.platos());
        // TODO R2: doble clic sobre un plato tambien abre su detalle.
    }

    /**
     * Ejemplo resuelto de paso de parametros: quien navega hacia esta vista
     * llama a este metodo con el pedido en curso.
     */
    public void inicializar(Pedido pedido) {
        this.pedido = pedido;
        lblPedido.setText("Pedido: " + pedido.getCantidadItems() + " ítems · " + Plato.pesos(pedido.getTotal()));
    }

    /** Muestra un mensaje al volver de otra vista, por ejemplo tras confirmar. */
    public void mostrarMensaje(String mensaje) {
        lblMensaje.setText(mensaje);
    }

    /**
     * TODO R1: navega a detalle-view.fxml y entrega al DetalleController el
     *  pedido y el plato seleccionado. Sin seleccion, muestra un Alert.
     */
    @FXML
    private void onVerDetalle() {
    }

    /** TODO R3: navega a pedido-view.fxml entregando el pedido. */
    @FXML
    private void onVerPedido() {
    }
}
