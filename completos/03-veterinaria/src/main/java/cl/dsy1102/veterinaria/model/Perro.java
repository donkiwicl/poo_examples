package cl.dsy1102.veterinaria.model;

import java.time.LocalDate;

/**
 * Perro: el costo depende de su tamano. Vacuna cada 21 dias como minimo.
 */
public class Perro extends Paciente implements Vacunable {

    private String raza;
    private Tamano tamano;

    public Perro(String nombre, String tutor, String telefonoTutor, LocalDate fechaNacimiento, String raza, Tamano tamano) {
        super(nombre, tutor, telefonoTutor, fechaNacimiento);
        setRaza(raza);
        setTamano(tamano);
    }

    @Override
    public String obtenerEspecie() {
        return "Perro";
    }

    @Override
    protected double factorCosto() {
        return tamano.getFactor();
    }

    @Override
    public int getDiasEntreVacunas() {
        return 21;
    }

    @Override
    public Paciente copiar() {
        Perro copia = new Perro(getNombre(), getTutor(), getTelefonoTutor(), getFechaNacimiento(), raza, tamano);
        copiarEn(copia);
        return copia;
    }

    @Override
    public String obtenerDetalle() {
        return super.obtenerDetalle() + "\nRaza: " + raza + " · Tamaño: " + tamano;
    }

    public String getRaza() {
        return raza;
    }

    /** Vacia se registra como "Mestizo". */
    public void setRaza(String raza) {
        this.raza = raza == null || raza.isBlank() ? "Mestizo" : raza.trim();
    }

    public Tamano getTamano() {
        return tamano;
    }

    public void setTamano(Tamano tamano) {
        if (tamano == null) {
            throw new IllegalArgumentException("El tamaño del perro es obligatorio.");
        }
        this.tamano = tamano;
    }
}
