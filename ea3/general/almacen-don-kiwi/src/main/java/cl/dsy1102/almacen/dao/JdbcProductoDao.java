package cl.dsy1102.almacen.dao;

import cl.dsy1102.almacen.model.Categoria;
import cl.dsy1102.almacen.model.Producto;
import cl.dsy1102.almacen.model.ProductoNoPerecible;
import cl.dsy1102.almacen.model.ProductoPerecible;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * ProductoDao con MySQL. Reemplaza a JsonProductoDao sin que cambien los
 * repositorios ni los controladores.
 *
 * Herencia en una sola tabla: la columna tipo indica la subclase y las
 * columnas fecha_vencimiento y marca quedan en NULL cuando no corresponden.
 */
public class JdbcProductoDao implements ProductoDao {

    private static final String PERECIBLE = "PERECIBLE";
    private static final String NO_PERECIBLE = "NO_PERECIBLE";

    private static final String SQL_LISTAR =
            "SELECT id, codigo, nombre, tipo, categoria, precio, stock, fecha_vencimiento, marca"
            + " FROM producto ORDER BY codigo";
    private static final String SQL_INSERTAR =
            "INSERT INTO producto (codigo, nombre, tipo, categoria, precio, stock, fecha_vencimiento, marca)"
            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_ACTUALIZAR =
            "UPDATE producto SET codigo = ?, nombre = ?, tipo = ?, categoria = ?, precio = ?, stock = ?,"
            + " fecha_vencimiento = ?, marca = ? WHERE id = ?";
    private static final String SQL_ELIMINAR = "DELETE FROM producto WHERE id = ?";

    private final ConexionBD conexion;

    public JdbcProductoDao(ConexionBD conexion) {
        this.conexion = conexion;
    }

    @Override
    public List<Producto> listarTodos() throws PersistenciaException {
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {
            List<Producto> productos = new ArrayList<>();
            while (rs.next()) {
                productos.add(mapear(rs));
            }
            return productos;
        } catch (SQLException e) {
            throw ErroresSql.traducir("leer los productos", e);
        }
    }

    @Override
    public void insertar(Producto producto) throws PersistenciaException {
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            asignarDatos(ps, producto);
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    producto.setId(claves.getInt(1));
                }
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw codigoRepetido(producto, e);
        } catch (SQLException e) {
            throw ErroresSql.traducir("guardar el producto", e);
        }
    }

    @Override
    public boolean actualizar(Producto producto) throws PersistenciaException {
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR)) {
            asignarDatos(ps, producto);
            ps.setInt(9, producto.getId());
            return ps.executeUpdate() == 1;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw codigoRepetido(producto, e);
        } catch (SQLException e) {
            throw ErroresSql.traducir("actualizar el producto", e);
        }
    }

    @Override
    public boolean eliminar(int id) throws PersistenciaException {
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(SQL_ELIMINAR)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLIntegrityConstraintViolationException e) {
            // En un DELETE, la restricción que puede fallar es la clave foránea de detalle_venta.
            throw new PersistenciaException("No se puede eliminar el producto: tiene ventas registradas."
                    + " Si ya no se vende, deja su stock en 0.", e);
        } catch (SQLException e) {
            throw ErroresSql.traducir("eliminar el producto", e);
        }
    }

    /** Parámetros 1 a 8, comunes a INSERT y UPDATE. */
    private void asignarDatos(PreparedStatement ps, Producto producto) throws SQLException {
        ps.setString(1, producto.getCodigo());
        ps.setString(2, producto.getNombre());
        ps.setString(3, producto instanceof ProductoPerecible ? PERECIBLE : NO_PERECIBLE);
        ps.setString(4, producto.getCategoria().name());
        ps.setInt(5, producto.getPrecio());
        ps.setInt(6, producto.getStock());
        if (producto instanceof ProductoPerecible perecible) {
            ps.setObject(7, perecible.getFechaVencimiento());
            ps.setNull(8, Types.VARCHAR);
        } else if (producto instanceof ProductoNoPerecible noPerecible) {
            ps.setNull(7, Types.DATE);
            ps.setString(8, noPerecible.getMarca());
        }
    }

    /** Crea la subclase que indica la columna tipo. */
    private Producto mapear(ResultSet rs) throws SQLException {
        String codigo = rs.getString("codigo");
        String nombre = rs.getString("nombre");
        Categoria categoria = Categoria.valueOf(rs.getString("categoria"));
        int precio = rs.getInt("precio");
        int stock = rs.getInt("stock");
        String tipo = rs.getString("tipo");

        Producto producto = switch (tipo) {
            case PERECIBLE -> new ProductoPerecible(codigo, nombre, categoria, precio, stock,
                    rs.getObject("fecha_vencimiento", LocalDate.class));
            case NO_PERECIBLE -> new ProductoNoPerecible(codigo, nombre, categoria, precio, stock,
                    rs.getString("marca"));
            default -> throw new SQLException("Tipo de producto desconocido en la base de datos: " + tipo);
        };
        producto.setId(rs.getInt("id"));
        return producto;
    }

    private PersistenciaException codigoRepetido(Producto producto, SQLException e) {
        return new PersistenciaException("Ya existe un producto con el código " + producto.getCodigo() + ".", e);
    }
}
