package cl.dsy1102.ejemplos.polimorfismo;

/**
 * Capacidad de acumular puntos del programa de fidelizacion.
 *
 * No todos los medios de pago la tienen, por eso es una interfaz y no un
 * metodo de MedioPago. Ya esta resuelta.
 */
public interface Acumulable {

    /** Puntos que otorga un pago por {@code totalPagado} pesos. */
    int calcularPuntos(int totalPagado);
}
