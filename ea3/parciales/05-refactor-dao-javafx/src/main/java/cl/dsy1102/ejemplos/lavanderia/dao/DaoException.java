package cl.dsy1102.ejemplos.lavanderia.dao;

/**
 * Error al acceder a los datos. El controlador solo conoce esta excepción:
 * nunca la SQLException del driver.
 */
public class DaoException extends Exception {

    public DaoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
