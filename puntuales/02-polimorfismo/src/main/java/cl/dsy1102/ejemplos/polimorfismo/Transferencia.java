package cl.dsy1102.ejemplos.polimorfismo;

/**
 * Desafio R4. Un medio de pago nuevo que se integra a la caja sin modificarla:
 * comision fija de $300 y 2 puntos por cada $100.
 */
public class Transferencia extends MedioPago implements Acumulable {

    public static final int COMISION = 300;

    public Transferencia(String titular) {
        super(titular);
    }

    @Override
    public int calcularTotal(int monto) {
        return monto + COMISION;
    }

    @Override
    public String getNombre() {
        return "Transferencia";
    }

    @Override
    public int calcularPuntos(int totalPagado) {
        return totalPagado / 100 * 2;
    }
}
