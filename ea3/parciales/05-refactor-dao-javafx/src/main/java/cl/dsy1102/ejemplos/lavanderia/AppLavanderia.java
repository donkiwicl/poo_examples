package cl.dsy1102.ejemplos.lavanderia;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Lavandería La Burbuja: registro de clientes. Ejecuta con: mvn javafx:run
 */
public class AppLavanderia extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("view/clientes-view.fxml"));
        stage.setScene(new Scene(loader.load(), 900, 520));
        stage.setTitle("Lavandería La Burbuja · Clientes");
        stage.setMinWidth(760);
        stage.setMinHeight(420);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
