package cl.dsy1102.arriendo.repository;

import cl.dsy1102.arriendo.dao.PersistenciaException;
import cl.dsy1102.arriendo.dao.VehiculoDao;
import cl.dsy1102.arriendo.model.Vehiculo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

/**
 * Mantiene la lista observable de la flota y la persiste mediante el DAO
 * despues de cada cambio.
 *
 * Cada operacion guarda primero una COPIA de la lista y solo si el guardado
 * resulta modifica la lista observable: la tabla nunca muestra un cambio que
 * no quedo en el archivo.
 */
public class VehiculoRepository implements Repository<Vehiculo> {

    private final VehiculoDao dao;
    private final ObservableList<Vehiculo> vehiculos = FXCollections.observableArrayList();

    public VehiculoRepository(VehiculoDao dao) {
        this.dao = dao;
    }

    @Override
    public void cargar() throws PersistenciaException {
        vehiculos.setAll(dao.cargar());
    }

    @Override
    public ObservableList<Vehiculo> listar() {
        return vehiculos;
    }

    /** @throws IllegalArgumentException si ya existe un vehiculo con el mismo codigo */
    @Override
    public void agregar(Vehiculo vehiculo) throws PersistenciaException {
        if (buscarPorCodigo(vehiculo.getCodigo()) != null) {
            throw new IllegalArgumentException("Ya existe un vehículo con el código " + vehiculo.getCodigo() + ".");
        }
        List<Vehiculo> copia = new ArrayList<>(vehiculos);
        copia.add(vehiculo);
        dao.guardar(copia);
        vehiculos.add(vehiculo);
    }

    @Override
    public void actualizar(Vehiculo original, Vehiculo actualizado) throws PersistenciaException {
        int indice = vehiculos.indexOf(original);
        if (indice < 0) {
            throw new IllegalArgumentException("El vehículo a actualizar no está registrado.");
        }
        List<Vehiculo> copia = new ArrayList<>(vehiculos);
        copia.set(indice, actualizado);
        dao.guardar(copia);
        // set() reemplaza el elemento: la tabla recibe el aviso y redibuja la fila.
        vehiculos.set(indice, actualizado);
    }

    @Override
    public void eliminar(Vehiculo vehiculo) throws PersistenciaException {
        List<Vehiculo> copia = new ArrayList<>(vehiculos);
        copia.remove(vehiculo);
        dao.guardar(copia);
        vehiculos.remove(vehiculo);
    }

    private Vehiculo buscarPorCodigo(String codigo) {
        return vehiculos.stream().filter(v -> v.getCodigo().equalsIgnoreCase(codigo)).findFirst().orElse(null);
    }
}
