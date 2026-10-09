package cl.dsy1102.veterinaria.model;

/**
 * Una regla clinica impidio registrar la atencion.
 */
public class AtencionRechazadaException extends Exception {

    public AtencionRechazadaException(String motivo) {
        super(motivo);
    }
}
