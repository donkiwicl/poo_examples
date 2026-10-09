package cl.dsy1102.ejemplos.polimorfismo;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas de autoevaluacion. No compilan hasta que existan TarjetaDebito y
 * TarjetaCredito. Ejecuta con: mvn test
 */
class CajaTest {

    @Test
    void efectivoRedondeaALaDecena() {
        Efectivo efectivo = new Efectivo("Ana");
        assertEquals(12_340, efectivo.calcularTotal(12_345));
        assertEquals(5_000, efectivo.calcularTotal(4_996));
        assertEquals(1_000, efectivo.calcularTotal(1_000));
    }

    @Test
    void debitoNoTieneRecargo() {
        assertEquals(8_990, new TarjetaDebito("Bruno").calcularTotal(8_990));
    }

    @Test
    void creditoCobraInteresDesdeLaSegundaCuota() {
        assertEquals(60_000, new TarjetaCredito("Carla", 1).calcularTotal(60_000));
        assertEquals(61_800, new TarjetaCredito("Carla", 3).calcularTotal(60_000));
        assertThrows(IllegalArgumentException.class, () -> new TarjetaCredito("Carla", 13));
    }

    @Test
    void creditoAcumulaPuntos() {
        MedioPago credito = new TarjetaCredito("Carla", 3);
        assertEquals(618, ((Acumulable) credito).calcularPuntos(61_800));
    }

    @Test
    void cajaCobraConCualquierMedioDePago() {
        Caja caja = new Caja();
        List<MedioPago> medios = List.of(new Efectivo("Ana"), new TarjetaDebito("Bruno"), new TarjetaCredito("Carla", 3));
        for (MedioPago medio : medios) {
            caja.cobrar(60_000, medio);
        }
        assertEquals(60_000 + 60_000 + 61_800, caja.getTotalRecaudado());
        assertEquals(618, caja.getPuntosOtorgados());
        assertEquals(3, caja.getBoletas().size());
    }

    @Test
    void lineaDeBoleta() {
        Caja caja = new Caja();
        assertEquals("Credito 3 cuotas | Carla Soto | Compra: $60.000 | Total: $61.800 | Puntos: 618",
                caja.cobrar(60_000, new TarjetaCredito("Carla Soto", 3)));
        assertEquals("Debito | Bruno Diaz | Compra: $8.990 | Total: $8.990",
                caja.cobrar(8_990, new TarjetaDebito("Bruno Diaz")));
    }

    @Test
    void cajaRechazaMontosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new Caja().cobrar(0, new Efectivo("Ana")));
    }
}
