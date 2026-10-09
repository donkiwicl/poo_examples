package cl.dsy1102.ejemplos.cooperativa.model;

import java.time.LocalDateTime;

/**
 * Un cargo (sale dinero) o un abono (entra dinero) en una cuenta.
 */
public record Movimiento(LocalDateTime fechaHora, String tipo, long monto, String glosa) {

    public static final String CARGO = "CARGO";
    public static final String ABONO = "ABONO";

    @Override
    public String toString() {
        return String.format("%-5s %s$%,10d  %s", tipo, CARGO.equals(tipo) ? "-" : "+", monto, glosa);
    }
}
