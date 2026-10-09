package cl.dsy1102.ejemplos.liceo.dao;

import cl.dsy1102.ejemplos.liceo.model.Alumno;
import cl.dsy1102.ejemplos.liceo.model.PromedioAlumno;
import cl.dsy1102.ejemplos.liceo.model.ResumenAsignatura;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Consultas de solo lectura sobre la base de datos del liceo.
 *
 * Reglas para todos los métodos:
 *  - try-with-resources para Connection, PreparedStatement y ResultSet;
 *  - leer las columnas por NOMBRE (o alias), nunca por posición;
 *  - los valores que vienen del usuario van como parámetros (?), no concatenados.
 */
public class ConsultasLiceoDao {

    private final ConexionBD conexion;

    public ConsultasLiceoDao(ConexionBD conexion) {
        this.conexion = conexion;
    }

    /** Ejemplo resuelto: una consulta que retorna un solo valor. */
    public int contarAlumnos() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM alumno";
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next(); // COUNT siempre retorna exactamente una fila
            return rs.getInt("total");
        }
    }

    /**
     * TODO R1: todos los alumnos con el nombre de su curso (JOIN), ordenados por
     * curso y luego por nombre. Escribe un método privado mapearAlumno(ResultSet)
     * que convierta la fila actual en un Alumno:
     *  - la fecha se lee con rs.getObject("fecha_nacimiento", LocalDate.class);
     *  - correo_apoderado puede ser NULL;
     *  - las tablas alumno y curso tienen ambas una columna "nombre": usa un alias.
     */
    public List<Alumno> listarAlumnos() throws SQLException {
        throw new UnsupportedOperationException("TODO R1");
    }

    /**
     * TODO R2: el alumno con ese RUT, u Optional.empty() si no existe.
     * Reutiliza mapearAlumno.
     */
    public Optional<Alumno> buscarPorRut(String rut) throws SQLException {
        throw new UnsupportedOperationException("TODO R2");
    }

    /**
     * TODO R3: los alumnos de un curso ("3°A"), ordenados por nombre.
     * Un curso inexistente retorna una lista vacía.
     */
    public List<Alumno> listarPorCurso(String curso) throws SQLException {
        throw new UnsupportedOperationException("TODO R3");
    }

    /**
     * TODO R4: el promedio de cada alumno del curso, redondeado a un decimal
     * (ROUND(AVG(...), 1)), del mayor al menor promedio. Los alumnos SIN notas
     * también aparecen, con promedio null y 0 notas, al final de la lista.
     * Pista: LEFT JOIN y GROUP BY. Ojo con getDouble y los NULL.
     */
    public List<PromedioAlumno> promediosDelCurso(String curso) throws SQLException {
        throw new UnsupportedOperationException("TODO R4");
    }

    /**
     * TODO R5: alumnos de cualquier curso cuyo promedio es menor que el límite,
     * del menor al mayor promedio. Los alumnos sin notas no están en riesgo.
     * Pista: HAVING.
     */
    public List<PromedioAlumno> alumnosEnRiesgo(double limite) throws SQLException {
        throw new UnsupportedOperationException("TODO R5");
    }

    /**
     * TODO R6: por cada asignatura, en orden alfabético: cantidad de notas,
     * promedio (un decimal), nota mínima y nota máxima.
     */
    public List<ResumenAsignatura> resumenPorAsignatura() throws SQLException {
        throw new UnsupportedOperationException("TODO R6");
    }
}
