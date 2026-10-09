package cl.dsy1102.ejemplos.imagenes;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Catalogo de Parques Nacionales de Chile. Ejecuta con: mvn javafx:run
 */
public class AppParques extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(AppParques.class.getResource("parques-view.fxml"));
        stage.setScene(new Scene(loader.load(), 960, 600));
        stage.setTitle("Parques Nacionales de Chile");
        stage.setMinWidth(720);
        stage.setMinHeight(480);
        // TODO R6: usa img/icono.png como icono de la ventana (stage.getIcons()).
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
