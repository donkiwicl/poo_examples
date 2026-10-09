package cl.dsy1102.veterinaria.dao;

import cl.dsy1102.veterinaria.model.Paciente;

import java.util.List;

/**
 * Contrato de acceso a los datos de los pacientes.
 */
public interface PacienteDao {

    /** Todos los pacientes guardados; lista vacia si aun no hay datos. */
    List<Paciente> cargar() throws PersistenciaException;

    /** Reemplaza los datos guardados por la lista recibida. */
    void guardar(List<Paciente> pacientes) throws PersistenciaException;
}
