package cl.dsy1102.ejemplos.navegacion.controller;

import cl.dsy1102.ejemplos.navegacion.Navegador;
import cl.dsy1102.ejemplos.navegacion.model.Carta;
import cl.dsy1102.ejemplos.navegacion.model.Pedido;
import cl.dsy1102.ejemplos.navegacion.model.Plato;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
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
        // R2: doble clic sobre un plato tambien abre su detalle.
        lstPlatos.setOnMouseClicked(evento -> {
            if (evento.getClickCount() == 2 && lstPlatos.getSelectionModel().getSelectedItem() != null) {
                onVerDetalle();
            }
        });
    }

    /**
     * Paso de parametros: quien navega hacia esta vista llama a este metodo
     * con el pedido en curso.
     */
    public void inicializar(Pedido pedido) {
        this.pedido = pedido;
        lblPedido.setText("Pedido: " + pedido.getCantidadItems() + " ítems · " + Plato.pesos(pedido.getTotal()));
    }

    /** Muestra un mensaje al volver de otra vista, por ejemplo tras confirmar. */
    public void mostrarMensaje(String mensaje) {
        lblMensaje.setText(mensaje);
    }

    @FXML
    private void onVerDetalle() {
        Plato plato = lstPlatos.getSelectionModel().getSelectedItem();
        if (plato == null) {
            new Alert(Alert.AlertType.WARNING, "Selecciona un plato de la carta.").showAndWait();
            return;
        }
        // navegar() retorna el controlador de la vista nueva: se le entregan los datos.
        DetalleController detalle = Navegador.navegar("detalle-view.fxml", plato.getNombre());
        detalle.inicializar(pedido, plato);
    }

    @FXML
    private void onVerPedido() {
        PedidoController vista = Navegador.navegar("pedido-view.fxml", "Tu pedido");
        vista.inicializar(pedido);
    }
}
