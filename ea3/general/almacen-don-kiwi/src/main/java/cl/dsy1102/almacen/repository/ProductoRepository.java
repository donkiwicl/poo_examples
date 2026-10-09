package cl.dsy1102.almacen.repository;

import cl.dsy1102.almacen.dao.PersistenciaException;
import cl.dsy1102.almacen.dao.ProductoDao;
import cl.dsy1102.almacen.model.Producto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Mantiene la lista observable de productos sincronizada con el DAO: primero
 * guarda y, solo si el DAO no falla, modifica la lista que ve la interfaz.
 */
public class ProductoRepository implements Repository<Producto> {

    private final ProductoDao dao;
    private final ObservableList<Producto> productos = FXCollections.observableArrayList();

    public ProductoRepository(ProductoDao dao) {
        this.dao = dao;
    }

    @Override
    public void cargar() throws PersistenciaException {
        productos.setAll(dao.listarTodos());
    }

    @Override
    public ObservableList<Producto> listar() {
        return productos;
    }

    @Override
    public void agregar(Producto producto) throws PersistenciaException {
        dao.insertar(producto);
        productos.add(producto);
    }

    @Override
    public void actualizar(Producto original, Producto actualizado) throws PersistenciaException {
        int indice = productos.indexOf(original);
        if (indice < 0) {
            throw new IllegalArgumentException("El producto a actualizar no está en la lista.");
        }
        if (!dao.actualizar(actualizado)) {
            productos.remove(indice);
            throw new PersistenciaException("El producto " + original.getCodigo() + " ya no existe.");
        }
        productos.set(indice, actualizado);
    }

    @Override
    public void eliminar(Producto producto) throws PersistenciaException {
        dao.eliminar(producto.getId());
        productos.remove(producto);
    }
}
