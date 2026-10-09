package cl.dsy1102.ejemplos.cooperativa;

import cl.dsy1102.ejemplos.cooperativa.dao.CuentaDao;
import cl.dsy1102.ejemplos.cooperativa.dao.OperacionRechazadaException;
import cl.dsy1102.ejemplos.cooperativa.model.Movimiento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de autoevaluación con H2 en memoria. Ejecuta con: mvn test
 *
 * Saldos iniciales: 1001 $2.500.000 · 2001 $150.000 · 2002 $80.000 · 2003 $0
 */
class CuentaDaoTest {

    private CuentaDao dao;

    @BeforeEach
    void crearBase() throws Exception {
        dao = new CuentaDao(BaseDePrueba.crear());
    }

    private void assertSinCambios() throws Exception {
        assertEquals(2_500_000, dao.saldo("1001"));
        assertEquals(150_000, dao.saldo("2001"));
        assertEquals(80_000, dao.saldo("2002"));
        assertEquals(0, dao.saldo("2003"));
        for (String numero : new String[]{"1001", "2001", "2002", "2003"}) {
            assertTrue(dao.movimientos(numero).isEmpty(), "No deben quedar movimientos en " + numero);
        }
    }

    // ---------- Demostración: la versión sin transacción (ya pasa) ----------

    @Test
    void demostracionSinTransaccionSePierdeElDinero() throws Exception {
        assertThrows(OperacionRechazadaException.class,
                () -> dao.transferirSinTransaccion("2001", "9999", 50_000, "Arriendo"));
        assertEquals(100_000, dao.saldo("2001"), "El cargo quedó confirmado aunque el abono falló");
    }

    // ---------- R1: validaciones ----------

    @Test
    void montoDebeSerPositivo() {
        assertThrows(IllegalArgumentException.class, () -> dao.transferir("2001", "2002", 0, "x"));
        assertThrows(IllegalArgumentException.class, () -> dao.transferir("2001", "2002", -5, "x"));
    }

    @Test
    void origenYDestinoDebenSerDistintos() {
        assertThrows(IllegalArgumentException.class, () -> dao.transferir("2001", "2001", 1_000, "x"));
    }

    // ---------- R2: transferencia exitosa ----------

    @Test
    void transferirMueveElDineroYRegistraMovimientos() throws Exception {
        dao.transferir("2001", "2002", 50_000, "Arriendo");
        assertEquals(100_000, dao.saldo("2001"));
        assertEquals(130_000, dao.saldo("2002"));

        List<Movimiento> origen = dao.movimientos("2001");
        List<Movimiento> destino = dao.movimientos("2002");
        assertEquals(1, origen.size());
        assertEquals(1, destino.size());
        assertEquals(Movimiento.CARGO, origen.get(0).tipo());
        assertEquals(50_000, origen.get(0).monto());
        assertEquals("Arriendo", origen.get(0).glosa());
        assertEquals(Movimiento.ABONO, destino.get(0).tipo());
        assertEquals(origen.get(0).fechaHora(), destino.get(0).fechaHora(), "Misma fecha y hora en ambos");
    }

    @Test
    void sePuedeTransferirTodoElSaldo() throws Exception {
        dao.transferir("2002", "2003", 80_000, "Todo");
        assertEquals(0, dao.saldo("2002"));
        assertEquals(80_000, dao.saldo("2003"));
    }

    // ---------- R3: rechazos sin cambios ----------

    @Test
    void saldoInsuficienteNoCambiaNada() throws Exception {
        OperacionRechazadaException e = assertThrows(OperacionRechazadaException.class,
                () -> dao.transferir("2003", "2001", 10_000, "Préstamo"));
        assertEquals("Saldo insuficiente en la cuenta 2003.", e.getMessage());
        assertSinCambios();
    }

    @Test
    void origenInexistente() throws Exception {
        OperacionRechazadaException e = assertThrows(OperacionRechazadaException.class,
                () -> dao.transferir("9999", "2001", 10_000, "x"));
        assertEquals("La cuenta 9999 no existe.", e.getMessage());
        assertSinCambios();
    }

    @Test
    void destinoInexistenteDeshaceElCargo() throws Exception {
        OperacionRechazadaException e = assertThrows(OperacionRechazadaException.class,
                () -> dao.transferir("2001", "9999", 50_000, "Arriendo"));
        assertEquals("La cuenta 9999 no existe.", e.getMessage());
        assertSinCambios(); // rollback: el cargo al 2001 se deshizo
    }

    // ---------- R4: remuneraciones en lote ----------

    private static Map<String, Long> pagosOctubre() {
        Map<String, Long> pagos = new LinkedHashMap<>();
        pagos.put("2001", 650_000L);
        pagos.put("2002", 580_000L);
        pagos.put("2003", 520_000L);
        return pagos;
    }

    @Test
    void pagarRemuneraciones() throws Exception {
        assertEquals(1_750_000, dao.pagarRemuneraciones("1001", pagosOctubre(), "Sueldo octubre"));
        assertEquals(750_000, dao.saldo("1001"));
        assertEquals(800_000, dao.saldo("2001"));
        assertEquals(660_000, dao.saldo("2002"));
        assertEquals(520_000, dao.saldo("2003"));

        List<Movimiento> empresa = dao.movimientos("1001");
        assertEquals(1, empresa.size(), "Un solo cargo por el total");
        assertEquals("Remuneraciones (3 pagos)", empresa.get(0).glosa());
        assertEquals(1_750_000, empresa.get(0).monto());
        assertEquals("Sueldo octubre", dao.movimientos("2003").get(0).glosa());
    }

    @Test
    void pagoConCuentaInexistenteNoPagaANadie() throws Exception {
        Map<String, Long> pagos = pagosOctubre();
        pagos.put("2999", 100_000L);
        OperacionRechazadaException e = assertThrows(OperacionRechazadaException.class,
                () -> dao.pagarRemuneraciones("1001", pagos, "Sueldo octubre"));
        assertEquals("La cuenta 2999 no existe.", e.getMessage());
        assertSinCambios();
    }

    @Test
    void pagoMayorQueElSaldoDeLaEmpresa() throws Exception {
        Map<String, Long> pagos = pagosOctubre();
        pagos.put("2001", 2_000_000L);
        OperacionRechazadaException e = assertThrows(OperacionRechazadaException.class,
                () -> dao.pagarRemuneraciones("1001", pagos, "Sueldo octubre"));
        assertEquals("Saldo insuficiente en la cuenta 1001.", e.getMessage());
        assertSinCambios();
    }

    @Test
    void pagosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> dao.pagarRemuneraciones("1001", Map.of(), "x"));
        assertThrows(IllegalArgumentException.class,
                () -> dao.pagarRemuneraciones("1001", Map.of("2001", 0L), "x"));
    }
}
