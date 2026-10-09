package cl.dsy1102.ejemplos.agenda.model;

/**
 * Contacto de la agenda. Es abstracta: siempre es personal o laboral.
 *
 * TODO R1: prepara la clase para Jackson.
 *  - Constructor sin parametros (protected) en esta clase y en cada subclase.
 *  - @JsonTypeInfo + @JsonSubTypes para que el JSON guarde "tipo": "PERSONAL" o "LABORAL".
 */
public abstract class Contacto {

    private String nombre;
    private String telefono;
    private String email;

    protected Contacto(String nombre, String telefono, String email) {
        setNombre(nombre);
        setTelefono(telefono);
        setEmail(email);
    }

    /** Linea para mostrar en consola. Cada subclase agrega sus datos. */
    public String obtenerDetalle() {
        return nombre + " | " + telefono + " | " + email;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        this.nombre = nombre.trim();
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        if (telefono == null || !telefono.matches("\\+?\\d{8,12}")) {
            throw new IllegalArgumentException("Telefono invalido: " + telefono);
        }
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new IllegalArgumentException("Email invalido: " + email);
        }
        this.email = email.trim().toLowerCase();
    }
}
