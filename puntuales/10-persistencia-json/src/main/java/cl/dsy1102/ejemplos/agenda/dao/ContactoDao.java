package cl.dsy1102.ejemplos.agenda.dao;

import cl.dsy1102.ejemplos.agenda.model.Contacto;

import java.util.List;

/**
 * Contrato de acceso a datos. Quien lo usa no sabe si los contactos estan en
 * un JSON, un CSV o una base de datos. Ya esta resuelto.
 */
public interface ContactoDao {

    /** Retorna todos los contactos guardados; lista vacia si aun no hay datos. */
    List<Contacto> cargar() throws PersistenciaException;

    /** Reemplaza los datos guardados por la lista recibida. */
    void guardar(List<Contacto> contactos) throws PersistenciaException;
}
