package cl.dsy1102.almacen.dao;

import cl.dsy1102.almacen.model.ProductoVendido;
import cl.dsy1102.almacen.model.ResumenCategoria;
import cl.dsy1102.almacen.model.Venta;

import java.time.LocalDate;
import java.util.List;

/**
 * Contrato de acceso a las ventas. Solo tiene implementación con base de datos
 * (R5 y R6): la versión JSON nunca registró ventas.
 *
 * En todos los métodos, el período va desde el inicio del día "desde" hasta el
 * final del día "hasta" (ambos inclusive).
 */
public interface VentaDao {

    /**
     * Guarda la venta con sus líneas y descuenta el stock, todo o nada, y le
     * asigna su id. Si otra venta dejó a un producto sin stock suficiente,
     * lanza PersistenciaException("Stock insuficiente de <nombre>...") y no guarda nada.
     */
    void registrar(Venta venta) throws PersistenciaException;

    /** Unidades y total vendido por categoría en el período, del mayor al menor total. */
    List<ResumenCategoria> ventasPorCategoria(LocalDate desde, LocalDate hasta) throws PersistenciaException;

    /** Los productos con más unidades vendidas en el período (a igual cantidad, mayor total primero). */
    List<ProductoVendido> masVendidos(LocalDate desde, LocalDate hasta, int limite) throws PersistenciaException;

    /** Cantidad de ventas (boletas) del período. */
    int contarVentas(LocalDate desde, LocalDate hasta) throws PersistenciaException;
}
