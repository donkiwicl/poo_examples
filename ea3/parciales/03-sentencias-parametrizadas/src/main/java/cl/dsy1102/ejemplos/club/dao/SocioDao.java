package cl.dsy1102.ejemplos.club.dao;

import cl.dsy1102.ejemplos.club.model.Categoria;
import cl.dsy1102.ejemplos.club.model.Socio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
     * TODO R1: misma búsqueda con PreparedStatement. Sin distinguir mayúsculas
     * ("tapia" encuentra a "Florencia Tapia"). Los % del LIKE van en el
     * VALOR del parámetro, no en el SQL.
     */
    public List<Socio> buscarPorNombre(String texto) throws SQLException {
        throw new UnsupportedOperationException("TODO R1");
    }

    /**
     * TODO R2: inserta el socio y le asigna el id generado por la base de datos
     * (Statement.RETURN_GENERATED_KEYS y getGeneratedKeys()).
     * Si el RUT ya existe, lanza SocioDuplicadoException("Ya existe un socio con el RUT <rut>.").
     * Pista: el driver lanza SQLIntegrityConstraintViolationException.
     */
    public void insertar(Socio socio) throws SocioDuplicadoException, SQLException {
        throw new UnsupportedOperationException("TODO R2");
    }

    /**
     * TODO R3: actualiza nombre, categoría, cuota y estado del socio con ese id.
     * Retorna true si se modificó una fila; false si el id no existe.
     */
    public boolean actualizar(Socio socio) throws SQLException {
        throw new UnsupportedOperationException("TODO R3");
    }

    /** TODO R4: elimina por id. Retorna true si existía. */
    public boolean eliminar(int id) throws SQLException {
        throw new UnsupportedOperationException("TODO R4");
    }

    /**
     * TODO R5: reajusta en un porcentaje (10 = +10 %) la cuota de los socios
     * ACTIVOS de una categoría, redondeando al peso. Retorna cuántos socios
     * cambiaron. Un solo UPDATE: no recorras los socios en Java.
     */
    public int reajustarCuotas(Categoria categoria, double porcentaje) throws SQLException {
        throw new UnsupportedOperationException("TODO R5");
    }

    /**
     * TODO R6: socios de una categoría que ingresaron desde la fecha indicada
     * (inclusive), del más antiguo al más nuevo. Usa setObject para la fecha.
     */
    public List<Socio> filtrar(Categoria categoria, LocalDate desde) throws SQLException {
        throw new UnsupportedOperationException("TODO R6");
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
