package cl.dsy1102.ejemplos.excepciones;

import java.util.Locale;

/**
 * Cuenta a la vista con limite de giro diario.
 */
public class CuentaBancaria {

    public static final int LIMITE_GIRO_DIARIO = 200_000;

    private static final Locale CHILE = Locale.of("es", "CL");

    private final String numero;
    private int saldo;
    private int giradoHoy;

    public CuentaBancaria(String numero, int saldoInicial) {
        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("El numero de cuenta es obligatorio");
        }
        if (saldoInicial < 0) {
            throw new IllegalArgumentException("El saldo inicial no puede ser negativo");
        }
        this.numero = numero;
        this.saldo = saldoInicial;
    }

    /**
     * Ejemplo resuelto de excepcion NO comprobada: un monto <= 0 es un error
     * de quien llama, asi que se lanza IllegalArgumentException.
     */
    public void depositar(int monto) {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que 0");
        }
        saldo += monto;
    }

    /**
     * Gira dinero de la cuenta.
     *
     * @throws IllegalArgumentException       si el monto no es mayor que 0
     * @throws SaldoInsuficienteException     si el saldo no alcanza
     * @throws LimiteDiarioExcedidoException  si supera el limite de giro del dia
     */
    public void girar(int monto) throws OperacionRechazadaException {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que 0");
        }
        if (monto > saldo) {
            throw new SaldoInsuficienteException(monto - saldo);
        }
        if (giradoHoy + monto > LIMITE_GIRO_DIARIO) {
            throw new LimiteDiarioExcedidoException(LIMITE_GIRO_DIARIO - giradoHoy);
        }
        // Solo se modifica el estado cuando todas las reglas se cumplen.
        saldo -= monto;
        giradoHoy += monto;
    }

    /**
     * Transfiere a otra cuenta. Si el giro falla, el destino no recibe nada:
     * la excepcion interrumpe el metodo antes de depositar.
     */
    public void transferir(CuentaBancaria destino, int monto) throws OperacionRechazadaException {
        if (destino == null || destino == this) {
            throw new IllegalArgumentException("La cuenta de destino no es valida");
        }
        girar(monto);
        destino.depositar(monto);
    }

    public static String pesos(int monto) {
        return String.format(CHILE, "$%,d", monto);
    }

    public String getNumero() {
        return numero;
    }

    public int getSaldo() {
        return saldo;
    }

    public int getGiradoHoy() {
        return giradoHoy;
    }
}
