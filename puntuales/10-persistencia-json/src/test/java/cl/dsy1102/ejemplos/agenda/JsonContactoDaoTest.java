package cl.dsy1102.ejemplos.agenda;

import cl.dsy1102.ejemplos.agenda.dao.ContactoDao;
import cl.dsy1102.ejemplos.agenda.dao.JsonContactoDao;
import cl.dsy1102.ejemplos.agenda.dao.PersistenciaException;
import cl.dsy1102.ejemplos.agenda.model.Contacto;
import cl.dsy1102.ejemplos.agenda.model.ContactoLaboral;
import cl.dsy1102.ejemplos.agenda.model.ContactoPersonal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de autoevaluacion. Compilan desde el inicio, pero fallan hasta
 * completar R1 a R4. Ejecuta con: mvn test
 *
 * @TempDir entrega una carpeta temporal nueva para cada prueba: nunca se toca data/.
 */
class JsonContactoDaoTest {

    @TempDir
    Path carpeta;

    private List<Contacto> ejemplo() {
        return List.of(
                new ContactoPersonal("Ana Rojas", "+56911112222", "ana@mail.cl", "Anita", true),
                new ContactoLaboral("Carla Soto", "225551234", "csoto@kiwi.cl", "Kiwi SpA", "Jefa de TI"));
    }

    @Test
    void archivoInexistenteRetornaListaVacia() throws PersistenciaException {
        assertTrue(new JsonContactoDao(carpeta.resolve("no-existe.json")).cargar().isEmpty());
    }

    @Test
    void guardarYCargarConservaSubtiposYDatos() throws PersistenciaException {
        Path archivo = carpeta.resolve("contactos.json");
        new JsonContactoDao(archivo).guardar(ejemplo());

        List<Contacto> cargados = new JsonContactoDao(archivo).cargar();
        assertEquals(2, cargados.size());
        ContactoPersonal ana = assertInstanceOf(ContactoPersonal.class, cargados.get(0));
        assertEquals("Anita", ana.getApodo());
        assertTrue(ana.isFavorito());
        ContactoLaboral carla = assertInstanceOf(ContactoLaboral.class, cargados.get(1));
        assertEquals("Kiwi SpA", carla.getEmpresa());
    }

    @Test
    void elJsonEsLegibleEIncluyeElTipo() throws Exception {
        Path archivo = carpeta.resolve("contactos.json");
        new JsonContactoDao(archivo).guardar(ejemplo());
        String json = Files.readString(archivo);
        assertTrue(json.contains("\"tipo\" : \"PERSONAL\""), json);
        assertTrue(json.contains("\"tipo\" : \"LABORAL\""), json);
        assertTrue(json.lines().count() > 5, "Se esperaba formato legible (pretty print)");
    }

    @Test
    void creaLaCarpetaSiNoExiste() throws PersistenciaException {
        Path archivo = carpeta.resolve("sub/carpeta/contactos.json");
        new JsonContactoDao(archivo).guardar(ejemplo());
        assertTrue(Files.exists(archivo));
    }

    @Test
    void archivoDanadoLanzaPersistenciaException() throws Exception {
        Path archivo = carpeta.resolve("danado.json");
        Files.writeString(archivo, "[ { \"tipo\" : ");
        ContactoDao dao = new JsonContactoDao(archivo);
        PersistenciaException e = assertThrows(PersistenciaException.class, dao::cargar);
        assertFalse(e.getMessage().isBlank());
    }

    @Test
    void datoInvalidoSegunElModeloLanzaPersistenciaException() throws Exception {
        Path archivo = carpeta.resolve("invalido.json");
        Files.writeString(archivo, """
                [ { "tipo" : "PERSONAL", "nombre" : "Ana", "telefono" : "+56911112222",
                    "email" : "sin-arroba", "apodo" : null, "favorito" : false } ]""");
        assertThrows(PersistenciaException.class, new JsonContactoDao(archivo)::cargar);
    }

    @Test
    void apodoVacioSeGuardaComoNull() throws PersistenciaException {
        Path archivo = carpeta.resolve("contactos.json");
        new JsonContactoDao(archivo).guardar(List.of(new ContactoPersonal("Bruno", "+56933334444", "b@mail.cl", "", false)));
        ContactoPersonal bruno = (ContactoPersonal) new JsonContactoDao(archivo).cargar().get(0);
        assertNull(bruno.getApodo());
    }
}
