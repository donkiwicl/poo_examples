package cl.dsy1102.arriendo;

import cl.dsy1102.arriendo.controller.Alertas;
import cl.dsy1102.arriendo.controller.PrincipalController;
import cl.dsy1102.arriendo.dao.JsonVehiculoDao;
import cl.dsy1102.arriendo.dao.PersistenciaException;
import cl.dsy1102.arriendo.model.Vehiculo;
import cl.dsy1102.arriendo.repository.Repository;
import cl.dsy1102.arriendo.repository.VehiculoRepository;
import javafx.application.Application;
import javafx.stage.Stage;

import java.nio.file.Path;

/**
 * Punto de entrada de la aplicacion grafica. Ejecuta con: mvn javafx:run
 *
 * Es el unico lugar, junto al DAO, que conoce la ubicacion del archivo de datos.
 */
public class AppFX extends Application {

    private static final Path ARCHIVO_DATOS = Path.of("data", "vehiculos.json");

    @Override
    public void init() {
        System.out.println("[Ciclo de vida] init()  - hilo: " + Thread.currentThread().getName());
    }

    @Override
    public void start(Stage stage) {
        System.out.println("[Ciclo de vida] start() - hilo: " + Thread.currentThread().getName());

        // Se arman las capas: el controlador recibira solo la interfaz Repository.
        Repository<Vehiculo> repositorio = new VehiculoRepository(new JsonVehiculoDao(ARCHIVO_DATOS));

        Navegador.setStage(stage);
        stage.setMinWidth(800);
        stage.setMinHeight(500);
        PrincipalController principal = Navegador.navegar("principal-view.fxml", "Arriendo Cerro Alegre");
        principal.inicializar(repositorio);
        stage.show();

        try {
            repositorio.cargar();
        } catch (PersistenciaException e) {
            Alertas.error("No se pudieron cargar los datos", e.getMessage()
                    + "\n\nLa aplicación iniciará sin vehículos. Si registras cambios, el archivo se reemplazará.");
        }
    }

    @Override
    public void stop() {
        System.out.println("[Ciclo de vida] stop()  - hilo: " + Thread.currentThread().getName());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
