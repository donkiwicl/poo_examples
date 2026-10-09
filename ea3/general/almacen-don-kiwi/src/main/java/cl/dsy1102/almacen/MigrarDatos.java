package cl.dsy1102.almacen;

import cl.dsy1102.almacen.dao.ConexionBD;
import cl.dsy1102.almacen.dao.JdbcProductoDao;
import cl.dsy1102.almacen.dao.JsonProductoDao;
import cl.dsy1102.almacen.dao.PersistenciaException;
import cl.dsy1102.almacen.dao.ProductoDao;
import cl.dsy1102.almacen.model.Producto;

import java.nio.file.Path;

/**
 * Copia los productos de la versión JSON a MySQL. Ejecuta con:
 *   mvn compile exec:java                                   (lee data/productos.json)
 *   mvn compile exec:java -Dexec.args="datos-ejemplo/productos.json"
 *
 * Usa dos implementaciones de la misma interfaz: lee con una y escribe con la otra.
 */
public class MigrarDatos {

    public static void main(String[] args) {
        Path archivo = Path.of(args.length > 0 ? args[0] : "data/productos.json");
        ProductoDao origen = new JsonProductoDao(archivo);
        ProductoDao destino = new JdbcProductoDao(ConexionBD.desdeRecurso("/db.properties"));

        System.out.println("=== Migración de " + archivo + " a MySQL ===");
        int migrados = 0;
        int omitidos = 0;
        try {
            for (Producto producto : origen.listarTodos()) {
                producto.setId(0); // MySQL asigna un id nuevo
                try {
                    destino.insertar(producto);
                    migrados++;
                    System.out.println("  + " + producto + " (id " + producto.getId() + ")");
                } catch (PersistenciaException e) {
                    omitidos++;
                    System.out.println("  - " + producto + ": " + e.getMessage());
                }
            }
        } catch (PersistenciaException e) {
            System.out.println("No se pudo leer el archivo: " + e.getMessage());
        }
        System.out.println(migrados + " producto(s) migrado(s), " + omitidos + " omitido(s).");
    }
}
