package cl.dsy1102.veterinaria;

import cl.dsy1102.veterinaria.controller.Alertas;
import cl.dsy1102.veterinaria.controller.PrincipalController;
import cl.dsy1102.veterinaria.dao.JsonPacienteDao;
import cl.dsy1102.veterinaria.dao.PersistenciaException;
import cl.dsy1102.veterinaria.model.Paciente;
import cl.dsy1102.veterinaria.repository.Repository;
import cl.dsy1102.veterinaria.repository.PacienteRepository;
import javafx.application.Application;
import javafx.stage.Stage;

import java.nio.file.Path;

/**
 * Punto de entrada de la aplicacion grafica. Ejecuta con: mvn javafx:run
 *
 * Es el unico lugar, junto al DAO, que conoce la ubicacion del archivo de datos.
 */
public class AppFX extends Application {

    private static final Path ARCHIVO_DATOS = Path.of("data", "pacientes.json");

    @Override
    public void init() {
        System.out.println("[Ciclo de vida] init()  - hilo: " + Thread.currentThread().getName());
    }

    @Override
    public void start(Stage stage) {
        System.out.println("[Ciclo de vida] start() - hilo: " + Thread.currentThread().getName());

        // Se arman las capas: el controlador recibira solo la interfaz Repository.
        Repository<Paciente> repositorio = new PacienteRepository(new JsonPacienteDao(ARCHIVO_DATOS));

        Navegador.setStage(stage);
        stage.setMinWidth(800);
        stage.setMinHeight(500);
        PrincipalController principal = Navegador.navegar("principal-view.fxml", "Clínica Veterinaria Patitas del Sur");
        principal.inicializar(repositorio);
        stage.show();

        try {
            repositorio.cargar();
        } catch (PersistenciaException e) {
            Alertas.error("No se pudieron cargar los datos", e.getMessage()
                    + "\n\nLa aplicación iniciará sin pacientes. Si registras cambios, el archivo se reemplazará.");
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
