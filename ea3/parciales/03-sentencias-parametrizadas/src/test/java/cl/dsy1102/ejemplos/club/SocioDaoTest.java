package cl.dsy1102.ejemplos.club;

import cl.dsy1102.ejemplos.club.dao.SocioDao;
import cl.dsy1102.ejemplos.club.dao.SocioDuplicadoException;
import cl.dsy1102.ejemplos.club.model.Categoria;
import cl.dsy1102.ejemplos.club.model.Socio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de autoevaluación con H2 en memoria. Ejecuta con: mvn test
 */
class SocioDaoTest {

    private SocioDao dao;

    @BeforeEach
    void crearBase() throws Exception {
        dao = new SocioDao(BaseDePrueba.crear());
    }

    private Socio buscarPorRut(String rut) throws SQLException {
        return dao.listarTodos().stream().filter(s -> s.getRut().equals(rut)).findFirst().orElse(null);
    }

    // ---------- Demostración: la versión insegura (ya pasan) ----------

    @Test
    void demostracionInyeccionEnLaVersionInsegura() throws Exception {
        assertEquals(6, dao.buscarPorNombreInseguro("' OR '1'='1").size(),
                "El texto cambia la lógica del WHERE y retorna todos los socios");
    }

    @Test
    void demostracionComillaRompeLaVersionInsegura() {
        assertThrows(SQLException.class, () -> dao.buscarPorNombreInseguro("O'Higgins"));
    }

    // ---------- R1 ----------

    @Test
    void buscarPorNombreNoSeDejaInyectar() throws Exception {
        assertTrue(dao.buscarPorNombre("' OR '1'='1").isEmpty());
    }

    @Test
    void buscarPorNombreConComilla() throws Exception {
        List<Socio> encontrados = dao.buscarPorNombre("O'Higgins");
        assertEquals(1, encontrados.size());
        assertEquals("Bernardo O'Higgins Riquelme", encontrados.get(0).getNombre());
    }

    @Test
    void buscarPorNombreSinDistinguirMayusculas() throws Exception {
        assertEquals(List.of("Florencia Tapia", "Matías Tapia"),
                dao.buscarPorNombre("tapia").stream().map(Socio::getNombre).toList());
    }

    // ---------- R2 ----------

    @Test
    void insertarAsignaElIdGenerado() throws Exception {
        Socio socio = new Socio("20.111.222-3", "Javiera Lagos", Categoria.ADULTO, 18000, LocalDate.of(2026, 10, 9));
        dao.insertar(socio);
        assertTrue(socio.getId() > 6, "El id debe venir de getGeneratedKeys()");
        Socio guardado = buscarPorRut("20.111.222-3");
        assertNotNull(guardado);
        assertEquals(socio.getId(), guardado.getId());
        assertEquals(LocalDate.of(2026, 10, 9), guardado.getFechaIngreso());
        assertTrue(guardado.isActivo());
    }

    @Test
    void insertarRutRepetidoLanzaSocioDuplicado() throws Exception {
        Socio repetido = new Socio("15.678.901-2", "Otra Persona", Categoria.ADULTO, 18000, LocalDate.now());
        SocioDuplicadoException e = assertThrows(SocioDuplicadoException.class, () -> dao.insertar(repetido));
        assertTrue(e.getMessage().contains("15.678.901-2"), e.getMessage());
        assertNotNull(e.getCause(), "Conserva la SQLException original como causa");
        assertEquals(6, dao.listarTodos().size());
    }

    @Test
    void insertarGuardaElTextoTalCual() throws Exception {
        String nombre = "Robert'); DROP TABLE socio;--";
        dao.insertar(new Socio("1-9", nombre, Categoria.ADULTO, 18000, LocalDate.now()));
        assertEquals(7, dao.listarTodos().size(), "La tabla sigue existiendo");
        assertEquals(nombre, buscarPorRut("1-9").getNombre());
    }

    // ---------- R3 ----------

    @Test
    void actualizarExistente() throws Exception {
        Socio diego = buscarPorRut("16.789.012-3");
        diego.setNombre("Diego Araya Silva");
        diego.setCategoria(Categoria.SENIOR);
        diego.setCuotaMensual(12000);
        diego.setActivo(false);
        assertTrue(dao.actualizar(diego));

        Socio guardado = buscarPorRut("16.789.012-3");
        assertEquals("Diego Araya Silva", guardado.getNombre());
        assertEquals(Categoria.SENIOR, guardado.getCategoria());
        assertEquals(12000, guardado.getCuotaMensual());
        assertFalse(guardado.isActivo());
    }

    @Test
    void actualizarIdInexistenteRetornaFalse() throws Exception {
        Socio fantasma = new Socio(999, "1-1", "Nadie", Categoria.ADULTO, 1000, LocalDate.now(), true);
        assertFalse(dao.actualizar(fantasma));
    }

    // ---------- R4 ----------

    @Test
    void eliminarRetornaSiExistia() throws Exception {
        int id = buscarPorRut("9.876.543-2").getId();
        assertTrue(dao.eliminar(id));
        assertFalse(dao.eliminar(id));
        assertEquals(5, dao.listarTodos().size());
    }

    // ---------- R5 ----------

    @Test
    void reajustarCuotasDeUnaCategoria() throws Exception {
        assertEquals(2, dao.reajustarCuotas(Categoria.ADULTO, 10));
        assertEquals(19800, buscarPorRut("15.678.901-2").getCuotaMensual());
        assertEquals(12000, buscarPorRut("12.345.678-5").getCuotaMensual(), "Otras categorías no cambian");
    }

    @Test
    void reajustarSoloSociosActivos() throws Exception {
        assertEquals(1, dao.reajustarCuotas(Categoria.INFANTIL, 5.5));
        assertEquals(9495, buscarPorRut("22.123.456-7").getCuotaMensual());
        assertEquals(9000, buscarPorRut("22.234.567-8").getCuotaMensual(), "Matías está inactivo");
    }

    // ---------- R6 ----------

    @Test
    void filtrarPorCategoriaYFecha() throws Exception {
        assertEquals(List.of("Camila Vergara", "Diego Araya"),
                dao.filtrar(Categoria.ADULTO, LocalDate.of(2021, 7, 15)).stream().map(Socio::getNombre).toList());
        assertEquals(List.of("Diego Araya"),
                dao.filtrar(Categoria.ADULTO, LocalDate.of(2021, 7, 16)).stream().map(Socio::getNombre).toList());
    }
}
