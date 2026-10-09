package cl.dsy1102.veterinaria.model;

/**
 * Tamano de un perro: define el factor por el que se multiplica el precio base.
 */
public enum Tamano {

    PEQUENO("Pequeño", 1.0),
    MEDIANO("Mediano", 1.2),
    GRANDE("Grande", 1.5);

    private final String nombre;
    private final double factor;

    Tamano(String nombre, double factor) {
        this.nombre = nombre;
        this.factor = factor;
    }

    public double getFactor() {
        return factor;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
