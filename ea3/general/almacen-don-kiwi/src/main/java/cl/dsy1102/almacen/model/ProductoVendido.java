package cl.dsy1102.almacen.model;

/**
 * Fila del reporte de productos más vendidos.
 */
public record ProductoVendido(String codigo, String nombre, int unidades, int total) {
}
