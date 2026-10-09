package cl.dsy1102.ejemplos.imagenes;

/**
 * Parque nacional del catalogo. Ya esta resuelto.
 */
public class Parque {

    private final String nombre;
    private final String region;
    private final String descripcion;
    private final String archivoImagen;

    /**
     * @param archivoImagen nombre del archivo dentro de la carpeta img de los recursos
     */
    public Parque(String nombre, String region, String descripcion, String archivoImagen) {
        this.nombre = nombre;
        this.region = region;
        this.descripcion = descripcion;
        this.archivoImagen = archivoImagen;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRegion() {
        return region;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getArchivoImagen() {
        return archivoImagen;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
