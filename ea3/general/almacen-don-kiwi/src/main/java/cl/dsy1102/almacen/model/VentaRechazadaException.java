package cl.dsy1102.almacen.model;

/**
 * Una regla del negocio impide vender el producto (sin stock, vencido...).
 */
public class VentaRechazadaException extends Exception {

    public VentaRechazadaException(String mensaje) {
        super(mensaje);
    }
}
