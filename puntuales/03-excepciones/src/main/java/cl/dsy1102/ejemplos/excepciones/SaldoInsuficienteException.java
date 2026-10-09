package cl.dsy1102.ejemplos.excepciones;

/**
 * El saldo de la cuenta no alcanza para el giro. Guarda cuanto falta para
 * que quien la captura pueda usar el dato, no solo el mensaje.
 */
public class SaldoInsuficienteException extends OperacionRechazadaException {

    private final int faltante;

    public SaldoInsuficienteException(int faltante) {
        super("Saldo insuficiente: faltan " + CuentaBancaria.pesos(faltante));
        this.faltante = faltante;
    }

    public int getFaltante() {
        return faltante;
    }
}
