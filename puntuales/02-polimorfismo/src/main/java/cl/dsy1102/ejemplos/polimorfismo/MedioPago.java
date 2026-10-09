package cl.dsy1102.ejemplos.polimorfismo;

/**
 * Forma de pago aceptada en la caja. Es abstracta: no existe un "medio de
 * pago" generico, siempre es uno concreto (efectivo, debito, credito...).
 *
 * Ya esta resuelta. No la modifiques.
 */
public abstract class MedioPago {

    private final String titular;

    protected MedioPago(String titular) {
        if (titular == null || titular.isBlank()) {
            throw new IllegalArgumentException("El titular es obligatorio.");
        }
        this.titular = titular.trim();
    }

    /** Monto final que paga el cliente por una compra de {@code monto} pesos. */
    public abstract int calcularTotal(int monto);

    /** Nombre del medio para la boleta: "Efectivo", "Debito"... */
    public abstract String getNombre();

    public String getTitular() {
        return titular;
    }
}
