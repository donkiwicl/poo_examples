package cl.dsy1102.ejemplos.lavanderia.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
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

    public ConexionBD(Properties propiedades) {
        this.url = obligatoria(propiedades, PROPIEDAD_URL);
        this.usuario = obligatoria(propiedades, PROPIEDAD_USUARIO);
        this.clave = propiedades.getProperty(PROPIEDAD_CLAVE, "");
    }

    private static String obligatoria(Properties propiedades, String nombre) {
        String valor = propiedades.getProperty(nombre);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Falta la propiedad " + nombre + " en db.properties.");
        }
        return valor.trim();
    }

    public static ConexionBD desdeRecurso(String recurso) {
        try (InputStream entrada = ConexionBD.class.getResourceAsStream(recurso)) {
            if (entrada == null) {
                throw new IllegalStateException("No se encontró el archivo " + recurso + " en resources.");
            }
            Properties propiedades = new Properties();
            propiedades.load(entrada);
            return new ConexionBD(propiedades);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer " + recurso + ": " + e.getMessage(), e);
        }
    }

    /**
     * Abre una conexión nueva. Quien la pide es responsable de cerrarla
     * (try-with-resources).
     */
    public Connection abrir() throws SQLException {
        // DriverManager busca, entre los drivers del classpath, el que acepta
        // esta URL (jdbc:mysql:... -> MySQL Connector/J, jdbc:h2:... -> H2).
        return DriverManager.getConnection(url, usuario, clave);
    }

    public String getUrl() {
        return url;
    }
}
