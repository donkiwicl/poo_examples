package cl.dsy1102.ejemplos.conexion;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Consulta un resumen de la base de datos del camping.
 */
public class ResumenCamping {

    private final ConexionBD conexion;

    public ResumenCamping(ConexionBD conexion) {
        this.conexion = conexion;
    }

    /**
     * Retorna, por ejemplo:
     * "Conectado a MySQL 8.4.9 · 3 sitios · capacidad total 22 personas"
     *
     * TODO R6: este método funciona, pero deja abiertos la conexión, el
     * Statement y el ResultSet cada vez que se llama. Reescríbelo con
     * try-with-resources (la prueba sinFugasDeConexiones lo comprueba).
     */
    public String generar() throws SQLException {
        Connection con = conexion.abrir();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT COUNT(*) AS sitios, SUM(capacidad) AS capacidad FROM sitio");
        rs.next();
        int sitios = rs.getInt("sitios");
        int capacidad = rs.getInt("capacidad");
        DatabaseMetaData datos = con.getMetaData();
        return "Conectado a " + datos.getDatabaseProductName() + " " + datos.getDatabaseProductVersion()
                + " · " + sitios + " sitios · capacidad total " + capacidad + " personas";
    }
}
