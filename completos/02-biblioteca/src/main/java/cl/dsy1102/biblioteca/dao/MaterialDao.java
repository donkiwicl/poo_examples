package cl.dsy1102.biblioteca.dao;

import cl.dsy1102.biblioteca.model.Material;

import java.util.List;

/**
 * Contrato de acceso a los datos de la colección.
 */
public interface MaterialDao {

    /** Todos los materiales guardados; lista vacia si aun no hay datos. */
    List<Material> cargar() throws PersistenciaException;

    /** Reemplaza los datos guardados por la lista recibida. */
    void guardar(List<Material> materiales) throws PersistenciaException;
}
