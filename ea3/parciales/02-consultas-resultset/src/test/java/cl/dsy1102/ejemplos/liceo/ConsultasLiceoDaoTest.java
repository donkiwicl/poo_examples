package cl.dsy1102.ejemplos.liceo;

import cl.dsy1102.ejemplos.liceo.dao.ConsultasLiceoDao;
import cl.dsy1102.ejemplos.liceo.model.Alumno;
import cl.dsy1102.ejemplos.liceo.model.PromedioAlumno;
import cl.dsy1102.ejemplos.liceo.model.ResumenAsignatura;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de autoevaluación con H2 en memoria. Ejecuta con: mvn test
 */
class ConsultasLiceoDaoTest {

    private ConsultasLiceoDao dao;

    @BeforeEach
    void crearBase() throws Exception {
        dao = new ConsultasLiceoDao(BaseDePrueba.crear());
    }

    @Test
    void ejemploContarAlumnos() throws Exception {
        assertEquals(6, dao.contarAlumnos());
    }

    // ---------- R1 ----------

    @Test
    void listarAlumnosOrdenaPorCursoYNombre() throws Exception {
        List<String> nombres = dao.listarAlumnos().stream().map(Alumno::getNombre).toList();
        assertEquals(List.of("Benjamín Soto", "Martina Pérez", "Valentina Rojas",
                "Agustín Díaz", "Isidora Muñoz", "Tomás Fuentes"), nombres);
    }

    @Test
    void listarAlumnosMapeaTodasLasColumnas() throws Exception {
        Alumno valentina = dao.listarAlumnos().get(2);
        assertEquals(1, valentina.getId());
        assertEquals("21.345.678-9", valentina.getRut());
        assertEquals(LocalDate.of(2009, 4, 12), valentina.getFechaNacimiento());
        assertEquals("mrojas@correo.cl", valentina.getCorreoApoderado());
        assertEquals("3°A", valentina.getCurso(), "El curso debe ser el nombre del curso, no el del alumno");
    }

    @Test
    void correoNuloSeMantieneNulo() throws Exception {
        Alumno benjamin = dao.listarAlumnos().get(0);
        assertNull(benjamin.getCorreoApoderado());
        assertFalse(benjamin.tieneCorreo());
    }

    // ---------- R2 ----------

    @Test
    void buscarPorRutExistente() throws Exception {
        Optional<Alumno> alumno = dao.buscarPorRut("21.678.901-2");
        assertTrue(alumno.isPresent());
        assertEquals("Tomás Fuentes", alumno.get().getNombre());
        assertEquals("3°B", alumno.get().getCurso());
    }

    @Test
    void buscarPorRutInexistente() throws Exception {
        assertTrue(dao.buscarPorRut("11.111.111-1").isEmpty());
    }

    @Test
    void buscarPorRutNoSeDejaEnganar() throws Exception {
        assertTrue(dao.buscarPorRut("x' OR '1'='1").isEmpty(), "El RUT debe ir como parámetro (?)");
    }

    // ---------- R3 ----------

    @Test
    void listarPorCurso() throws Exception {
        List<String> nombres = dao.listarPorCurso("3°B").stream().map(Alumno::getNombre).toList();
        assertEquals(List.of("Agustín Díaz", "Isidora Muñoz", "Tomás Fuentes"), nombres);
        assertTrue(dao.listarPorCurso("4°C").isEmpty());
    }

    // ---------- R4 ----------

    @Test
    void promediosDelCursoOrdenadosDeMayorAMenor() throws Exception {
        List<PromedioAlumno> promedios = dao.promediosDelCurso("3°A");
        assertEquals(3, promedios.size());
        assertEquals(new PromedioAlumno("Valentina Rojas", "3°A", 6.3, 4), promedios.get(0));
        assertEquals(new PromedioAlumno("Martina Pérez", "3°A", 5.9, 4), promedios.get(1));
        assertEquals(new PromedioAlumno("Benjamín Soto", "3°A", 3.8, 4), promedios.get(2));
    }

    @Test
    void alumnoSinNotasTienePromedioNuloYVaAlFinal() throws Exception {
        List<PromedioAlumno> promedios = dao.promediosDelCurso("3°B");
        assertEquals(3, promedios.size(), "Agustín no tiene notas pero igual debe aparecer (LEFT JOIN)");
        PromedioAlumno agustin = promedios.get(2);
        assertEquals("Agustín Díaz", agustin.nombre());
        assertNull(agustin.promedio(), "Sin notas el promedio es null, no 0.0");
        assertEquals(0, agustin.cantidadNotas());
        assertEquals(6.8, promedios.get(0).promedio());
    }

    // ---------- R5 ----------

    @Test
    void alumnosEnRiesgo() throws Exception {
        List<PromedioAlumno> enRiesgo = dao.alumnosEnRiesgo(4.0);
        assertEquals(List.of("Tomás Fuentes", "Benjamín Soto"),
                enRiesgo.stream().map(PromedioAlumno::nombre).toList());
        assertEquals(3.7, enRiesgo.get(0).promedio());
        assertEquals("3°B", enRiesgo.get(0).curso());
    }

    @Test
    void alumnosEnRiesgoConOtroLimite() throws Exception {
        assertEquals(3, dao.alumnosEnRiesgo(6.0).size()); // Martina (5,875) también
        assertTrue(dao.alumnosEnRiesgo(1.0).isEmpty());
    }

    // ---------- R6 ----------

    @Test
    void resumenPorAsignatura() throws Exception {
        List<ResumenAsignatura> resumen = dao.resumenPorAsignatura();
        assertEquals(List.of(
                new ResumenAsignatura("Historia", 6, 5.1, 3.5, 6.9),
                new ResumenAsignatura("Lenguaje", 6, 5.2, 3.0, 6.6),
                new ResumenAsignatura("Matemática", 7, 5.4, 3.2, 7.0)), resumen);
    }
}
