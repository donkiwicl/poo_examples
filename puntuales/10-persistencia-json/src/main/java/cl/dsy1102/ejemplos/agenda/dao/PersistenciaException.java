package cl.dsy1102.ejemplos.agenda.dao;

/**
 * Error al leer o escribir los datos. Traduce la IOException (o el error de
 * formato) a un mensaje que la capa superior puede mostrar al usuario.
 *
 * Ya esta resuelta.
 */
public class PersistenciaException extends Exception {

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
