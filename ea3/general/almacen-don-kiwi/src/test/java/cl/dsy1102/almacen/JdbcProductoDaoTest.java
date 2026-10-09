package cl.dsy1102.almacen;

import cl.dsy1102.almacen.dao.ConexionBD;
import cl.dsy1102.almacen.dao.JdbcProductoDao;
import cl.dsy1102.almacen.dao.PersistenciaException;
import cl.dsy1102.almacen.dao.ProductoDao;
import cl.dsy1102.almacen.model.Categoria;
import cl.dsy1102.almacen.model.Producto;
import cl.dsy1102.almacen.model.ProductoNoPerecible;
import cl.dsy1102.almacen.model.ProductoPerecible;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de JdbcProductoDao (R3) con H2: ejecutan TU sql/tablas.sql y el sql/datos.sql entregado.
 * Ejecuta con: mvn test
 */
class JdbcProductoDaoTest {

    private ProductoDao dao;

    @BeforeEach
    void crearBase() throws Exception {
        dao = new JdbcProductoDao(BaseDePrueba.crear());
    }

    private Producto buscar(String codigo) throws PersistenciaException {
        return dao.listarTodos().stream().filter(p -> p.getCodigo().equals(codigo)).findFirst().orElse(null);
    }

    @Test
    void listarTodosOrdenadosPorCodigo() throws Exception {
        List<Producto> productos = dao.listarTodos();
        assertEquals(12, productos.size());
        assertEquals("AB-001", productos.get(0).getCodigo());
        assertEquals("PA-001", productos.get(11).getCodigo());
        assertTrue(productos.stream().allMatch(p -> p.getId() > 0), "Cada producto trae su id");
    }

    @Test
    void listarCreaLaSubclaseCorrecta() throws Exception {
        Producto arroz = buscar("AB-001");
        assertInstanceOf(ProductoNoPerecible.class, arroz);
        assertEquals("Tucapel", ((ProductoNoPerecible) arroz).getMarca());
        assertEquals(Categoria.ABARROTES, arroz.getCategoria());
        assertEquals(1490, arroz.getPrecio());
        assertEquals(30, arroz.getStock());

        Producto yogur = buscar("LA-002");
        assertInstanceOf(ProductoPerecible.class, yogur);
        assertEquals(LocalDate.now().plusDays(2), ((ProductoPerecible) yogur).getFechaVencimiento());
        assertEquals(Categoria.LACTEOS, yogur.getCategoria());
    }

    @Test
    void insertarPerecibleYNoPerecible() throws Exception {
        ProductoPerecible pan = new ProductoPerecible("PA-002", "Hallulla (1 kg)", Categoria.PANADERIA, 2300, 15,
                LocalDate.of(2026, 12, 1));
        ProductoNoPerecible sal = new ProductoNoPerecible("AB-010", "Sal fina 1 kg", Categoria.ABARROTES, 590, 50, "Lobos");
        dao.insertar(pan);
        dao.insertar(sal);
        assertTrue(pan.getId() > 0 && sal.getId() > 0, "insertar asigna el id generado");
        assertNotEquals(pan.getId(), sal.getId());

        ProductoPerecible panGuardado = (ProductoPerecible) buscar("PA-002");
        assertEquals(pan.getId(), panGuardado.getId());
        assertEquals(LocalDate.of(2026, 12, 1), panGuardado.getFechaVencimiento());
        assertEquals("Lobos", ((ProductoNoPerecible) buscar("AB-010")).getMarca());
        assertEquals(14, dao.listarTodos().size());
    }

    @Test
    void codigoRepetido() {
        Producto repetido = new ProductoNoPerecible("AB-001", "Otro arroz", Categoria.ABARROTES, 1000, 1, "X");
        PersistenciaException e = assertThrows(PersistenciaException.class, () -> dao.insertar(repetido));
        assertEquals("Ya existe un producto con el código AB-001.", e.getMessage());
        assertNotNull(e.getCause(), "Conserva la SQLException como causa");
    }

    @Test
    void actualizar() throws Exception {
        ProductoNoPerecible aceite = (ProductoNoPerecible) buscar("AB-003").copiar();
        aceite.setPrecio(2990);
        aceite.setStock(12);
        aceite.setMarca("Chef");
        assertTrue(dao.actualizar(aceite));

        ProductoNoPerecible guardado = (ProductoNoPerecible) buscar("AB-003");
        assertEquals(2990, guardado.getPrecio());
        assertEquals(12, guardado.getStock());
        assertEquals("Chef", guardado.getMarca());
    }

    @Test
    void actualizarFechaDeUnPerecible() throws Exception {
        ProductoPerecible leche = (ProductoPerecible) buscar("LA-001").copiar();
        leche.setFechaVencimiento(LocalDate.of(2027, 1, 15));
        assertTrue(dao.actualizar(leche));
        assertEquals(LocalDate.of(2027, 1, 15), ((ProductoPerecible) buscar("LA-001")).getFechaVencimiento());
    }

    @Test
    void actualizarInexistenteOConCodigoAjeno() throws Exception {
        Producto fantasma = new ProductoNoPerecible("ZZ-999", "Nada", Categoria.ABARROTES, 1, 1, "X");
        fantasma.setId(9999);
        assertFalse(dao.actualizar(fantasma));

        Producto azucar = buscar("AB-004").copiar();
        azucar.setCodigo("AB-001");
        PersistenciaException e = assertThrows(PersistenciaException.class, () -> dao.actualizar(azucar));
        assertEquals("Ya existe un producto con el código AB-001.", e.getMessage());
    }

    @Test
    void eliminarProductoSinVentas() throws Exception {
        Producto cloro = buscar("LI-002");
        assertTrue(dao.eliminar(cloro.getId()));
        assertFalse(dao.eliminar(cloro.getId()));
        assertNull(buscar("LI-002"));
    }

    @Test
    void noSeEliminaUnProductoConVentas() throws Exception {
        Producto arroz = buscar("AB-001");
        PersistenciaException e = assertThrows(PersistenciaException.class, () -> dao.eliminar(arroz.getId()));
        assertTrue(e.getMessage().contains("tiene ventas registradas"), e.getMessage());
        assertNotNull(buscar("AB-001"));
    }

    @Test
    void sinServidor() {
        Properties propiedades = new Properties();
        propiedades.setProperty("db.url", "jdbc:mysql://127.0.0.1:1/almacen_kiwi"); // MySQL detenido
        propiedades.setProperty("db.usuario", "root");
        ProductoDao sinConexion = new JdbcProductoDao(new ConexionBD(propiedades));
        PersistenciaException e = assertThrows(PersistenciaException.class, sinConexion::listarTodos);
        assertTrue(e.getMessage().startsWith("No hay conexión con la base de datos"), e.getMessage());
    }
}
