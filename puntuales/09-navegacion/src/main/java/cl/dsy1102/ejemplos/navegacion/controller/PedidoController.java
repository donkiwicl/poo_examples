package cl.dsy1102.ejemplos.navegacion.controller;

import cl.dsy1102.ejemplos.navegacion.model.LineaPedido;
import cl.dsy1102.ejemplos.navegacion.model.Pedido;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

/**
 * Resumen del pedido.
 */
public class PedidoController {

    @FXML private ListView<LineaPedido> lstLineas;
    @FXML private Label lblTotal;
    @FXML private Button btnConfirmar;

    private Pedido pedido;

    /** TODO R3: muestra las lineas y el total. Con el pedido vacio, Confirmar queda deshabilitado. */
    public void inicializar(Pedido pedido) {
    }

    /**
     * TODO R4: abre confirmacion-view.fxml como ventana MODAL con
     *  Navegador.abrirModal(...), entregandole el total. Si el usuario confirmo,
     *  vacia el pedido y vuelve a la carta con "Pedido listo para Ana. Total: $...".
     *  Si cancelo, se queda en esta vista.
     */
    @FXML
    private void onConfirmar() {
    }

    /** TODO R3: vuelve a la carta conservando el pedido. */
    @FXML
    private void onVolver() {
    }
}
