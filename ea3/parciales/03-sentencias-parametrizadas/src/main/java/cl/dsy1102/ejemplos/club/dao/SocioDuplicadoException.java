package cl.dsy1102.ejemplos.club.dao;

/**
 * Se intentó registrar un RUT que ya existe (restricción UNIQUE de la tabla).
 */
public class SocioDuplicadoException extends Exception {

    public SocioDuplicadoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
