package cl.dsy1102.almacen;

import cl.dsy1102.almacen.controller.Alertas;
import cl.dsy1102.almacen.controller.PrincipalController;
import cl.dsy1102.almacen.dao.ConexionBD;
import cl.dsy1102.almacen.dao.JdbcProductoDao;
import cl.dsy1102.almacen.dao.JdbcVentaDao;
import cl.dsy1102.almacen.dao.PersistenciaException;
import cl.dsy1102.almacen.model.Producto;
import cl.dsy1102.almacen.repository.ProductoRepository;
import cl.dsy1102.almacen.repository.Repository;
import cl.dsy1102.almacen.repository.VentaRepository;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Almacén Don Kiwi. Ejecuta con: mvn javafx:run
 *
 * Aquí se arman las capas: es el único lugar que conoce las implementaciones
 * concretas de los DAO. La migración de JSON a MySQL cambió solo estas líneas.
 */
public class AppFX extends Application {

    @Override
    public void start(Stage stage) {
        ConexionBD conexion;
        try {
            conexion = ConexionBD.desdeRecurso("/db.properties");
        } catch (IllegalStateException e) {
            Alertas.error("Configuración de la base de datos inválida", e.getMessage());
            Platform.exit();
            return;
        }
        Repository<Producto> productos = new ProductoRepository(new JdbcProductoDao(conexion));
        VentaRepository ventas = new VentaRepository(new JdbcVentaDao(conexion), productos);

        PrincipalController principal = Navegador.iniciar(stage, "principal-view.fxml", "Almacén Don Kiwi");
        principal.inicializar(productos, ventas);
        stage.setMinWidth(860);
        stage.setMinHeight(520);
        stage.show();

        try {
            productos.cargar();
        } catch (PersistenciaException e) {
            Alertas.error("No se pudieron cargar los productos", e.getMessage()
                    + "\n\nCuando el servidor esté disponible, pulsa Recargar.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
