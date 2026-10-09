package cl.dsy1102.almacen;

import cl.dsy1102.almacen.dao.ConexionBD;
import cl.dsy1102.almacen.dao.JdbcProductoDao;
import cl.dsy1102.almacen.dao.JdbcVentaDao;
import cl.dsy1102.almacen.dao.PersistenciaException;
import cl.dsy1102.almacen.dao.ProductoDao;
import cl.dsy1102.almacen.dao.VentaDao;
import cl.dsy1102.almacen.model.Categoria;
import cl.dsy1102.almacen.model.Producto;
import cl.dsy1102.almacen.model.ProductoVendido;
import cl.dsy1102.almacen.model.ResumenCategoria;
import cl.dsy1102.almacen.model.Venta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de JdbcVentaDao (R5 y R6) con H2.
 *
 * sql/datos.sql trae 3 ventas: una de hace 3 días ($6.550) y dos de ayer ($15.730 y $6.070).
 */
class JdbcVentaDaoTest {

    private static final LocalDate HOY = LocalDate.now();

    private ProductoDao productos;
    private VentaDao ventas;

    @BeforeEach
    void crearBase() throws Exception {
        ConexionBD conexion = BaseDePrueba.crear();
        productos = new JdbcProductoDao(conexion);
        ventas = new JdbcVentaDao(conexion);
    }

    private Producto buscar(String codigo) throws PersistenciaException {
        return productos.listarTodos().stream().filter(p -> p.getCodigo().equals(codigo)).findFirst().orElseThrow();
    }

    // ---------- R5: registrar en una transacción ----------

    @Test
    void registrarGuardaLaVentaYDescuentaStock() throws Exception {
        Venta venta = new Venta();
        venta.agregar(buscar("AB-003"), 2, HOY);   // 2 x $2.790
        venta.agregar(buscar("LA-002"), 3, HOY);   // vence en 2 días: 3 x $273
        ventas.registrar(venta);

        assertTrue(venta.getId() > 3, "registrar asigna el id generado");
        assertEquals(2, buscar("AB-003").getStock());
        assertEquals(15, buscar("LA-002").getStock());
        assertEquals(1, ventas.contarVentas(HOY, HOY), "La venta quedó registrada hoy");
        List<ResumenCategoria> hoy = ventas.ventasPorCategoria(HOY, HOY);
        assertEquals(List.of(new ResumenCategoria(Categoria.ABARROTES, 2, 5580),
                new ResumenCategoria(Categoria.LACTEOS, 3, 819)), hoy);
    }

    @Test
    void siOtraVentaSeLlevoElStockNoSeGuardaNada() throws Exception {
        Producto aceite = buscar("AB-003");          // stock 4
        Venta mia = new Venta();
        mia.agregar(buscar("AB-002"), 1, HOY);       // esta línea sí tiene stock
        mia.agregar(aceite, 4, HOY);

        Venta otra = new Venta();                    // otra caja vende 2 aceites antes
        otra.agregar(aceite, 2, HOY);
        ventas.registrar(otra);

        PersistenciaException e = assertThrows(PersistenciaException.class, () -> ventas.registrar(mia));
        assertTrue(e.getMessage().startsWith("Stock insuficiente de Aceite maravilla 1 L"), e.getMessage());
        assertEquals(0, mia.getId(), "La venta rechazada no tiene id");
        assertEquals(2, buscar("AB-003").getStock());
        assertEquals(40, buscar("AB-002").getStock(), "Rollback: la primera línea tampoco descontó stock");
        assertEquals(1, ventas.contarVentas(HOY, HOY), "Solo quedó registrada la otra venta");
    }

    @Test
    void ventaVaciaNoSeRegistra() {
        assertThrows(IllegalArgumentException.class, () -> ventas.registrar(new Venta()));
    }

    // ---------- R6: reportes ----------

    @Test
    void ventasPorCategoriaUltimos7Dias() throws Exception {
        assertEquals(List.of(
                new ResumenCategoria(Categoria.ABARROTES, 7, 8430),
                new ResumenCategoria(Categoria.LIMPIEZA, 1, 7990),
                new ResumenCategoria(Categoria.LACTEOS, 5, 5950),
                new ResumenCategoria(Categoria.BEBIDAS, 2, 3780),
                new ResumenCategoria(Categoria.PANADERIA, 1, 2200)), ventas.ventasPorCategoria(HOY.minusDays(6), HOY));
    }

    @Test
    void elPeriodoIncluyeElDiaCompleto() throws Exception {
        List<ResumenCategoria> ayer = ventas.ventasPorCategoria(HOY.minusDays(1), HOY.minusDays(1));
        assertEquals(5, ayer.size());
        assertEquals(new ResumenCategoria(Categoria.LIMPIEZA, 1, 7990), ayer.get(0));
        assertEquals(new ResumenCategoria(Categoria.ABARROTES, 5, 5450), ayer.get(1));
        assertTrue(ventas.ventasPorCategoria(HOY, HOY).isEmpty());
    }

    @Test
    void masVendidos() throws Exception {
        assertEquals(List.of(
                new ProductoVendido("LA-001", "Leche entera 1 L", 5, 5950),
                new ProductoVendido("AB-002", "Fideos spaghetti 400 g", 4, 3960),
                new ProductoVendido("AB-001", "Arroz grado 1 1 kg", 3, 4470)), ventas.masVendidos(HOY.minusDays(6), HOY, 3));
    }

    @Test
    void contarVentas() throws Exception {
        assertEquals(3, ventas.contarVentas(HOY.minusDays(6), HOY));
        assertEquals(2, ventas.contarVentas(HOY.minusDays(1), HOY));
        assertEquals(0, ventas.contarVentas(HOY, HOY));
    }
}
