package cl.dsy1102.ejemplos.navegacion;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Centraliza el cambio de vistas sobre la ventana principal.
 */
public final class Navegador {

    private static final String RUTA_VISTAS = "/cl/dsy1102/ejemplos/navegacion/view/";

    private static Stage stage;

    private Navegador() {
    }

    public static void setStage(Stage stagePrincipal) {
        stage = stagePrincipal;
    }

    /**
     * Ya resuelto. Carga una vista en la ventana principal y retorna su
     * controlador, para que quien navega le entregue los datos que necesita.
     */
    public static <T> T navegar(String fxml, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(Navegador.class.getResource(RUTA_VISTAS + fxml));
            Parent raiz = loader.load();
            if (stage.getScene() == null) {
                stage.setScene(new Scene(raiz, 620, 480));
            } else {
                stage.getScene().setRoot(raiz);   // misma ventana y mismo tamano
            }
            stage.setTitle(titulo);
            return loader.getController();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar la vista " + fxml, e);
        }
    }

    /**
     * Abre la vista en una ventana MODAL nueva, duena de la principal, y
     * espera a que se cierre.
     *
     * @param preparar recibe el controlador ANTES de mostrar la ventana, para entregarle datos
     * @return el controlador, para que quien abrio la ventana lea el resultado
     */
    public static <T> T abrirModal(String fxml, String titulo, Consumer<T> preparar) {
        try {
            FXMLLoader loader = new FXMLLoader(Navegador.class.getResource(RUTA_VISTAS + fxml));
            Parent raiz = loader.load();
            T controlador = loader.getController();
            preparar.accept(controlador);

            Stage modal = new Stage();
            modal.initOwner(stage);                         // se centra y queda encima de la principal
            modal.initModality(Modality.WINDOW_MODAL);      // bloquea la principal mientras este abierta
            modal.setTitle(titulo);
            modal.setResizable(false);
            modal.setScene(new Scene(raiz));
            modal.showAndWait();                            // el metodo se detiene aqui hasta cerrar
            return controlador;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar la vista " + fxml, e);
        }
    }
}
