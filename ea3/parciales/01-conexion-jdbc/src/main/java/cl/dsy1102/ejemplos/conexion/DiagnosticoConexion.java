package cl.dsy1102.ejemplos.conexion;

import java.sql.SQLException;

/**
 * Traduce los errores de conexión más comunes a mensajes que el usuario
 * (o quien instala la aplicación) puede entender y corregir.
 */
public final class DiagnosticoConexion {

    private DiagnosticoConexion() {
    }

    /**
     * TODO R5: retorna un mensaje según el error. Usa getErrorCode(),
     * getSQLState() y, solo para el driver, getMessage():
     *
     *  | Situación                         | Cómo se reconoce                         | Mensaje (debe contener)        |
     *  |-----------------------------------|------------------------------------------|--------------------------------|
     *  | Usuario o contraseña incorrectos  | código 1045 o SQLState "28000"           | "Usuario o contraseña"         |
     *  | La base de datos no existe        | código 1049                              | "La base de datos no existe"   |
     *  | No hay driver para la URL         | mensaje empieza con "No suitable driver" | "driver"                       |
     *  | Servidor detenido o puerto malo   | SQLState que empieza con "08"            | "servidor"                     |
     *  | Cualquier otro                    | -                                        | "código <n>: <mensaje>"        |
     */
    public static String explicar(SQLException e) {
        return e.getMessage();
    }
}
