package cl.dsy1102.ejemplos.lavanderia.dao;

import cl.dsy1102.ejemplos.lavanderia.model.Cliente;

import java.util.List;

/**
 * Contrato de acceso a los datos de los clientes. El controlador trabaja con
 * esta interfaz y no sabe si los datos están en MySQL, en otro motor o en memoria.
 */
public interface ClienteDao {

    /** Todos los clientes, ordenados por nombre. */
    List<Cliente> listarTodos() throws DaoException;

    /** Clientes cuyo nombre contiene el texto, sin distinguir mayúsculas. */
    List<Cliente> buscarPorNombre(String texto) throws DaoException;

    /** Guarda un cliente nuevo y le asigna el id generado. */
    void insertar(Cliente cliente) throws DaoException;

    /** Actualiza el cliente con ese id. Retorna false si ya no existe. */
    boolean actualizar(Cliente cliente) throws DaoException;

    /** Elimina el cliente. Retorna false si ya no existía. */
    boolean eliminar(int id) throws DaoException;
}
