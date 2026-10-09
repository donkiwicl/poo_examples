package cl.dsy1102.almacen.repository;

import cl.dsy1102.almacen.dao.PersistenciaException;
import javafx.collections.ObservableList;

/**
 * Operaciones CRUD que usan los controladores. La lista que retorna listar()
 * se enlaza a la vista y refleja cada cambio.
 */
public interface Repository<T> {

    /** Lee (o vuelve a leer) todos los datos desde el DAO. */
    void cargar() throws PersistenciaException;

    ObservableList<T> listar();

    void agregar(T elemento) throws PersistenciaException;

    void actualizar(T original, T actualizado) throws PersistenciaException;

    void eliminar(T elemento) throws PersistenciaException;
}
