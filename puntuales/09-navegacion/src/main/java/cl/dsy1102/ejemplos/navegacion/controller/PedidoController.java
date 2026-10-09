package cl.dsy1102.ejemplos.navegacion.controller;

import cl.dsy1102.ejemplos.navegacion.Navegador;
import cl.dsy1102.ejemplos.navegacion.model.LineaPedido;
import cl.dsy1102.ejemplos.navegacion.model.Pedido;
import cl.dsy1102.ejemplos.navegacion.model.Plato;
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

    public void inicializar(Pedido pedido) {
        this.pedido = pedido;
        lstLineas.setItems(pedido.getLineas());
        lblTotal.setText("Total: " + Plato.pesos(pedido.getTotal()));
        btnConfirmar.setDisable(pedido.getLineas().isEmpty());
    }

    @FXML
    private void onConfirmar() {
        int total = pedido.getTotal();
        // El lambda recibe el controlador de la modal ANTES de mostrarla: asi se le entregan datos.
        ConfirmacionController confirmacion = Navegador.abrirModal("confirmacion-view.fxml", "Confirmar pedido",
                (ConfirmacionController c) -> c.inicializar(pedido));

        // showAndWait() ya termino: la modal se cerro y se lee su resultado.
        if (confirmacion.isConfirmado()) {
            String nombre = pedido.getNombreRetiro();
            pedido.vaciar();
            CartaController carta = Navegador.navegar("carta-view.fxml", "Picada La Tía Rosa");
            carta.inicializar(pedido);
            carta.mostrarMensaje("Pedido listo para " + nombre + ". Total: " + Plato.pesos(total));
        }
    }

    @FXML
    private void onVolver() {
        CartaController carta = Navegador.navegar("carta-view.fxml", "Picada La Tía Rosa");
        carta.inicializar(pedido);
    }
}
