package cl.dsy1102.arriendo.model;

/**
 * Vehiculo con bateria. Solo se arrienda con carga suficiente.
 */
public interface Recargable {

    int BATERIA_MINIMA = 30;

    int getBateria();

    /** Deja la bateria al 100 %. */
    void recargar();

    default boolean tieneCargaSuficiente() {
        return getBateria() >= BATERIA_MINIMA;
    }
}
