package cl.dsy1102.ejemplos.navegacion.model;

/**
 * Una linea del pedido: un plato y cuantas unidades. Ya esta resuelta.
 */
public class LineaPedido {

    private final Plato plato;
    private final int cantidad;

    public LineaPedido(Plato plato, int cantidad) {
        this.plato = plato;
        this.cantidad = cantidad;
    }

    public int getSubtotal() {
        return plato.getPrecio() * cantidad;
    }

    public Plato getPlato() {
        return plato;
    }

    public int getCantidad() {
        return cantidad;
    }

    @Override
    public String toString() {
        return cantidad + " x " + plato.getNombre() + "  ·  " + Plato.pesos(getSubtotal());
    }
}
