package cl.dsy1102.ejemplos.polimorfismo;

/**
 * Credito: recargo de 1,5 % por cada cuota despues de la primera y
 * 1 punto por cada $100 pagados.
 */
public class TarjetaCredito extends MedioPago implements Acumulable {

    public static final int MAXIMO_CUOTAS = 12;
    public static final double INTERES_POR_CUOTA = 0.015;

    private final int cuotas;

    public TarjetaCredito(String titular, int cuotas) {
        super(titular);
        if (cuotas < 1 || cuotas > MAXIMO_CUOTAS) {
            throw new IllegalArgumentException("Las cuotas deben estar entre 1 y " + MAXIMO_CUOTAS + ".");
        }
        this.cuotas = cuotas;
    }

    @Override
    public int calcularTotal(int monto) {
        return (int) Math.round(monto * (1 + INTERES_POR_CUOTA * (cuotas - 1)));
    }

    @Override
    public String getNombre() {
        return cuotas == 1 ? "Credito 1 cuota" : "Credito " + cuotas + " cuotas";
    }

    @Override
    public int calcularPuntos(int totalPagado) {
        return totalPagado / 100;
    }

    public int getCuotas() {
        return cuotas;
    }
}
