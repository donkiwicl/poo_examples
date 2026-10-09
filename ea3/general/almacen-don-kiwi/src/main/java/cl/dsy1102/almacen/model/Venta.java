package cl.dsy1102.almacen.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Una venta (boleta) con sus líneas. Se arma en memoria con agregar() y se
 * guarda completa al confirmar.
 */
public class Venta {

    private int id;
    private LocalDateTime fechaHora;
    private final List<LineaVenta> lineas = new ArrayList<>();

    public Venta() {
        this.fechaHora = LocalDateTime.now().withNano(0);
    }

    /**
     * Agrega unidades de un producto, con el precio de hoy. Si el producto ya
     * está en la venta, suma las cantidades en la misma línea.
     *
     * @return el mensaje para el usuario
     */
    public String agregar(Producto producto, int cantidad, LocalDate hoy) throws VentaRechazadaException {
        int indice = indiceDe(producto.getId());
        int yaAgregadas = indice < 0 ? 0 : lineas.get(indice).getCantidad();
        producto.validarVenta(yaAgregadas + cantidad, hoy);

        LineaVenta linea = new LineaVenta(producto.getId(), producto.getCodigo(), producto.getNombre(),
                yaAgregadas + cantidad, producto.precioVenta(hoy));
        if (indice < 0) {
            lineas.add(linea);
        } else {
            lineas.set(indice, linea);
        }
        String descuento = producto.precioVenta(hoy) < producto.getPrecio() ? " (con descuento por vencimiento)" : "";
        return "Agregado: " + cantidad + " x " + producto.getNombre() + descuento + ".";
    }

    public void quitar(int indice) {
        lineas.remove(indice);
    }

    private int indiceDe(int productoId) {
        for (int i = 0; i < lineas.size(); i++) {
            if (lineas.get(i).getProductoId() == productoId) {
                return i;
            }
        }
        return -1;
    }

    public int getTotal() {
        return lineas.stream().mapToInt(LineaVenta::getSubtotal).sum();
    }

    public int getUnidades() {
        return lineas.stream().mapToInt(LineaVenta::getCantidad).sum();
    }

    public boolean estaVacia() {
        return lineas.isEmpty();
    }

    public List<LineaVenta> getLineas() {
        return Collections.unmodifiableList(lineas);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }
}
