package cl.dsy1102.ejemplos.lavanderia.dao;

import cl.dsy1102.ejemplos.lavanderia.database.ConexionBD;
import cl.dsy1102.ejemplos.lavanderia.model.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación JDBC de ClienteDao. Es la única clase que contiene SQL.
 */
public class ClienteDaoJdbc implements ClienteDao {

    private static final String COLUMNAS = "id, nombre, telefono, correo, comuna";
    private static final String SQL_LISTAR = "SELECT " + COLUMNAS + " FROM cliente ORDER BY nombre";
    private static final String SQL_BUSCAR =
            "SELECT " + COLUMNAS + " FROM cliente WHERE LOWER(nombre) LIKE LOWER(?) ORDER BY nombre";
    private static final String SQL_INSERTAR =
            "INSERT INTO cliente (nombre, telefono, correo, comuna) VALUES (?, ?, ?, ?)";
    private static final String SQL_ACTUALIZAR =
            "UPDATE cliente SET nombre = ?, telefono = ?, correo = ?, comuna = ? WHERE id = ?";
    private static final String SQL_ELIMINAR = "DELETE FROM cliente WHERE id = ?";

    private final ConexionBD conexion;

    public ClienteDaoJdbc(ConexionBD conexion) {
        this.conexion = conexion;
    }

    @Override
    public List<Cliente> listarTodos() throws DaoException {
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {
            return mapearTodos(rs);
        } catch (SQLException e) {
            throw traducir("leer los clientes", e);
        }
    }

    @Override
    public List<Cliente> buscarPorNombre(String texto) throws DaoException {
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(SQL_BUSCAR)) {
            ps.setString(1, "%" + texto + "%");
            try (ResultSet rs = ps.executeQuery()) {
                return mapearTodos(rs);
            }
        } catch (SQLException e) {
            throw traducir("buscar clientes", e);
        }
    }

    @Override
    public void insertar(Cliente cliente) throws DaoException {
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            asignarDatos(ps, cliente);
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    cliente.setId(claves.getInt(1));
                }
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw correoRepetido(cliente, e);
        } catch (SQLException e) {
            throw traducir("guardar el cliente", e);
        }
    }

    @Override
    public boolean actualizar(Cliente cliente) throws DaoException {
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR)) {
            asignarDatos(ps, cliente);
            ps.setInt(5, cliente.getId());
            return ps.executeUpdate() == 1;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw correoRepetido(cliente, e);
        } catch (SQLException e) {
            throw traducir("actualizar el cliente", e);
        }
    }

    @Override
    public boolean eliminar(int id) throws DaoException {
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(SQL_ELIMINAR)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw traducir("eliminar el cliente", e);
        }
    }

    /** Los cuatro primeros parámetros son iguales en INSERT y UPDATE. */
    private void asignarDatos(PreparedStatement ps, Cliente cliente) throws SQLException {
        ps.setString(1, cliente.getNombre());
        ps.setString(2, cliente.getTelefono());
        ps.setString(3, cliente.getCorreo());
        ps.setString(4, cliente.getComuna());
    }

    private List<Cliente> mapearTodos(ResultSet rs) throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        while (rs.next()) {
            clientes.add(new Cliente(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("telefono"),
                    rs.getString("correo"),
                    rs.getString("comuna")));
        }
        return clientes;
    }

    private DaoException correoRepetido(Cliente cliente, SQLException e) {
        return new DaoException("Ya existe un cliente con el correo " + cliente.getCorreo() + ".", e);
    }

    /** Convierte la SQLException en un mensaje para el usuario. La original queda como causa. */
    private DaoException traducir(String accion, SQLException e) {
        String estado = e.getSQLState() == null ? "" : e.getSQLState();
        if (estado.startsWith("08")) {
            return new DaoException("No hay conexión con la base de datos. Revisa que MySQL esté iniciado"
                    + " y los datos de db.properties.", e);
        }
        return new DaoException("No se pudo " + accion + ": " + e.getMessage(), e);
    }
}
