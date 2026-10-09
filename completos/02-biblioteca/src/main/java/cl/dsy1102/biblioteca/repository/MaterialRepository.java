package cl.dsy1102.biblioteca.repository;

import cl.dsy1102.biblioteca.dao.PersistenciaException;
import cl.dsy1102.biblioteca.dao.MaterialDao;
import cl.dsy1102.biblioteca.model.Material;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

/**
 * Mantiene la lista observable de la colección y la persiste mediante el DAO
 * despues de cada cambio.
 *
 * Cada operacion guarda primero una COPIA de la lista y solo si el guardado
 * resulta modifica la lista observable: la tabla nunca muestra un cambio que
 * no quedo en el archivo.
 */
public class MaterialRepository implements Repository<Material> {

    private final MaterialDao dao;
    private final ObservableList<Material> materiales = FXCollections.observableArrayList();

    public MaterialRepository(MaterialDao dao) {
        this.dao = dao;
    }

    @Override
    public void cargar() throws PersistenciaException {
        materiales.setAll(dao.cargar());
    }

    @Override
    public ObservableList<Material> listar() {
        return materiales;
    }

    /** @throws IllegalArgumentException si ya existe un material con el mismo codigo */
    @Override
    public void agregar(Material material) throws PersistenciaException {
        if (buscarPorCodigo(material.getCodigo()) != null) {
            throw new IllegalArgumentException("Ya existe un material con el código " + material.getCodigo() + ".");
        }
        List<Material> copia = new ArrayList<>(materiales);
        copia.add(material);
        dao.guardar(copia);
        materiales.add(material);
    }

    @Override
    public void actualizar(Material original, Material actualizado) throws PersistenciaException {
        int indice = materiales.indexOf(original);
        if (indice < 0) {
            throw new IllegalArgumentException("El material a actualizar no está registrado.");
        }
        List<Material> copia = new ArrayList<>(materiales);
        copia.set(indice, actualizado);
        dao.guardar(copia);
        // set() reemplaza el elemento: la tabla recibe el aviso y redibuja la fila.
        materiales.set(indice, actualizado);
    }

    @Override
    public void eliminar(Material material) throws PersistenciaException {
        List<Material> copia = new ArrayList<>(materiales);
        copia.remove(material);
        dao.guardar(copia);
        materiales.remove(material);
    }

    private Material buscarPorCodigo(String codigo) {
        return materiales.stream().filter(v -> v.getCodigo().equalsIgnoreCase(codigo)).findFirst().orElse(null);
    }
}
