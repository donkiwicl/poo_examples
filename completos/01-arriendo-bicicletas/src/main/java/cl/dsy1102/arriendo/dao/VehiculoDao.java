package cl.dsy1102.arriendo.dao;

import cl.dsy1102.arriendo.model.Vehiculo;

import java.util.List;

/**
 * Contrato de acceso a los datos de la flota.
 */
public interface VehiculoDao {

    /** Todos los vehiculos guardados; lista vacia si aun no hay datos. */
    List<Vehiculo> cargar() throws PersistenciaException;

    /** Reemplaza los datos guardados por la lista recibida. */
    void guardar(List<Vehiculo> vehiculos) throws PersistenciaException;
}
