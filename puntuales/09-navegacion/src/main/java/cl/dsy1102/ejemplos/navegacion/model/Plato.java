package cl.dsy1102.ejemplos.navegacion.model;

import java.util.Locale;

/**
 * Plato de la carta. Ya esta resuelto.
 */
public class Plato {

    private static final Locale CHILE = Locale.of("es", "CL");

    private final String nombre;
    private final String descripcion;
    private final int precio;

    public Plato(String nombre, String descripcion, int precio) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
    }

    public static String pesos(int monto) {
        return String.format(CHILE, "$%,d", monto);
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getPrecio() {
        return precio;
    }

    @Override
    public String toString() {
        return nombre + "  ·  " + pesos(precio);
    }
}
