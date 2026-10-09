package cl.dsy1102.veterinaria.model;

/**
 * Servicios de la clinica. Un enum puede tener atributos, constructor y
 * metodos: cada constante lleva su nombre visible y su precio base.
 */
public enum TipoAtencion {

    CONSULTA("Consulta general", 15_000),
    VACUNACION("Vacunación", 12_000),
    DESPARASITACION("Desparasitación", 9_000),
    CONTROL_PESO("Control de peso", 6_000),
    CIRUGIA_MENOR("Cirugía menor", 85_000);

    private final String nombre;
    private final int precioBase;

    TipoAtencion(String nombre, int precioBase) {
        this.nombre = nombre;
        this.precioBase = precioBase;
    }

    public String getNombre() {
        return nombre;
    }

    public int getPrecioBase() {
        return precioBase;
    }

    /** Lo que muestran ComboBox y tablas. */
    @Override
    public String toString() {
        return nombre;
    }
}
