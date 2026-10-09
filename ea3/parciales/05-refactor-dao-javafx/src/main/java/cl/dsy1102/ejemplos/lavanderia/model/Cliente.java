package cl.dsy1102.ejemplos.lavanderia.model;

/**
 * Un cliente de la lavandería. El id es 0 mientras no se ha guardado.
 */
public class Cliente {

    private int id;
    private String nombre;
    private String telefono;
    private String correo;
    private String comuna;

    public Cliente() {
    }

    public Cliente(int id, String nombre, String telefono, String correo, String comuna) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.correo = correo;
        this.comuna = comuna;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getComuna() { return comuna; }
    public void setComuna(String comuna) { this.comuna = comuna; }

    @Override
    public String toString() {
        return nombre + " (" + correo + ")";
    }
}
