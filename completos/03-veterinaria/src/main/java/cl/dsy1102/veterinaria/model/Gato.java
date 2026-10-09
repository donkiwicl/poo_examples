package cl.dsy1102.veterinaria.model;

import java.time.LocalDate;

/**
 * Gato: precio base. Vacuna cada 28 dias como minimo.
 */
public class Gato extends Paciente implements Vacunable {

    private boolean interior;

    public Gato(String nombre, String tutor, String telefonoTutor, LocalDate fechaNacimiento, boolean interior) {
        super(nombre, tutor, telefonoTutor, fechaNacimiento);
        this.interior = interior;
    }

    @Override
    public String obtenerEspecie() {
        return "Gato";
    }

    @Override
    protected double factorCosto() {
        return 1.0;
    }

    @Override
    public int getDiasEntreVacunas() {
        return 28;
    }

    @Override
    public Paciente copiar() {
        Gato copia = new Gato(getNombre(), getTutor(), getTelefonoTutor(), getFechaNacimiento(), interior);
        copiarEn(copia);
        return copia;
    }

    @Override
    public String obtenerDetalle() {
        return super.obtenerDetalle() + "\n" + (interior ? "Gato de interior" : "Sale al exterior");
    }

    public boolean isInterior() {
        return interior;
    }

    public void setInterior(boolean interior) {
        this.interior = interior;
    }
}
