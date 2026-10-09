package cl.dsy1102.ejemplos.polimorfismo;

/**
 * Debito: se paga el monto exacto, sin recargo ni puntos.
 */
public class TarjetaDebito extends MedioPago {

    public TarjetaDebito(String titular) {
        super(titular);
    }

    @Override
    public int calcularTotal(int monto) {
        return monto;
    }

    @Override
    public String getNombre() {
        return "Debito";
    }
}
