package cl.dsy1102.veterinaria.model;

import java.time.LocalDate;

/**
 * Mascota exotica (conejo, huron o ave): requiere manejo especial, por lo que
 * cuesta 30 % mas. No es Vacunable: la clinica no vacuna exoticos.
 */
public class Exotico extends Paciente {

    public static final String[] ESPECIES = {"Conejo", "Hurón", "Ave"};
    public static final double RECARGO_MANEJO = 1.3;

    private String especie;

    public Exotico(String nombre, String tutor, String telefonoTutor, LocalDate fechaNacimiento, String especie) {
        super(nombre, tutor, telefonoTutor, fechaNacimiento);
        setEspecie(especie);
    }

    @Override
    public String obtenerEspecie() {
        return especie;
    }

    @Override
    protected double factorCosto() {
        return RECARGO_MANEJO;
    }

    @Override
    public Paciente copiar() {
        Exotico copia = new Exotico(getNombre(), getTutor(), getTelefonoTutor(), getFechaNacimiento(), especie);
        copiarEn(copia);
        return copia;
    }

    @Override
    public String obtenerDetalle() {
        return super.obtenerDetalle() + "\nExótico: manejo especial (+30 %)";
    }

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        for (String valida : ESPECIES) {
            if (valida.equals(especie)) {
                this.especie = especie;
                return;
            }
        }
        throw new IllegalArgumentException("Especie exótica inválida: " + especie);
    }
}
