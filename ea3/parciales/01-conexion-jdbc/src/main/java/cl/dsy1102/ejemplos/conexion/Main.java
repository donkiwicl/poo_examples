package cl.dsy1102.ejemplos.conexion;

import java.sql.SQLException;

/**
 * Prueba la conexión con la base de datos del camping. Ejecuta con:
 *   mvn compile exec:java
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Camping Lago Ranco · prueba de conexión ===");
        try {
            ConexionBD conexion = ConexionBD.desdeRecurso("/db.properties");
            System.out.println("URL: " + conexion.getUrl());
            System.out.println(new ResumenCamping(conexion).generar());
        } catch (SQLException e) {
            System.out.println("No se pudo conectar: " + DiagnosticoConexion.explicar(e));
        } catch (IllegalStateException e) {
            System.out.println("Configuración inválida: " + e.getMessage());
        }
    }
}
