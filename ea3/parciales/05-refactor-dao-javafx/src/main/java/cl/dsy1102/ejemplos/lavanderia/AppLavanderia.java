package cl.dsy1102.ejemplos.lavanderia;

import cl.dsy1102.ejemplos.lavanderia.controller.ClienteController;
import cl.dsy1102.ejemplos.lavanderia.dao.ClienteDao;
import cl.dsy1102.ejemplos.lavanderia.dao.ClienteDaoJdbc;
import cl.dsy1102.ejemplos.lavanderia.database.ConexionBD;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Lavandería La Burbuja: registro de clientes. Ejecuta con: mvn javafx:run
 *
 * Aquí se arman las capas: es el único lugar que conoce la implementación
 * concreta del DAO.
 */
public class AppLavanderia extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        ConexionBD conexion;
        try {
            conexion = ConexionBD.desdeRecurso("/db.properties");
        } catch (IllegalStateException e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR, e.getMessage());
            alerta.setHeaderText("Configuración de la base de datos inválida");
            alerta.showAndWait();
            Platform.exit();
            return;
        }
        ClienteDao dao = new ClienteDaoJdbc(conexion);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("view/clientes-view.fxml"));
        stage.setScene(new Scene(loader.load(), 900, 520));
        stage.setTitle("Lavandería La Burbuja · Clientes");
        stage.setMinWidth(760);
        stage.setMinHeight(420);
        stage.show();

        // Después de show(): si no hay conexión, el Alert aparece sobre la ventana ya visible.
        ClienteController controlador = loader.getController();
        controlador.inicializar(dao);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
