package cl.dsy1102.ejemplos.club;

import cl.dsy1102.ejemplos.club.dao.ConexionBD;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Crea una base de datos de prueba con sql/tablas.sql y sql/datos.sql.
 *
 * Por defecto usa H2 en memoria (una base nueva por prueba, no necesita servidor).
 * Para correr las mismas pruebas contra tu MySQL:
 *   mvn test -Dbd.url=jdbc:mysql://localhost:3306/club_halcones -Dbd.usuario=root -Dbd.clave=...
 */
final class BaseDePrueba {

    private static int contador;

    private BaseDePrueba() {
    }

    static ConexionBD crear() throws IOException, SQLException {
        Properties propiedades = new Properties();
        String url = System.getProperty("bd.url");
        if (url == null) {
            propiedades.setProperty("db.url", "jdbc:h2:mem:prueba" + (++contador)
                    + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
            propiedades.setProperty("db.usuario", "sa");
        } else {
            propiedades.setProperty("db.url", url);
            propiedades.setProperty("db.usuario", System.getProperty("bd.usuario", "root"));
            propiedades.setProperty("db.clave", System.getProperty("bd.clave", ""));
        }
        ConexionBD conexion = new ConexionBD(propiedades);
        ejecutar(conexion, Path.of("sql", "tablas.sql"));
        ejecutar(conexion, Path.of("sql", "datos.sql"));
        return conexion;
    }

    /** Ejecuta un script: quita los comentarios -- y separa las sentencias por ';'. */
    static void ejecutar(ConexionBD conexion, Path script) throws IOException, SQLException {
        String sinComentarios = Files.readAllLines(script).stream()
                .filter(linea -> !linea.trim().startsWith("--"))
                .collect(Collectors.joining("\n"));
        try (Connection con = conexion.abrir(); Statement st = con.createStatement()) {
            for (String sentencia : sinComentarios.split(";")) {
                if (!sentencia.isBlank()) {
                    st.execute(sentencia);
                }
            }
        }
    }
}
