package cl.dsy1102.biblioteca.dao;

/**
 * Error al leer o escribir los datos. Los controladores solo conocen esta
 * excepcion: nunca la IOException del archivo.
 */
public class PersistenciaException extends Exception {

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
