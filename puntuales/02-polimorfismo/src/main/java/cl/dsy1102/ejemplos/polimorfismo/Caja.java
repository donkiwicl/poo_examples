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
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto de la compra debe ser mayor que 0.");
        }
        // Polimorfismo: se ejecuta el calcularTotal() de la clase real del objeto.
        int total = medio.calcularTotal(monto);
        totalRecaudado += total;

        String linea = medio.getNombre() + " | " + medio.getTitular()
                + " | Compra: " + Formato.pesos(monto) + " | Total: " + Formato.pesos(total);

        // Se pregunta por una capacidad (interfaz), no por una clase concreta.
        if (medio instanceof Acumulable acumulable) {
            int puntos = acumulable.calcularPuntos(total);
            puntosOtorgados += puntos;
            linea += " | Puntos: " + puntos;
        }
        boletas.add(linea);
        return linea;
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
