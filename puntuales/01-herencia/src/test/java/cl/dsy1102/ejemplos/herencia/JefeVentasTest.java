package cl.dsy1102.ejemplos.herencia;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/** Pruebas del desafio R4. */
class JefeVentasTest {

    @Test
    void cobraComisionYBonoSiCumpleLaMeta() {
        JefeVentas jefe = new JefeVentas("4-4", "Diego Pinto", 900_000, 12_000_000, 10_000_000);
        assertEquals(900_000 + 360_000 + JefeVentas.BONO_META, jefe.calcularSueldo());
    }

    @Test
    void sinMetaCobraComoVendedor() {
        JefeVentas jefe = new JefeVentas("4-4", "Diego Pinto", 900_000, 5_000_000, 10_000_000);
        assertEquals(900_000 + 150_000, jefe.calcularSueldo());
        assertInstanceOf(Vendedor.class, jefe);
    }
}
