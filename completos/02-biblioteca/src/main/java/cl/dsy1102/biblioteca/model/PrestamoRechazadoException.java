package cl.dsy1102.biblioteca.model;

/**
 * Una regla de negocio impidio el prestamo o la devolucion.
 */
public class PrestamoRechazadoException extends Exception {

    public PrestamoRechazadoException(String motivo) {
        super(motivo);
    }
}
