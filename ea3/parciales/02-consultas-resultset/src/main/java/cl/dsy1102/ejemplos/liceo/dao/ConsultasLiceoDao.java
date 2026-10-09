package cl.dsy1102.ejemplos.liceo.dao;

import cl.dsy1102.ejemplos.liceo.model.Alumno;
import cl.dsy1102.ejemplos.liceo.model.PromedioAlumno;
import cl.dsy1102.ejemplos.liceo.model.ResumenAsignatura;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
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

    // Base común de R1, R2 y R3. "c.nombre AS curso" evita el choque con a.nombre.
    private static final String SELECT_ALUMNO =
            "SELECT a.id, a.rut, a.nombre, a.fecha_nacimiento, a.correo_apoderado, c.nombre AS curso"
            + " FROM alumno a JOIN curso c ON c.id = a.curso_id";

    private static final String SQL_PROMEDIOS =
            "SELECT a.nombre, c.nombre AS curso, ROUND(AVG(n.valor), 1) AS promedio, COUNT(n.id) AS cantidad"
            + " FROM alumno a"
            + " JOIN curso c ON c.id = a.curso_id"
            + " LEFT JOIN nota n ON n.alumno_id = a.id"
            + " WHERE c.nombre = ?"
            + " GROUP BY a.id, a.nombre, c.nombre"
            + " ORDER BY promedio DESC, a.nombre";

    private static final String SQL_EN_RIESGO =
            "SELECT a.nombre, c.nombre AS curso, ROUND(AVG(n.valor), 1) AS promedio, COUNT(n.id) AS cantidad"
            + " FROM alumno a"
            + " JOIN curso c ON c.id = a.curso_id"
            + " JOIN nota n ON n.alumno_id = a.id"
            + " GROUP BY a.id, a.nombre, c.nombre"
            + " HAVING AVG(n.valor) < ?"
            + " ORDER BY promedio, a.nombre";

    private static final String SQL_RESUMEN_ASIGNATURA =
            "SELECT asignatura, COUNT(*) AS cantidad, ROUND(AVG(valor), 1) AS promedio,"
            + " MIN(valor) AS minima, MAX(valor) AS maxima"
            + " FROM nota GROUP BY asignatura ORDER BY asignatura";

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

    public List<Alumno> listarAlumnos() throws SQLException {
        String sql = SELECT_ALUMNO + " ORDER BY c.nombre, a.nombre";
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Alumno> alumnos = new ArrayList<>();
            while (rs.next()) {
                alumnos.add(mapearAlumno(rs));
            }
            return alumnos;
        }
    }

    public Optional<Alumno> buscarPorRut(String rut) throws SQLException {
        String sql = SELECT_ALUMNO + " WHERE a.rut = ?";
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, rut);
            // El ResultSet se abre después de asignar los parámetros: va en un try interno.
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapearAlumno(rs)) : Optional.empty();
            }
        }
    }

    public List<Alumno> listarPorCurso(String curso) throws SQLException {
        String sql = SELECT_ALUMNO + " WHERE c.nombre = ? ORDER BY a.nombre";
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, curso);
            try (ResultSet rs = ps.executeQuery()) {
                List<Alumno> alumnos = new ArrayList<>();
                while (rs.next()) {
                    alumnos.add(mapearAlumno(rs));
                }
                return alumnos;
            }
        }
    }

    public List<PromedioAlumno> promediosDelCurso(String curso) throws SQLException {
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(SQL_PROMEDIOS)) {
            ps.setString(1, curso);
            return leerPromedios(ps);
        }
    }

    public List<PromedioAlumno> alumnosEnRiesgo(double limite) throws SQLException {
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(SQL_EN_RIESGO)) {
            ps.setDouble(1, limite);
            return leerPromedios(ps);
        }
    }

    public List<ResumenAsignatura> resumenPorAsignatura() throws SQLException {
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(SQL_RESUMEN_ASIGNATURA);
             ResultSet rs = ps.executeQuery()) {
            List<ResumenAsignatura> resumen = new ArrayList<>();
            while (rs.next()) {
                resumen.add(new ResumenAsignatura(
                        rs.getString("asignatura"),
                        rs.getInt("cantidad"),
                        rs.getDouble("promedio"),
                        rs.getDouble("minima"),
                        rs.getDouble("maxima")));
            }
            return resumen;
        }
    }

    /** Convierte la fila ACTUAL del ResultSet en un Alumno. No llama a next(). */
    private Alumno mapearAlumno(ResultSet rs) throws SQLException {
        return new Alumno(
                rs.getInt("id"),
                rs.getString("rut"),
                rs.getString("nombre"),
                rs.getObject("fecha_nacimiento", LocalDate.class),
                rs.getString("correo_apoderado"), // getString retorna null si la columna es NULL
                rs.getString("curso"));
    }

    private List<PromedioAlumno> leerPromedios(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            List<PromedioAlumno> promedios = new ArrayList<>();
            while (rs.next()) {
                // getDouble retorna 0.0 cuando la columna es NULL. wasNull() se
                // consulta justo después, y se refiere a la última columna leída.
                double valor = rs.getDouble("promedio");
                Double promedio = rs.wasNull() ? null : valor;
                promedios.add(new PromedioAlumno(
                        rs.getString("nombre"), rs.getString("curso"), promedio, rs.getInt("cantidad")));
            }
            return promedios;
        }
    }
}
