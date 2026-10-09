package cl.dsy1102.ejemplos.navegacion.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Pedido en curso. Es el estado que comparten las vistas: se crea una vez
 * en AppPicada y se entrega a cada controlador al navegar.
 *
 * Ya esta resuelto.
 */
public class Pedido {

    public static final int MAXIMO_POR_PLATO = 10;

    private final ObservableList<LineaPedido> lineas = FXCollections.observableArrayList();
    private String nombreRetiro;

    /** Agrega unidades de un plato; si ya estaba en el pedido, suma a esa linea. */
    public void agregar(Plato plato, int cantidad) {
        if (cantidad < 1) {
            throw new IllegalArgumentException("La cantidad debe ser al menos 1.");
        }
        for (int i = 0; i < lineas.size(); i++) {
            LineaPedido linea = lineas.get(i);
            if (linea.getPlato() == plato) {
                int nueva = linea.getCantidad() + cantidad;
                if (nueva > MAXIMO_POR_PLATO) {
                    throw new IllegalArgumentException("Máximo " + MAXIMO_POR_PLATO + " unidades de " + plato.getNombre() + ".");
                }
                lineas.set(i, new LineaPedido(plato, nueva));
                return;
            }
        }
        if (cantidad > MAXIMO_POR_PLATO) {
            throw new IllegalArgumentException("Máximo " + MAXIMO_POR_PLATO + " unidades de " + plato.getNombre() + ".");
        }
        lineas.add(new LineaPedido(plato, cantidad));
    }

    public void confirmar(String nombreRetiro) {
        if (lineas.isEmpty()) {
            throw new IllegalStateException("El pedido está vacío.");
        }
        if (nombreRetiro == null || nombreRetiro.isBlank()) {
            throw new IllegalArgumentException("Indica un nombre para el retiro.");
        }
        this.nombreRetiro = nombreRetiro.trim();
    }

    /** Deja el pedido listo para un nuevo cliente. */
    public void vaciar() {
        lineas.clear();
        nombreRetiro = null;
    }

    public int getTotal() {
        return lineas.stream().mapToInt(LineaPedido::getSubtotal).sum();
    }

    public int getCantidadItems() {
        return lineas.stream().mapToInt(LineaPedido::getCantidad).sum();
    }

    public ObservableList<LineaPedido> getLineas() {
        return lineas;
    }

    public String getNombreRetiro() {
        return nombreRetiro;
    }
}
