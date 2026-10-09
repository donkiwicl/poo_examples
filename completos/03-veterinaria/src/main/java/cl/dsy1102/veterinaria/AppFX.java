package cl.dsy1102.veterinaria;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Punto de entrada de la aplicacion grafica.
 *
 * Revisa el enunciado en README.md. Ejecuta con: mvn javafx:run
 */
public class AppFX extends Application {

    @Override
    public void init() {
        // TODO R1: trazar el ciclo de vida imprimiendo por consola.
    }

    @Override
    public void start(Stage stage) {
        // TODO R6: crear el DAO JSON y el repositorio de pacientes, y cargar los datos.
        //          Si la carga falla, informar con un Alert y continuar con la lista vacia.
        // TODO R8: cargar la vista principal (FXML) con el Navegador, entregarle el
        //          repositorio a su controlador y mostrarla en el Stage recibido.
        stage.setTitle("Clínica Veterinaria Patitas del Sur");
        stage.show();
    }

    @Override
    public void stop() {
        // TODO R1: trazar el cierre de la aplicacion.
    }

    public static void main(String[] args) {
        launch(args);
    }
}
