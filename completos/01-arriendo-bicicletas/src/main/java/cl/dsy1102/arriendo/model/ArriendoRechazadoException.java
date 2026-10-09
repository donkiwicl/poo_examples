package cl.dsy1102.arriendo.model;

/**
 * Una regla de negocio impidio el arriendo o la devolucion.
 */
public class ArriendoRechazadoException extends Exception {

    public ArriendoRechazadoException(String motivo) {
        super(motivo);
    }
}
