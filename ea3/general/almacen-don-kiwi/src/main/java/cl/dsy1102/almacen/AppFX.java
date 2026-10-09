package cl.dsy1102.almacen;

import cl.dsy1102.almacen.controller.Alertas;
import cl.dsy1102.almacen.controller.PrincipalController;
import cl.dsy1102.almacen.dao.JsonProductoDao;
import cl.dsy1102.almacen.dao.PersistenciaException;
import cl.dsy1102.almacen.model.Producto;
import cl.dsy1102.almacen.repository.ProductoRepository;
import cl.dsy1102.almacen.repository.Repository;
import javafx.application.Application;
import javafx.stage.Stage;

import java.nio.file.Path;

/**
 * Almacén Don Kiwi. Ejecuta con: mvn javafx:run
 *
 * Aquí se arman las capas: es el único lugar que conoce la implementación
 * concreta del DAO.
 */
public class AppFX extends Application {

    private static final Path ARCHIVO_DATOS = Path.of("data", "productos.json");

    @Override
    public void start(Stage stage) {
        Repository<Producto> productos = new ProductoRepository(new JsonProductoDao(ARCHIVO_DATOS));

        PrincipalController principal = Navegador.iniciar(stage, "principal-view.fxml", "Almacén Don Kiwi");
        principal.inicializar(productos);
        stage.setMinWidth(860);
        stage.setMinHeight(520);
        stage.show();

        try {
            productos.cargar();
        } catch (PersistenciaException e) {
            Alertas.error("No se pudieron cargar los productos", e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
