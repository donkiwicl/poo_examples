package cl.dsy1102.ejemplos.liga;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Tabla de posiciones de la Liga Vecinal de Baby Futbol Cordillera.
 * Ejecuta con: mvn javafx:run
 */
public class AppLiga extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(AppLiga.class.getResource("liga-view.fxml"));
        stage.setScene(new Scene(loader.load(), 1040, 620));
        LigaController controlador = loader.getController();
        controlador.setLiga(Liga.conDatosDeEjemplo());
        stage.setTitle("Liga Vecinal Cordillera");
        stage.setMinWidth(980);
        stage.setMinHeight(520);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
