package cl.dsy1102.ejemplos.lavanderia;

import cl.dsy1102.ejemplos.lavanderia.dao.ClienteDao;
import cl.dsy1102.ejemplos.lavanderia.dao.ClienteDaoJdbc;
import cl.dsy1102.ejemplos.lavanderia.dao.DaoException;
import cl.dsy1102.ejemplos.lavanderia.database.ConexionBD;
import cl.dsy1102.ejemplos.lavanderia.model.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de autoevaluación de la capa DAO, con H2 en memoria.
 * No compilan hasta que crees ConexionBD, ClienteDao, ClienteDaoJdbc y DaoException (R2 a R4).
 * Ejecuta con: mvn test
 */
class ClienteDaoJdbcTest {

    // Se declara con el tipo de la interfaz, como lo hará el controlador.
    private ClienteDao dao;

    @BeforeEach
    void crearBase() throws Exception {
        dao = new ClienteDaoJdbc(BaseDePrueba.crear());
    }

    private static List<String> nombres(List<Cliente> clientes) {
        return clientes.stream().map(Cliente::getNombre).toList();
    }

    @Test
    void listarTodosOrdenadosPorNombre() throws Exception {
        List<Cliente> clientes = dao.listarTodos();
        assertEquals(List.of("Andrea Valdés", "Bastián Godoy", "Carolina Leiva", "Daniel Fuentes", "Elena O'Ryan"),
                nombres(clientes));
        Cliente andrea = clientes.get(0);
        assertTrue(andrea.getId() > 0);
        assertEquals("912345678", andrea.getTelefono());
        assertEquals("andrea.valdes@correo.cl", andrea.getCorreo());
        assertEquals("Ñuñoa", andrea.getComuna());
    }

    @Test
    void buscarPorNombreConParametros() throws Exception {
        assertEquals(List.of("Elena O'Ryan"), nombres(dao.buscarPorNombre("o'ryan")));
        assertEquals(List.of("Carolina Leiva"), nombres(dao.buscarPorNombre("LEIVA")));
        assertTrue(dao.buscarPorNombre("' OR '1'='1").isEmpty());
    }

    @Test
    void insertarAsignaId() throws Exception {
        Cliente nuevo = new Cliente(0, "Felipe Rivas", "967890123", "frivas@correo.cl", "Macul");
        dao.insertar(nuevo);
        assertTrue(nuevo.getId() > 0, "insertar debe asignar el id generado");
        assertEquals(6, dao.listarTodos().size());
    }

    @Test
    void correoRepetidoLanzaDaoException() throws Exception {
        Cliente repetido = new Cliente(0, "Otra Persona", "900000000", "bgodoy@correo.cl", "Macul");
        DaoException e = assertThrows(DaoException.class, () -> dao.insertar(repetido));
        assertEquals("Ya existe un cliente con el correo bgodoy@correo.cl.", e.getMessage());
        assertNotNull(e.getCause(), "Conserva la SQLException como causa");
    }

    @Test
    void actualizar() throws Exception {
        Cliente daniel = dao.buscarPorNombre("Daniel").get(0);
        daniel.setComuna("Peñalolén");
        daniel.setTelefono("911111111");
        assertTrue(dao.actualizar(daniel));
        Cliente guardado = dao.buscarPorNombre("Daniel").get(0);
        assertEquals("Peñalolén", guardado.getComuna());
        assertEquals("911111111", guardado.getTelefono());

        assertFalse(dao.actualizar(new Cliente(999, "Nadie", "900000000", "nadie@correo.cl", "Macul")));
    }

    @Test
    void actualizarConCorreoDeOtroCliente() throws Exception {
        Cliente daniel = dao.buscarPorNombre("Daniel").get(0);
        daniel.setCorreo("cleiva@correo.cl");
        DaoException e = assertThrows(DaoException.class, () -> dao.actualizar(daniel));
        assertEquals("Ya existe un cliente con el correo cleiva@correo.cl.", e.getMessage());
    }

    @Test
    void eliminar() throws Exception {
        int id = dao.buscarPorNombre("Bastián").get(0).getId();
        assertTrue(dao.eliminar(id));
        assertFalse(dao.eliminar(id));
        assertEquals(4, dao.listarTodos().size());
    }

    @Test
    void sinServidorLanzaDaoExceptionConMensajeClaro() {
        Properties propiedades = new Properties();
        // MySQL en un puerto donde no hay servidor: es lo que pasa si el servicio está detenido.
        propiedades.setProperty("db.url", "jdbc:mysql://127.0.0.1:1/lavanderia_burbuja");
        propiedades.setProperty("db.usuario", "root");
        ClienteDao sinConexion = new ClienteDaoJdbc(new ConexionBD(propiedades));
        DaoException e = assertThrows(DaoException.class, sinConexion::listarTodos);
        assertTrue(e.getMessage().startsWith("No hay conexión con la base de datos"), e.getMessage());
    }
}
