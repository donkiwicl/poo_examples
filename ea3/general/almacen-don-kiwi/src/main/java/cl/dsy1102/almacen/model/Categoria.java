package cl.dsy1102.almacen.model;

/**
 * Categorías del almacén. Se guardan por su nombre (name()): "LACTEOS".
 */
public enum Categoria {
    ABARROTES("Abarrotes"),
    LACTEOS("Lácteos"),
    BEBIDAS("Bebidas"),
    LIMPIEZA("Limpieza"),
    PANADERIA("Panadería");

    private final String nombre;

    Categoria(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
