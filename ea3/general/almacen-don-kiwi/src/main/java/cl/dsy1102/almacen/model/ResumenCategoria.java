package cl.dsy1102.almacen.model;

/**
 * Fila del reporte de ventas por categoría.
 */
public record ResumenCategoria(Categoria categoria, int unidades, int total) {
}
