package cl.dsy1102.almacen.model;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Formatos que se muestran al usuario.
 */
public final class Formato {

    public static final Locale CHILE = Locale.of("es", "CL");
    public static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private Formato() {
    }

    /** 12900 → "$12.900" */
    public static String pesos(long monto) {
        return String.format(CHILE, "$%,d", monto);
    }
}
