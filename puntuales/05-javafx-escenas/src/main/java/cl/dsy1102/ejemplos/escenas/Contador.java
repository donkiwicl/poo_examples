package cl.dsy1102.ejemplos.escenas;

/**
 * Modelo: cuenta las personas dentro de la ramada sin superar el aforo.
 * No sabe nada de JavaFX. Ya esta resuelto.
 */
public class Contador {

    private final int aforoMaximo;
    private int personas;

    public Contador(int aforoMaximo) {
        if (aforoMaximo <= 0) {
            throw new IllegalArgumentException("El aforo debe ser mayor que 0");
        }
        this.aforoMaximo = aforoMaximo;
    }

    /** Registra una entrada. Retorna false si el aforo ya esta completo. */
    public boolean entrar() {
        if (estaCompleto()) {
            return false;
        }
        personas++;
        return true;
    }

    /** Registra una salida. Retorna false si no hay nadie dentro. */
    public boolean salir() {
        if (personas == 0) {
            return false;
        }
        personas--;
        return true;
    }

    public boolean estaCompleto() {
        return personas >= aforoMaximo;
    }

    /** Ocupacion entre 0.0 y 1.0, lista para una ProgressBar. */
    public double getOcupacion() {
        return (double) personas / aforoMaximo;
    }

    public int getPersonas() {
        return personas;
    }

    public int getAforoMaximo() {
        return aforoMaximo;
    }
}
