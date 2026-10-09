package cl.dsy1102.ejemplos.excepciones;

/**
 * El giro supera el limite diario de la cuenta.
 */
public class LimiteDiarioExcedidoException extends OperacionRechazadaException {

    private final int disponibleHoy;

    public LimiteDiarioExcedidoException(int disponibleHoy) {
        super("Supera el limite diario de " + CuentaBancaria.pesos(CuentaBancaria.LIMITE_GIRO_DIARIO)
                + ": puedes girar hasta " + CuentaBancaria.pesos(disponibleHoy) + " hoy");
        this.disponibleHoy = disponibleHoy;
    }

    public int getDisponibleHoy() {
        return disponibleHoy;
    }
}
