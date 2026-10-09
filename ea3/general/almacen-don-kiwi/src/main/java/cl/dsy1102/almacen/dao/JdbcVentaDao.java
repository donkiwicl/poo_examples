package cl.dsy1102.almacen.dao;

import cl.dsy1102.almacen.model.Categoria;
import cl.dsy1102.almacen.model.LineaVenta;
import cl.dsy1102.almacen.model.ProductoVendido;
import cl.dsy1102.almacen.model.ResumenCategoria;
import cl.dsy1102.almacen.model.Venta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * VentaDao con MySQL: registro de ventas en una transacción y reportes con SQL.
 */
public class JdbcVentaDao implements VentaDao {

    private static final String SQL_VENTA = "INSERT INTO venta (fecha_hora, total) VALUES (?, ?)";
    // Descuenta solo si alcanza: verifica y modifica en una sola operación atómica.
    private static final String SQL_DESCONTAR =
            "UPDATE producto SET stock = stock - ? WHERE id = ? AND stock >= ?";
    private static final String SQL_DETALLE =
            "INSERT INTO detalle_venta (venta_id, producto_id, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";

    // Período: desde el inicio de "desde" hasta antes del inicio del día siguiente a "hasta".
    private static final String FILTRO_PERIODO = " WHERE v.fecha_hora >= ? AND v.fecha_hora < ?";

    private static final String SQL_POR_CATEGORIA =
            "SELECT p.categoria, SUM(d.cantidad) AS unidades, SUM(d.cantidad * d.precio_unitario) AS total"
            + " FROM detalle_venta d"
            + " JOIN venta v ON v.id = d.venta_id"
            + " JOIN producto p ON p.id = d.producto_id"
            + FILTRO_PERIODO
            + " GROUP BY p.categoria"
            + " ORDER BY total DESC";

    private static final String SQL_MAS_VENDIDOS =
            "SELECT p.codigo, p.nombre, SUM(d.cantidad) AS unidades, SUM(d.cantidad * d.precio_unitario) AS total"
            + " FROM detalle_venta d"
            + " JOIN venta v ON v.id = d.venta_id"
            + " JOIN producto p ON p.id = d.producto_id"
            + FILTRO_PERIODO
            + " GROUP BY p.id, p.codigo, p.nombre"
            + " ORDER BY unidades DESC, total DESC"
            + " LIMIT ?";

    private static final String SQL_CONTAR = "SELECT COUNT(*) AS cantidad FROM venta v" + FILTRO_PERIODO;

    private final ConexionBD conexion;

    public JdbcVentaDao(ConexionBD conexion) {
        this.conexion = conexion;
    }

    @Override
    public void registrar(Venta venta) throws PersistenciaException {
        if (venta.estaVacia()) {
            throw new IllegalArgumentException("La venta no tiene productos.");
        }
        try (Connection con = conexion.abrir()) {
            con.setAutoCommit(false);
            try {
                int id = insertarVenta(con, venta);
                descontarStockYGuardarDetalle(con, id, venta);
                con.commit();
                venta.setId(id); // solo después del commit: antes, la venta podía deshacerse
            } catch (SQLException | PersistenciaException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw ErroresSql.traducir("registrar la venta", e);
        }
    }

    private int insertarVenta(Connection con, Venta venta) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SQL_VENTA, Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, venta.getFechaHora());
            ps.setInt(2, venta.getTotal());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                claves.next();
                return claves.getInt(1);
            }
        }
    }

    private void descontarStockYGuardarDetalle(Connection con, int ventaId, Venta venta)
            throws SQLException, PersistenciaException {
        try (PreparedStatement descontar = con.prepareStatement(SQL_DESCONTAR);
             PreparedStatement detalle = con.prepareStatement(SQL_DETALLE)) {
            for (LineaVenta linea : venta.getLineas()) {
                descontar.setInt(1, linea.getCantidad());
                descontar.setInt(2, linea.getProductoId());
                descontar.setInt(3, linea.getCantidad());
                if (descontar.executeUpdate() == 0) {
                    // Entre que se armó el carro y se confirmó, otra venta se llevó las unidades.
                    throw new PersistenciaException("Stock insuficiente de " + linea.getNombre()
                            + ": otra venta se registró antes. Revisa la venta y vuelve a confirmar.");
                }
                detalle.setInt(1, ventaId);
                detalle.setInt(2, linea.getProductoId());
                detalle.setInt(3, linea.getCantidad());
                detalle.setInt(4, linea.getPrecioUnitario());
                detalle.addBatch();
            }
            detalle.executeBatch();
        }
    }

    @Override
    public List<ResumenCategoria> ventasPorCategoria(LocalDate desde, LocalDate hasta) throws PersistenciaException {
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(SQL_POR_CATEGORIA)) {
            asignarPeriodo(ps, desde, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                List<ResumenCategoria> resumen = new ArrayList<>();
                while (rs.next()) {
                    resumen.add(new ResumenCategoria(Categoria.valueOf(rs.getString("categoria")),
                            rs.getInt("unidades"), rs.getInt("total")));
                }
                return resumen;
            }
        } catch (SQLException e) {
            throw ErroresSql.traducir("consultar las ventas por categoría", e);
        }
    }

    @Override
    public List<ProductoVendido> masVendidos(LocalDate desde, LocalDate hasta, int limite)
            throws PersistenciaException {
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(SQL_MAS_VENDIDOS)) {
            asignarPeriodo(ps, desde, hasta);
            ps.setInt(3, limite);
            try (ResultSet rs = ps.executeQuery()) {
                List<ProductoVendido> productos = new ArrayList<>();
                while (rs.next()) {
                    productos.add(new ProductoVendido(rs.getString("codigo"), rs.getString("nombre"),
                            rs.getInt("unidades"), rs.getInt("total")));
                }
                return productos;
            }
        } catch (SQLException e) {
            throw ErroresSql.traducir("consultar los productos más vendidos", e);
        }
    }

    @Override
    public int contarVentas(LocalDate desde, LocalDate hasta) throws PersistenciaException {
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(SQL_CONTAR)) {
            asignarPeriodo(ps, desde, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("cantidad");
            }
        } catch (SQLException e) {
            throw ErroresSql.traducir("contar las ventas", e);
        }
    }

    private void asignarPeriodo(PreparedStatement ps, LocalDate desde, LocalDate hasta) throws SQLException {
        ps.setObject(1, desde.atStartOfDay());
        ps.setObject(2, hasta.plusDays(1).atStartOfDay());
    }
}
