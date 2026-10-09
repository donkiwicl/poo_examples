package cl.dsy1102.ejemplos.conexion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de autoevaluación. Usan H2 en memoria (src/test/resources/db-prueba.properties),
 * así que no necesitan un servidor MySQL. Ejecuta con: mvn test
 */
class ConexionBDTest {

    private ConexionBD conexion;

    @BeforeEach
    void crearTabla() throws SQLException {
        conexion = ConexionBD.desdeRecurso("/db-prueba.properties");
        try (Connection con = conexion.abrir(); Statement st = con.createStatement()) {
            st.execute("DROP TABLE IF EXISTS sitio");
            st.execute("CREATE TABLE sitio (id INT AUTO_INCREMENT PRIMARY KEY, codigo VARCHAR(5) NOT NULL UNIQUE,"
                    + " tipo VARCHAR(10) NOT NULL, capacidad INT NOT NULL, valor INT NOT NULL)");
            st.execute("INSERT INTO sitio (codigo, tipo, capacidad, valor) VALUES ('C-01', 'CARPA', 4, 18000),"
                    + " ('C-02', 'CARPA', 6, 22000), ('Q-01', 'QUINCHO', 12, 45000)");
        }
    }

    // ---------- R3: configuración ----------

    @Test
    void leeLaUrlDesdeElRecurso() {
        assertTrue(conexion.getUrl().startsWith("jdbc:h2:mem:camping"));
    }

    @Test
    void sinUrlLanzaIllegalStateException() {
        Properties propiedades = new Properties();
        propiedades.setProperty("db.usuario", "root");
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> new ConexionBD(propiedades));
        assertTrue(e.getMessage().contains("db.url"), e.getMessage());
    }

    @Test
    void urlEnBlancoTambienSeRechaza() {
        Properties propiedades = new Properties();
        propiedades.setProperty("db.url", "   ");
        propiedades.setProperty("db.usuario", "root");
        assertThrows(IllegalStateException.class, () -> new ConexionBD(propiedades));
    }

    @Test
    void laClavePuedeOmitirse() {
        Properties propiedades = new Properties();
        propiedades.setProperty("db.url", "jdbc:h2:mem:otra");
        propiedades.setProperty("db.usuario", "sa");
        assertDoesNotThrow(() -> new ConexionBD(propiedades));
    }

    @Test
    void recursoInexistente() {
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> ConexionBD.desdeRecurso("/no-existe.properties"));
        assertTrue(e.getMessage().contains("/no-existe.properties"), e.getMessage());
    }

    // ---------- R4: abrir ----------

    @Test
    void abreUnaConexionValida() throws SQLException {
        try (Connection con = conexion.abrir()) {
            assertTrue(con.isValid(2));
        }
    }

    @Test
    void sinDriverParaLaUrlLanzaSQLException() {
        Properties propiedades = new Properties();
        propiedades.setProperty("db.url", "jdbc:postgresql://localhost:5432/camping");
        propiedades.setProperty("db.usuario", "root");
        SQLException e = assertThrows(SQLException.class, () -> new ConexionBD(propiedades).abrir());
        assertTrue(DiagnosticoConexion.explicar(e).contains("driver"), DiagnosticoConexion.explicar(e));
    }

    // ---------- R5: diagnóstico ----------

    @Test
    void credencialesIncorrectas() {
        SQLException e = new SQLException("Access denied for user 'root'@'localhost'", "28000", 1045);
        assertTrue(DiagnosticoConexion.explicar(e).contains("Usuario o contraseña"));
    }

    @Test
    void baseDeDatosInexistente() {
        SQLException e = new SQLException("Unknown database 'camping'", "42000", 1049);
        assertTrue(DiagnosticoConexion.explicar(e).contains("La base de datos no existe"));
    }

    @Test
    void servidorDetenido() {
        SQLException e = new SQLException("Communications link failure", "08S01", 0);
        assertTrue(DiagnosticoConexion.explicar(e).contains("servidor"));
    }

    @Test
    void otroErrorMuestraElCodigo() {
        SQLException e = new SQLException("Table 'sitio' doesn't exist", "42S02", 1146);
        assertTrue(DiagnosticoConexion.explicar(e).contains("código 1146"));
    }

    // ---------- R6: resumen sin fugas ----------

    @Test
    void resumenConDatosDelCamping() throws SQLException {
        String resumen = new ResumenCamping(conexion).generar();
        assertTrue(resumen.startsWith("Conectado a H2"), resumen);
        assertTrue(resumen.contains("3 sitios"), resumen);
        assertTrue(resumen.contains("capacidad total 22 personas"), resumen);
    }

    @Test
    void sinFugasDeConexiones() throws SQLException {
        ResumenCamping resumen = new ResumenCamping(conexion);
        for (int i = 0; i < 5; i++) {
            resumen.generar();
        }
        // H2 lista en INFORMATION_SCHEMA.SESSIONS las conexiones abiertas.
        // Si generar() cierra todo, solo queda la conexión de esta consulta.
        try (Connection con = conexion.abrir();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM INFORMATION_SCHEMA.SESSIONS")) {
            rs.next();
            assertEquals(1, rs.getInt(1), "Quedaron conexiones abiertas: falta try-with-resources");
        }
    }
}
