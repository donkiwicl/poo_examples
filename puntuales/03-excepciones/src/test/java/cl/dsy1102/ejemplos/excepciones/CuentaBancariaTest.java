package cl.dsy1102.ejemplos.excepciones;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas de autoevaluacion. No compilan hasta que existan
 * SaldoInsuficienteException y LimiteDiarioExcedidoException.
 */
class CuentaBancariaTest {

    @Test
    void giroValidoDescuentaSaldo() throws OperacionRechazadaException {
        CuentaBancaria cuenta = new CuentaBancaria("1", 100_000);
        cuenta.girar(30_000);
        assertEquals(70_000, cuenta.getSaldo());
        assertEquals(30_000, cuenta.getGiradoHoy());
    }

    @Test
    void giroSinSaldoLanzaExcepcionConElFaltante() {
        CuentaBancaria cuenta = new CuentaBancaria("1", 100_000);
        SaldoInsuficienteException e = assertThrows(SaldoInsuficienteException.class, () -> cuenta.girar(130_000));
        assertEquals(30_000, e.getFaltante());
        assertEquals("Saldo insuficiente: faltan $30.000", e.getMessage());
        assertEquals(100_000, cuenta.getSaldo(), "Un giro rechazado no debe cambiar el saldo");
    }

    @Test
    void giroSobreElLimiteDiario() throws OperacionRechazadaException {
        CuentaBancaria cuenta = new CuentaBancaria("1", 500_000);
        cuenta.girar(150_000);
        LimiteDiarioExcedidoException e = assertThrows(LimiteDiarioExcedidoException.class, () -> cuenta.girar(60_000));
        assertEquals(50_000, e.getDisponibleHoy());
        assertEquals(350_000, cuenta.getSaldo());
    }

    @Test
    void montoInvalidoEsErrorDeProgramacion() {
        CuentaBancaria cuenta = new CuentaBancaria("1", 100_000);
        assertThrows(IllegalArgumentException.class, () -> cuenta.girar(0));
    }

    @Test
    void transferenciaFallidaNoDepositaEnElDestino() {
        CuentaBancaria origen = new CuentaBancaria("1", 10_000);
        CuentaBancaria destino = new CuentaBancaria("2", 0);
        assertThrows(SaldoInsuficienteException.class, () -> origen.transferir(destino, 20_000));
        assertEquals(0, destino.getSaldo());
    }

    @Test
    void transferenciaExitosa() throws OperacionRechazadaException {
        CuentaBancaria origen = new CuentaBancaria("1", 10_000);
        CuentaBancaria destino = new CuentaBancaria("2", 0);
        origen.transferir(destino, 4_000);
        assertEquals(6_000, origen.getSaldo());
        assertEquals(4_000, destino.getSaldo());
        assertThrows(IllegalArgumentException.class, () -> origen.transferir(origen, 1_000));
    }
}
