package cl.dsy1102.ejemplos.polimorfismo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Pruebas del desafio R4. */
class TransferenciaTest {

    @Test
    void laCajaAceptaUnMedioNuevoSinModificarse() {
        Caja caja = new Caja();
        assertEquals("Transferencia | Elena Mora | Compra: $25.000 | Total: $25.300 | Puntos: 506",
                caja.cobrar(25_000, new Transferencia("Elena Mora")));
        assertEquals(506, caja.getPuntosOtorgados());
    }
}
