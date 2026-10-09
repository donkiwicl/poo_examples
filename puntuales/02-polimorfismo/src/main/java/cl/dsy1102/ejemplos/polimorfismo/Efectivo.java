package cl.dsy1102.ejemplos.polimorfismo;

/**
 * Ejemplo resuelto. En efectivo el total se redondea a la decena, como exige
 * la ley desde que no circulan monedas de $1 y $5: si termina en 1 a 5 baja,
 * si termina en 6 a 9 sube.
 */
public class Efectivo extends MedioPago {

    public Efectivo(String titular) {
        super(titular);
    }

    @Override
    public int calcularTotal(int monto) {
        int unidad = monto % 10;
        return unidad <= 5 ? monto - unidad : monto + (10 - unidad);
    }

    @Override
    public String getNombre() {
        return "Efectivo";
    }
}
