package cl.dsy1102.veterinaria.repository;

import cl.dsy1102.veterinaria.dao.PersistenciaException;
import cl.dsy1102.veterinaria.dao.PacienteDao;
import cl.dsy1102.veterinaria.model.Paciente;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

/**
 * Mantiene la lista observable de los pacientes y la persiste mediante el DAO
 * despues de cada cambio.
 *
 * Cada operacion guarda primero una COPIA de la lista y solo si el guardado
 * resulta modifica la lista observable: la tabla nunca muestra un cambio que
 * no quedo en el archivo.
 */
public class PacienteRepository implements Repository<Paciente> {

    private final PacienteDao dao;
    private final ObservableList<Paciente> pacientes = FXCollections.observableArrayList();

    public PacienteRepository(PacienteDao dao) {
        this.dao = dao;
    }

    @Override
    public void cargar() throws PersistenciaException {
        pacientes.setAll(dao.cargar());
    }

    @Override
    public ObservableList<Paciente> listar() {
        return pacientes;
    }

    /** @throws IllegalArgumentException si el tutor ya tiene un paciente con el mismo nombre */
    @Override
    public void agregar(Paciente paciente) throws PersistenciaException {
        boolean repetido = pacientes.stream().anyMatch(p -> p.getNombre().equalsIgnoreCase(paciente.getNombre())
                && p.getTutor().equalsIgnoreCase(paciente.getTutor()));
        if (repetido) {
            throw new IllegalArgumentException(paciente.getTutor() + " ya tiene un paciente llamado " + paciente.getNombre() + ".");
        }
        List<Paciente> copia = new ArrayList<>(pacientes);
        copia.add(paciente);
        dao.guardar(copia);
        pacientes.add(paciente);
    }

    @Override
    public void actualizar(Paciente original, Paciente actualizado) throws PersistenciaException {
        int indice = pacientes.indexOf(original);
        if (indice < 0) {
            throw new IllegalArgumentException("El paciente a actualizar no está registrado.");
        }
        List<Paciente> copia = new ArrayList<>(pacientes);
        copia.set(indice, actualizado);
        dao.guardar(copia);
        // set() reemplaza el elemento: la tabla recibe el aviso y redibuja la fila.
        pacientes.set(indice, actualizado);
    }

    @Override
    public void eliminar(Paciente paciente) throws PersistenciaException {
        List<Paciente> copia = new ArrayList<>(pacientes);
        copia.remove(paciente);
        dao.guardar(copia);
        pacientes.remove(paciente);
    }
}
