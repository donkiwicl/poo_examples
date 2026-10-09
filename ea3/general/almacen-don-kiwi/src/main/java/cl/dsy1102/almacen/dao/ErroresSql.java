package cl.dsy1102.almacen.dao;

import java.sql.SQLException;

/**
 * Traduce una SQLException a una PersistenciaException con un mensaje para el
 * usuario. La excepción original queda como causa.
 */
final class ErroresSql {

    private ErroresSql() {
    }

    static PersistenciaException traducir(String accion, SQLException e) {
        String estado = e.getSQLState() == null ? "" : e.getSQLState();
        if (estado.startsWith("08")) {
            return new PersistenciaException("No hay conexión con la base de datos. Revisa que MySQL esté"
                    + " iniciado y los datos de db.properties.", e);
        }
        return new PersistenciaException("No se pudo " + accion + ": " + e.getMessage(), e);
    }
}
