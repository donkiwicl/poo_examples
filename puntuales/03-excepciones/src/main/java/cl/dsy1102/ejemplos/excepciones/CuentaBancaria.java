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
     * TODO R2: implementa las reglas en este orden y agrega "throws" a la firma.
     *  1. monto <= 0                              -> IllegalArgumentException
     *  2. monto > saldo                           -> SaldoInsuficienteException
     *  3. giradoHoy + monto > LIMITE_GIRO_DIARIO  -> LimiteDiarioExcedidoException
     *  Si todo esta bien, descuenta el saldo y suma a giradoHoy.
     */
    public void girar(int monto) {
    }

    /**
     * Transfiere a otra cuenta. Si el giro falla, el destino no recibe nada.
     *
     * TODO R3: destino null o igual a esta cuenta -> IllegalArgumentException.
     *  Luego gira de esta cuenta y deposita en el destino.
     */
    public void transferir(CuentaBancaria destino, int monto) {
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
