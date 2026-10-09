package cl.dsy1102.ejemplos.club.dao;

import cl.dsy1102.ejemplos.club.model.Categoria;
import cl.dsy1102.ejemplos.club.model.Socio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a la tabla socio.
 */
public class SocioDao {

    private static final String COLUMNAS = "id, rut, nombre, categoria, cuota_mensual, fecha_ingreso, activo";

    private final ConexionBD conexion;

    public SocioDao(ConexionBD conexion) {
        this.conexion = conexion;
    }

    /** Resuelto: todos los socios, ordenados por nombre. */
    public List<Socio> listarTodos() throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM socio ORDER BY nombre";
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapearTodos(rs);
        }
    }

    /**
     * NO USAR: versión vulnerable, que se deja para comparar.
     * Concatena el texto del usuario dentro del SQL.
     */
    public List<Socio> buscarPorNombreInseguro(String texto) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM socio WHERE nombre LIKE '%" + texto + "%' ORDER BY nombre";
        try (Connection con = conexion.abrir();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return mapearTodos(rs);
        }
    }

    /**
     * Misma búsqueda que la insegura, con PreparedStatement. LOWER en ambos
     * lados: MySQL compara sin distinguir mayúsculas, pero H2 y otros motores sí.
     */
    public List<Socio> buscarPorNombre(String texto) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM socio WHERE LOWER(nombre) LIKE LOWER(?) ORDER BY nombre";
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            // Los comodines son parte del VALOR. "LIKE '%?%'" no funcionaría:
            // dentro de comillas, ? es un carácter, no un parámetro.
            ps.setString(1, "%" + texto + "%");
            try (ResultSet rs = ps.executeQuery()) {
                return mapearTodos(rs);
            }
        }
    }

    /**
     * Inserta el socio y le asigna el id generado por la base de datos.
     */
    public void insertar(Socio socio) throws SocioDuplicadoException, SQLException {
        String sql = "INSERT INTO socio (rut, nombre, categoria, cuota_mensual, fecha_ingreso, activo)"
                + " VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, socio.getRut());
            ps.setString(2, socio.getNombre());
            ps.setString(3, socio.getCategoria().name());
            ps.setInt(4, socio.getCuotaMensual());
            ps.setObject(5, socio.getFechaIngreso());
            ps.setBoolean(6, socio.isActivo());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    socio.setId(claves.getInt(1)); // la clave generada no tiene nombre de columna fijo
                }
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            // La única restricción que puede fallar en un INSERT de esta tabla es UNIQUE(rut):
            // los NOT NULL y CHECK ya los protege el modelo.
            throw new SocioDuplicadoException("Ya existe un socio con el RUT " + socio.getRut() + ".", e);
        }
    }

    /**
     * Actualiza nombre, categoría, cuota y estado. Retorna false si el id no existe.
     */
    public boolean actualizar(Socio socio) throws SQLException {
        String sql = "UPDATE socio SET nombre = ?, categoria = ?, cuota_mensual = ?, activo = ? WHERE id = ?";
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, socio.getNombre());
            ps.setString(2, socio.getCategoria().name());
            ps.setInt(3, socio.getCuotaMensual());
            ps.setBoolean(4, socio.isActivo());
            ps.setInt(5, socio.getId()); // los parámetros se numeran por su posición en el SQL
            return ps.executeUpdate() == 1;
        }
    }

    /** Elimina por id. Retorna true si existía. */
    public boolean eliminar(int id) throws SQLException {
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement("DELETE FROM socio WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Reajusta en un porcentaje (10 = +10 %) la cuota de los socios activos de
     * una categoría. Retorna cuántos socios cambiaron.
     */
    public int reajustarCuotas(Categoria categoria, double porcentaje) throws SQLException {
        // CAST: algunos motores (H2) deducen el tipo del ? a partir de la otra
        // columna de la operación (INT) y truncarían 1.055 a 1.
        String sql = "UPDATE socio SET cuota_mensual = ROUND(cuota_mensual * CAST(? AS DECIMAL(6,3)))"
                + " WHERE categoria = ? AND activo = TRUE";
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, 1 + porcentaje / 100);
            ps.setString(2, categoria.name());
            return ps.executeUpdate();
        }
    }

    /**
     * Socios de una categoría que ingresaron desde la fecha indicada (inclusive).
     */
    public List<Socio> filtrar(Categoria categoria, LocalDate desde) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM socio WHERE categoria = ? AND fecha_ingreso >= ?"
                + " ORDER BY fecha_ingreso, nombre";
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, categoria.name());
            ps.setObject(2, desde); // LocalDate -> DATE, sin pasar por texto
            try (ResultSet rs = ps.executeQuery()) {
                return mapearTodos(rs);
            }
        }
    }

    private List<Socio> mapearTodos(ResultSet rs) throws SQLException {
        List<Socio> socios = new ArrayList<>();
        while (rs.next()) {
            socios.add(new Socio(
                    rs.getInt("id"),
                    rs.getString("rut"),
                    rs.getString("nombre"),
                    Categoria.valueOf(rs.getString("categoria")),
                    rs.getInt("cuota_mensual"),
                    rs.getObject("fecha_ingreso", LocalDate.class),
                    rs.getBoolean("activo")));
        }
        return socios;
    }
}
