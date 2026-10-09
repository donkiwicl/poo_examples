package cl.dsy1102.ejemplos.cooperativa.dao;

/**
 * Una regla del negocio impidió la operación (saldo insuficiente, cuenta
 * inexistente...). La base de datos quedó sin cambios.
 */
public class OperacionRechazadaException extends Exception {

    public OperacionRechazadaException(String mensaje) {
        super(mensaje);
    }
}
