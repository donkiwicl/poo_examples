package cl.dsy1102.ejemplos.polimorfismo;

import java.util.ArrayList;
import java.util.List;

/**
 * Caja del almacen. Cobra con cualquier medio de pago sin saber cual es.
 */
public class Caja {

    private final List<String> boletas = new ArrayList<>();
    private int totalRecaudado;
    private int puntosOtorgados;

    /**
     * Cobra una compra y retorna la linea de boleta, por ejemplo:
     * "Credito 3 cuotas | Ana Rojas | Compra: $60.000 | Total: $61.800 | Puntos: 618"
     *
     * @throws IllegalArgumentException si el monto no es mayor que 0
     */
    public String cobrar(int monto, MedioPago medio) {
        // TODO R3: valida el monto, calcula el total con el medio de pago,
        //  acumula el total recaudado y, si el medio es Acumulable, sus puntos.
        //  Registra la linea en boletas y retornala.
        //  Prohibido: preguntar por la clase concreta (instanceof Efectivo, getClass()...).
        return "";
    }

    public List<String> getBoletas() {
        return List.copyOf(boletas);
    }

    public int getTotalRecaudado() {
        return totalRecaudado;
    }

    public int getPuntosOtorgados() {
        return puntosOtorgados;
    }
}
