package cl.dsy1102.almacen.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Producto con fecha de vencimiento. A 3 días o menos de vencer se vende con
 * 30 % de descuento; vencido no se puede vender.
 */
public class ProductoPerecible extends Producto {

    public static final int DIAS_DESCUENTO = 3;
    public static final double DESCUENTO = 0.30;

    private LocalDate fechaVencimiento;

    protected ProductoPerecible() {
    }

    public ProductoPerecible(String codigo, String nombre, Categoria categoria, int precio, int stock,
                             LocalDate fechaVencimiento) {
        super(codigo, nombre, categoria, precio, stock);
        setFechaVencimiento(fechaVencimiento);
    }

    @Override
    public String obtenerTipo() {
        return "Perecible";
    }

    public long diasParaVencer(LocalDate hoy) {
        return ChronoUnit.DAYS.between(hoy, fechaVencimiento);
    }

    public boolean estaVencido(LocalDate hoy) {
        return fechaVencimiento.isBefore(hoy);
    }

    @Override
    public int precioVenta(LocalDate hoy) {
        if (!estaVencido(hoy) && diasParaVencer(hoy) <= DIAS_DESCUENTO) {
            return (int) Math.round(getPrecio() * (1 - DESCUENTO));
        }
        return getPrecio();
    }

    @Override
    public void validarVenta(int cantidad, LocalDate hoy) throws VentaRechazadaException {
        if (estaVencido(hoy)) {
            throw new VentaRechazadaException(getNombre() + " está vencido desde el "
                    + fechaVencimiento.format(Formato.FECHA) + ".");
        }
        super.validarVenta(cantidad, hoy);
    }

    @Override
    public String obtenerDetalle() {
        return super.obtenerDetalle() + " · vence " + fechaVencimiento.format(Formato.FECHA);
    }

    @Override
    public Producto copiar() {
        ProductoPerecible copia = new ProductoPerecible();
        copiarDatosEn(copia);
        copia.fechaVencimiento = fechaVencimiento;
        return copia;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        if (fechaVencimiento == null) {
            throw new IllegalArgumentException("La fecha de vencimiento es obligatoria.");
        }
        this.fechaVencimiento = fechaVencimiento;
    }
}
