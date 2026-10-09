package cl.dsy1102.ejemplos.excepciones;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProcesadorMovimientosTest {

    private final CuentaBancaria cuenta = new CuentaBancaria("1234-5", 500_000);
    private final ProcesadorMovimientos procesador = new ProcesadorMovimientos(cuenta);

    @Test
    void comandosValidos() {
        assertEquals("OK | DEPOSITAR 50000 | Saldo: $550.000", procesador.procesar("DEPOSITAR 50000"));
        assertEquals("OK | SALDO | Saldo: $550.000", procesador.procesar("SALDO"));
    }

    @Test
    void reglaDeNegocioRechazada() {
        assertEquals("RECHAZADO | GIRAR 900000 | Saldo insuficiente: faltan $400.000", procesador.procesar("GIRAR 900000"));
    }

    @Test
    void erroresDeFormato() {
        assertEquals("ERROR | GIRAR abc | 'abc' no es un monto valido", procesador.procesar("GIRAR abc"));
        assertEquals("ERROR | GIRAR | Falta el monto", procesador.procesar("GIRAR"));
        assertEquals("ERROR | VOLAR 10 | Comando desconocido: VOLAR", procesador.procesar("VOLAR 10"));
        assertEquals("ERROR | DEPOSITAR -500 | El monto debe ser mayor que 0", procesador.procesar("DEPOSITAR -500"));
    }

    @Test
    void cuentaTodasLasLineasAunqueFallen() {
        procesador.procesar("SALDO");
        procesador.procesar("GIRAR abc");
        procesador.procesar("VOLAR 1");
        assertEquals(3, procesador.getLineasProcesadas());
    }

    @Test
    void procesaElArchivoDeEjemplo() throws IOException {
        var resultados = procesador.procesarArchivo(Path.of("movimientos.txt"));
        assertEquals(10, resultados.size());
        assertEquals(370_000, cuenta.getSaldo());
    }

    @Test
    void archivoInexistentePropagaIOException() {
        assertThrows(IOException.class, () -> procesador.procesarArchivo(Path.of("no-existe.txt")));
    }
}
