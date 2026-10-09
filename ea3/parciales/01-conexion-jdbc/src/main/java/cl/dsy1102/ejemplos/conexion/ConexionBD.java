package cl.dsy1102.ejemplos.conexion;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Centraliza los parámetros de conexión y la creación de objetos Connection.
 *
 * Ninguna otra clase del proyecto conoce la URL, el usuario ni la contraseña:
 * si cambian, se modifica solo db.properties.
 */
public class ConexionBD {

    public static final String PROPIEDAD_URL = "db.url";
    public static final String PROPIEDAD_USUARIO = "db.usuario";
    public static final String PROPIEDAD_CLAVE = "db.clave";

    private final String url;
    private final String usuario;
    private final String clave;

    /**
     * TODO R3: lee las tres propiedades.
     *  - Si db.url o db.usuario no existen o están en blanco, lanza
     *    IllegalStateException("Falta la propiedad db.url en db.properties.")
     *    (con el nombre de la propiedad que falta).
     *  - db.clave puede estar vacía (si no existe, usa "").
     */
    public ConexionBD(Properties propiedades) {
        this.url = null;
        this.usuario = null;
        this.clave = null;
    }

    /**
     * TODO R3: carga un archivo .properties desde src/main/resources (o
     * src/test/resources en las pruebas) con getResourceAsStream y crea la
     * ConexionBD.
     *  - Si el recurso no existe, lanza
     *    IllegalStateException("No se encontró el archivo /db.properties en resources.").
     *  - Una IOException al leer se traduce a IllegalStateException.
     *  - Cierra el InputStream con try-with-resources.
     */
    public static ConexionBD desdeRecurso(String recurso) {
        throw new UnsupportedOperationException("TODO R3");
    }

    /**
     * TODO R4: abre una conexión nueva con DriverManager. Quien la pide es
     * responsable de cerrarla (try-with-resources).
     */
    public Connection abrir() throws SQLException {
        throw new UnsupportedOperationException("TODO R4");
    }

    public String getUrl() {
        return url;
    }
}
