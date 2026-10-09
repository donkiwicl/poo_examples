package cl.dsy1102.almacen.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDate;

/**
 * Producto del almacén. El id es 0 mientras el producto no se ha guardado.
 *
 * Las anotaciones de Jackson son de la versión EA2 (persistencia en JSON).
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "tipo")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ProductoPerecible.class, name = "PERECIBLE"),
        @JsonSubTypes.Type(value = ProductoNoPerecible.class, name = "NO_PERECIBLE")
})
public abstract class Producto {

    public static final int STOCK_BAJO = 5;

    private int id;
    private String codigo;
    private String nombre;
    private Categoria categoria;
    private int precio;
    private int stock;

    protected Producto() {
    }

    protected Producto(String codigo, String nombre, Categoria categoria, int precio, int stock) {
        setCodigo(codigo);
        setNombre(nombre);
        setCategoria(categoria);
        setPrecio(precio);
        setStock(stock);
    }

    /** "Perecible" o "No perecible". */
    public abstract String obtenerTipo();

    /** Copia independiente, para editar sin tocar el original hasta guardar. */
    public abstract Producto copiar();

    /** Precio que se cobra hoy. Por defecto, el precio de lista. */
    public int precioVenta(LocalDate hoy) {
        return precio;
    }

    /**
     * Revisa si se pueden vender esas unidades hoy.
     */
    public void validarVenta(int cantidad, LocalDate hoy) throws VentaRechazadaException {
        if (cantidad <= 0) {
            throw new VentaRechazadaException("La cantidad debe ser mayor que cero.");
        }
        if (cantidad > stock) {
            throw new VentaRechazadaException("Stock insuficiente de " + nombre + ": quedan " + stock + ".");
        }
    }

    public boolean tieneStockBajo() {
        return stock < STOCK_BAJO;
    }

    public String obtenerDetalle() {
        return codigo + " · " + nombre + " · " + categoria + " · " + Formato.pesos(precio) + " · stock " + stock;
    }

    /** Copia los datos comunes en otro producto (lo usan las subclases en copiar()). */
    protected void copiarDatosEn(Producto copia) {
        copia.id = id;
        copia.codigo = codigo;
        copia.nombre = nombre;
        copia.categoria = categoria;
        copia.precio = precio;
        copia.stock = stock;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        if (codigo == null || !codigo.trim().toUpperCase().matches("[A-Z]{2}-\\d{3}")) {
            throw new IllegalArgumentException("El código debe tener el formato AB-123.");
        }
        this.codigo = codigo.trim().toUpperCase();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank() || nombre.trim().length() > 60) {
            throw new IllegalArgumentException("El nombre es obligatorio y tiene hasta 60 caracteres.");
        }
        this.nombre = nombre.trim();
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException("La categoría es obligatoria.");
        }
        this.categoria = categoria;
    }

    public int getPrecio() {
        return precio;
    }

    public void setPrecio(int precio) {
        if (precio <= 0 || precio > 1_000_000) {
            throw new IllegalArgumentException("El precio debe estar entre $1 y $1.000.000.");
        }
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        if (stock < 0 || stock > 10_000) {
            throw new IllegalArgumentException("El stock debe estar entre 0 y 10.000.");
        }
        this.stock = stock;
    }

    @Override
    public String toString() {
        return codigo + " · " + nombre;
    }
}
