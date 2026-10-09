package cl.dsy1102.ejemplos.inscripcion;

/**
 * Modelo de una inscripcion al taller. Ya esta resuelto.
 */
public class Inscripcion {

    public static final int EDAD_MINIMA = 8;
    public static final int EDAD_MAXIMA = 99;

    private final String nombre;
    private final int edad;
    private final String nivel;
    private final String horario;
    private final String pareja;

    /**
     * @param pareja nombre de la pareja de baile, o null si viene sin pareja
     * @throws IllegalArgumentException si algun dato no cumple las reglas
     */
    public Inscripcion(String nombre, int edad, String nivel, String horario, String pareja) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (edad < EDAD_MINIMA || edad > EDAD_MAXIMA) {
            throw new IllegalArgumentException("La edad debe estar entre " + EDAD_MINIMA + " y " + EDAD_MAXIMA + " años.");
        }
        if (nivel == null || horario == null) {
            throw new IllegalArgumentException("El nivel y el horario son obligatorios.");
        }
        if (pareja != null && pareja.isBlank()) {
            throw new IllegalArgumentException("Si viene con pareja, indica su nombre.");
        }
        this.nombre = nombre.trim();
        this.edad = edad;
        this.nivel = nivel;
        this.horario = horario;
        this.pareja = pareja == null ? null : pareja.trim();
    }

    public String obtenerDetalle() {
        return nombre + "\n"
                + "Edad: " + edad + " años\n"
                + "Nivel: " + nivel + "\n"
                + "Horario: " + horario + "\n"
                + "Pareja: " + (pareja == null ? "sin pareja" : pareja);
    }

    /** Texto que muestra el ListView para cada inscripcion. */
    @Override
    public String toString() {
        return nombre + " (" + nivel + ", " + horario + ")";
    }

    public String getNombre() {
        return nombre;
    }

    public int getEdad() {
        return edad;
    }

    public String getNivel() {
        return nivel;
    }

    public String getHorario() {
        return horario;
    }

    public String getPareja() {
        return pareja;
    }
}
