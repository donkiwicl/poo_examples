package cl.dsy1102.almacen.dao;

/**
 * Error al leer o escribir los datos. Los controladores solo conocen esta
 * excepción: nunca la IOException ni la SQLException.
 */
public class PersistenciaException extends Exception {

    public PersistenciaException(String mensaje) {
        super(mensaje);
    }

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
