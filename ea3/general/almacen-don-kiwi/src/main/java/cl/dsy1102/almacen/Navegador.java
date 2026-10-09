package cl.dsy1102.almacen;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Centraliza el cambio de vistas sobre el Stage principal.
 *
 * La vista de inicio se carga una sola vez y se recuerda: volverAlInicio() la
 * muestra de nuevo con su controlador y su estado (filtros, selección).
 */
public final class Navegador {

    private static final String RUTA_VISTAS = "/cl/dsy1102/almacen/view/";

    private static Stage stage;
    private static Parent raizInicio;
    private static String tituloInicio;

    private Navegador() {
    }

    /** Carga la vista de inicio, la muestra y la recuerda. Retorna su controlador. */
    public static <T> T iniciar(Stage stagePrincipal, String fxml, String titulo) {
        stage = stagePrincipal;
        FXMLLoader loader = cargar(fxml);
        raizInicio = loader.getRoot();
        tituloInicio = titulo;
        stage.setScene(new Scene(raizInicio, 1000, 600));
        stage.setTitle(titulo);
        return loader.getController();
    }

    /** Muestra otra vista. Retorna su controlador para entregarle datos. */
    public static <T> T navegar(String fxml, String titulo) {
        FXMLLoader loader = cargar(fxml);
        stage.getScene().setRoot(loader.getRoot());
        stage.setTitle(titulo);
        return loader.getController();
    }

    public static void volverAlInicio() {
        stage.getScene().setRoot(raizInicio);
        stage.setTitle(tituloInicio);
    }

    private static FXMLLoader cargar(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(Navegador.class.getResource(RUTA_VISTAS + fxml));
            loader.load();
            return loader;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar la vista " + fxml, e);
        }
    }
}
