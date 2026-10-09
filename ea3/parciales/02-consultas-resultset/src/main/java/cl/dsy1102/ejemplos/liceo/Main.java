package cl.dsy1102.ejemplos.liceo;

import cl.dsy1102.ejemplos.liceo.dao.ConexionBD;
import cl.dsy1102.ejemplos.liceo.dao.ConsultasLiceoDao;

import java.sql.SQLException;

/**
 * Reporte de notas del Liceo Bicentenario de Talca. Ejecuta con:
 *   mvn compile exec:java
 */
public class Main {

    public static void main(String[] args) {
        ConsultasLiceoDao dao = new ConsultasLiceoDao(ConexionBD.desdeRecurso("/db.properties"));
        try {
            System.out.println("=== Liceo Bicentenario de Talca · " + dao.contarAlumnos() + " alumnos ===");

            System.out.println("\n-- Nómina");
            dao.listarAlumnos().forEach(System.out::println);

            System.out.println("\n-- Buscar 21.456.789-K");
            System.out.println(dao.buscarPorRut("21.456.789-K").map(Object::toString).orElse("No existe"));
            System.out.println("-- Buscar 11.111.111-1");
            System.out.println(dao.buscarPorRut("11.111.111-1").map(Object::toString).orElse("No existe"));

            for (String curso : new String[]{"3°A", "3°B"}) {
                System.out.println("\n-- Promedios " + curso);
                dao.promediosDelCurso(curso).forEach(System.out::println);
            }

            System.out.println("\n-- En riesgo (promedio menor que 4,0)");
            dao.alumnosEnRiesgo(4.0).forEach(System.out::println);

            System.out.println("\n-- Por asignatura");
            dao.resumenPorAsignatura().forEach(System.out::println);
        } catch (SQLException e) {
            System.out.println("Error de base de datos: " + e.getMessage());
        }
    }
}
