package cl.dsy1102.ejemplos.navegacion;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
     * TODO R4: abre la vista en una ventana MODAL nueva, duena de la principal.
     *  1. Carga el FXML y obtiene su controlador.
     *  2. Llama a preparar.accept(controlador) para entregarle datos ANTES de mostrarla.
     *  3. Crea un Stage con initOwner(stage) e initModality(Modality.WINDOW_MODAL).
     *  4. showAndWait(): el codigo se detiene aqui hasta que la ventana se cierre.
     *  5. Retorna el controlador para que quien la abrio lea el resultado.
     */
    public static <T> T abrirModal(String fxml, String titulo, Consumer<T> preparar) {
        throw new UnsupportedOperationException("TODO R4: Navegador.abrirModal");
    }
}
