package cl.dsy1102.almacen.dao;

import cl.dsy1102.almacen.model.Producto;

import java.util.List;

/**
 * Contrato de acceso a los productos. Los repositorios y controladores solo
 * conocen esta interfaz, no dónde ni cómo se guardan los datos.
 */
public interface ProductoDao {

    /** Todos los productos, ordenados por código. Lista vacía si no hay datos. */
    List<Producto> listarTodos() throws PersistenciaException;

    /**
     * Guarda un producto nuevo y le asigna su id.
     * Un código repetido lanza PersistenciaException("Ya existe un producto con el código AB-123.").
     */
    void insertar(Producto producto) throws PersistenciaException;

    /** Actualiza el producto con ese id. Retorna false si ya no existe. */
    boolean actualizar(Producto producto) throws PersistenciaException;

    /** Elimina el producto. Retorna false si ya no existía. */
    boolean eliminar(int id) throws PersistenciaException;
}
