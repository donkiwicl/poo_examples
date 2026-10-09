package cl.dsy1102.ejemplos.agenda.model;

/**
 * Contacto de trabajo.
 */
public class ContactoLaboral extends Contacto {

    private String empresa;
    private String cargo;

    /** Para Jackson. */
    protected ContactoLaboral() {
    }

    public ContactoLaboral(String nombre, String telefono, String email, String empresa, String cargo) {
        super(nombre, telefono, email);
        setEmpresa(empresa);
        setCargo(cargo);
    }

    @Override
    public String obtenerDetalle() {
        return "[Laboral]  " + super.obtenerDetalle() + " | " + cargo + " en " + empresa;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        if (empresa == null || empresa.isBlank()) {
            throw new IllegalArgumentException("La empresa es obligatoria.");
        }
        this.empresa = empresa.trim();
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        if (cargo == null || cargo.isBlank()) {
            throw new IllegalArgumentException("El cargo es obligatorio.");
        }
        this.cargo = cargo.trim();
    }
}
