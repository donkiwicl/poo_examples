package cl.dsy1102.almacen.model;

/**
 * Una línea de una venta. Guarda el precio cobrado: si el precio del producto
 * cambia después, la venta no cambia.
 */
public class LineaVenta {

    private final int productoId;
    private final String codigo;
    private final String nombre;
    private final int cantidad;
    private final int precioUnitario;

    public LineaVenta(int productoId, String codigo, String nombre, int cantidad, int precioUnitario) {
        this.productoId = productoId;
        this.codigo = codigo;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public int getProductoId() { return productoId; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public int getCantidad() { return cantidad; }
    public int getPrecioUnitario() { return precioUnitario; }

    public int getSubtotal() {
        return cantidad * precioUnitario;
    }

    @Override
    public String toString() {
        return cantidad + " x " + nombre + " a " + Formato.pesos(precioUnitario) + " = " + Formato.pesos(getSubtotal());
    }
}
