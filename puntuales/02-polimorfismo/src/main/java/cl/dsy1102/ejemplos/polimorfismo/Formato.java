package cl.dsy1102.ejemplos.polimorfismo;

import java.util.Locale;

/** Utilidad para mostrar montos en pesos chilenos. */
public final class Formato {

    private static final Locale CHILE = Locale.of("es", "CL");

    private Formato() {
    }

    /** 12345 -> "$12.345", igual en cualquier equipo. */
    public static String pesos(int monto) {
        return String.format(CHILE, "$%,d", monto);
    }
}
