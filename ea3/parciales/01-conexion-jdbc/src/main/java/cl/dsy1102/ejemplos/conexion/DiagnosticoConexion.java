package cl.dsy1102.ejemplos.conexion;

import java.sql.SQLException;

/**
 * Traduce los errores de conexión más comunes a mensajes que el usuario
 * (o quien instala la aplicación) puede entender y corregir.
 */
public final class DiagnosticoConexion {

    private static final int ACCESO_DENEGADO = 1045;
    private static final int BASE_DESCONOCIDA = 1049;

    private DiagnosticoConexion() {
    }

    public static String explicar(SQLException e) {
        String estado = e.getSQLState() == null ? "" : e.getSQLState();
        String mensaje = e.getMessage() == null ? "" : e.getMessage();

        if (e.getErrorCode() == ACCESO_DENEGADO || estado.equals("28000")) {
            return "Usuario o contraseña incorrectos. Revisa db.usuario y db.clave en db.properties.";
        }
        if (e.getErrorCode() == BASE_DESCONOCIDA) {
            return "La base de datos no existe. Ejecuta sql/camping.sql en tu servidor MySQL.";
        }
        if (mensaje.startsWith("No suitable driver")) {
            return "No hay un driver JDBC para la URL. Revisa la dependencia mysql-connector-j en el pom.xml"
                    + " y que db.url empiece con jdbc:mysql://";
        }
        if (estado.startsWith("08")) {
            return "No se pudo contactar al servidor. Revisa que MySQL esté iniciado y el host y el puerto de db.url.";
        }
        return "Error de base de datos (código " + e.getErrorCode() + ": " + mensaje + ")";
    }
}
