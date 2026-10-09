package cl.dsy1102.ejemplos.excepciones;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Aplica a una cuenta los movimientos escritos en un archivo de texto.
 * Cada linea es un comando: DEPOSITAR monto, GIRAR monto o SALDO.
 */
public class ProcesadorMovimientos {

    private final CuentaBancaria cuenta;
    private int lineasProcesadas;

    public ProcesadorMovimientos(CuentaBancaria cuenta) {
        this.cuenta = cuenta;
    }

    /**
     * Procesa un comando y retorna el resultado. NUNCA lanza excepciones:
     * una linea mala no debe detener el resto del archivo.
     *
     * Formatos de respuesta:
     *   "OK | DEPOSITAR 50000 | Saldo: $550.000"
     *   "RECHAZADO | GIRAR 900000 | Saldo insuficiente: faltan $380.000"
     *   "ERROR | GIRAR abc | 'abc' no es un monto valido"
     *
     * TODO R4: separa el comando del monto, ejecuta la operacion y captura:
     *  - OperacionRechazadaException -> RECHAZADO
     *  - NumberFormatException       -> ERROR con "'<texto>' no es un monto valido"
     *  - IllegalArgumentException    -> ERROR con el mensaje de la excepcion
     *  Comando desconocido: "Comando desconocido: VOLAR". Sin monto: "Falta el monto".
     *  Usa finally para contar la linea en lineasProcesadas pase lo que pase.
     */
    public String procesar(String linea) {
        return "";
    }

    /**
     * Lee el archivo y procesa cada linea que no este vacia ni empiece con #.
     *
     * TODO R5: abre el archivo con try-with-resources (Files.newBufferedReader).
     *  No captures la IOException aqui: declarala con throws para que Main
     *  decida que mostrar al usuario.
     */
    public List<String> procesarArchivo(Path archivo) throws IOException {
        return new ArrayList<>();
    }

    public int getLineasProcesadas() {
        return lineasProcesadas;
    }
}
