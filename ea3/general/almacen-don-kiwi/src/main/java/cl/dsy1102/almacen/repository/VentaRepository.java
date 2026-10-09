package cl.dsy1102.almacen.repository;

import cl.dsy1102.almacen.dao.PersistenciaException;
import cl.dsy1102.almacen.dao.VentaDao;
import cl.dsy1102.almacen.model.Producto;
import cl.dsy1102.almacen.model.ProductoVendido;
import cl.dsy1102.almacen.model.ResumenCategoria;
import cl.dsy1102.almacen.model.Venta;

import java.time.LocalDate;
import java.util.List;

/**
 * Operaciones de ventas que usan los controladores. Después de registrar una
 * venta vuelve a leer los productos, para que la interfaz muestre el stock real.
 */
public class VentaRepository {

    private final VentaDao dao;
    private final Repository<Producto> productos;

    public VentaRepository(VentaDao dao, Repository<Producto> productos) {
        this.dao = dao;
        this.productos = productos;
    }

    public void registrar(Venta venta) throws PersistenciaException {
        try {
            dao.registrar(venta);
        } finally {
            // También si falla: si otra venta se llevó el stock, la interfaz debe mostrarlo.
            recargarProductos();
        }
    }

    private void recargarProductos() {
        try {
            productos.cargar();
        } catch (PersistenciaException e) {
            // La venta ya quedó registrada (o rechazada): no se informa como error de la venta.
            // La lista conserva los datos anteriores y el botón Recargar permite reintentar.
        }
    }

    public List<ResumenCategoria> ventasPorCategoria(LocalDate desde, LocalDate hasta) throws PersistenciaException {
        return dao.ventasPorCategoria(desde, hasta);
    }

    public List<ProductoVendido> masVendidos(LocalDate desde, LocalDate hasta, int limite) throws PersistenciaException {
        return dao.masVendidos(desde, hasta, limite);
    }

    public int contarVentas(LocalDate desde, LocalDate hasta) throws PersistenciaException {
        return dao.contarVentas(desde, hasta);
    }
}
