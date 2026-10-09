package cl.dsy1102.ejemplos.agenda.model;

/**
 * Contacto de la vida personal.
 */
public class ContactoPersonal extends Contacto {

    private String apodo;
    private boolean favorito;

    /** Para Jackson. */
    protected ContactoPersonal() {
    }

    public ContactoPersonal(String nombre, String telefono, String email, String apodo, boolean favorito) {
        super(nombre, telefono, email);
        setApodo(apodo);
        this.favorito = favorito;
    }

    @Override
    public String obtenerDetalle() {
        return "[Personal] " + super.obtenerDetalle()
                + (apodo == null ? "" : " | \"" + apodo + "\"") + (favorito ? " | ★" : "");
    }

    public String getApodo() {
        return apodo;
    }

    /** El apodo es opcional: vacio se guarda como null. */
    public void setApodo(String apodo) {
        this.apodo = apodo == null || apodo.isBlank() ? null : apodo.trim();
    }

    public boolean isFavorito() {
        return favorito;
    }

    public void setFavorito(boolean favorito) {
        this.favorito = favorito;
    }
}
