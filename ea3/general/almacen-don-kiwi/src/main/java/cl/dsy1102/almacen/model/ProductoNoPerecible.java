package cl.dsy1102.almacen.model;

/**
 * Producto sin vencimiento (abarrotes secos, limpieza...). Registra la marca.
 */
public class ProductoNoPerecible extends Producto {

    private String marca;

    protected ProductoNoPerecible() {
    }

    public ProductoNoPerecible(String codigo, String nombre, Categoria categoria, int precio, int stock, String marca) {
        super(codigo, nombre, categoria, precio, stock);
        setMarca(marca);
    }

    @Override
    public String obtenerTipo() {
        return "No perecible";
    }

    @Override
    public String obtenerDetalle() {
        return super.obtenerDetalle() + " · marca " + marca;
    }

    @Override
    public Producto copiar() {
        ProductoNoPerecible copia = new ProductoNoPerecible();
        copiarDatosEn(copia);
        copia.marca = marca;
        return copia;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        if (marca == null || marca.isBlank() || marca.trim().length() > 40) {
            throw new IllegalArgumentException("La marca es obligatoria y tiene hasta 40 caracteres.");
        }
        this.marca = marca.trim();
    }
}
