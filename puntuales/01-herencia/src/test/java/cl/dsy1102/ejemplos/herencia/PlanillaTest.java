package cl.dsy1102.ejemplos.herencia;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de autoevaluacion. No compilan hasta que existan Vendedor y Jefatura:
 * ese es tu primer objetivo. Ejecuta con: mvn test
 */
class PlanillaTest {

    @Test
    void vendedorSumaComisionAlSueldoBase() {
        Vendedor vendedor = new Vendedor("22.222.222-2", "Bruno Diaz", 560_000, 4_000_000);
        assertEquals(120_000, vendedor.calcularComision());
        assertEquals(680_000, vendedor.calcularSueldo());
    }

    @Test
    void vendedorEsUnEmpleado() {
        Empleado empleado = new Vendedor("22.222.222-2", "Bruno Diaz", 560_000, 0);
        assertInstanceOf(Empleado.class, empleado);
        assertEquals(560_000, empleado.calcularSueldo());
    }

    @Test
    void vendedorReutilizaLasValidacionesDeEmpleado() {
        assertThrows(IllegalArgumentException.class, () -> new Vendedor("22.222.222-2", "Bruno", 100_000, 0));
        assertThrows(IllegalArgumentException.class, () -> new Vendedor("22.222.222-2", "Bruno", 560_000, -1));
    }

    @Test
    void jefaturaSumaAsignacionDeResponsabilidad() {
        Jefatura jefa = new Jefatura("33.333.333-3", "Carla Soto", 1_200_000, 6);
        assertEquals(240_000, jefa.calcularAsignacion());
        assertEquals(1_440_000, jefa.calcularSueldo());
    }

    @Test
    void jefaturaValidaPersonasACargo() {
        assertThrows(IllegalArgumentException.class, () -> new Jefatura("3-3", "Carla", 1_200_000, 0));
        assertThrows(IllegalArgumentException.class, () -> new Jefatura("3-3", "Carla", 1_200_000, 51));
    }

    @Test
    void detalleIncluyeLosDatosDeLaSuperclase() {
        Vendedor vendedor = new Vendedor("22.222.222-2", "Bruno Diaz", 560_000, 4_000_000);
        String detalle = vendedor.obtenerDetalle();
        assertTrue(detalle.startsWith("22.222.222-2 | Bruno Diaz | Base: $560.000"), detalle);
        assertTrue(detalle.endsWith("Comision: $120.000"), detalle);
    }
}
