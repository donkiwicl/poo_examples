package cl.dsy1102.ejemplos.navegacion;

import cl.dsy1102.ejemplos.navegacion.controller.CartaController;
import cl.dsy1102.ejemplos.navegacion.model.Pedido;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Pedidos para llevar de la Picada La Tia Rosa. Ejecuta con: mvn javafx:run
 */
public class AppPicada extends Application {

    @Override
    public void start(Stage stage) {
        // Estado compartido: un solo pedido que viaja de vista en vista.
        Pedido pedido = new Pedido();

        Navegador.setStage(stage);
        stage.setMinWidth(520);
        stage.setMinHeight(420);
        CartaController carta = Navegador.navegar("carta-view.fxml", "Picada La Tía Rosa");
        carta.inicializar(pedido);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
