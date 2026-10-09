package cl.dsy1102.ejemplos.agenda.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Contacto de la agenda. Es abstracta: siempre es personal o laboral.
 *
 * Jackson no puede crear una clase abstracta: con estas anotaciones escribe un
 * atributo "tipo" en cada objeto del JSON y al leer lo usa para elegir la subclase.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "tipo")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ContactoPersonal.class, name = "PERSONAL"),
        @JsonSubTypes.Type(value = ContactoLaboral.class, name = "LABORAL")
})
public abstract class Contacto {

    private String nombre;
    private String telefono;
    private String email;

    /**
     * Para Jackson: crea el objeto vacio y luego llama a los setters, de modo que
     * las validaciones del modelo tambien se aplican al leer el archivo.
     */
    protected Contacto() {
    }

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
