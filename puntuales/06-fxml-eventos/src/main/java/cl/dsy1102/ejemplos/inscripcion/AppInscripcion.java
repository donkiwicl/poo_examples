package cl.dsy1102.ejemplos.inscripcion;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Inscripciones al Taller de Cueca del Centro Cultural Estacion Yungay.
 * Ejecuta con: mvn javafx:run
 */
public class AppInscripcion extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // La ruta es relativa a esta clase: el FXML esta en el mismo paquete, dentro de resources.
        FXMLLoader loader = new FXMLLoader(AppInscripcion.class.getResource("inscripcion-view.fxml"));
        Parent raiz = loader.load();
        stage.setScene(new Scene(raiz, 820, 520));
        stage.setTitle("Taller de Cueca - Inscripciones");
        stage.setMinWidth(700);
        stage.setMinHeight(460);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
