package cl.dsy1102.ejemplos.liceo.model;

import java.time.LocalDate;
import java.time.Period;

/**
 * Un alumno tal como lo necesita la aplicación: con el nombre de su curso
 * (que en la base de datos está en otra tabla).
 */
public class Alumno {

    private final int id;
    private final String rut;
    private final String nombre;
    private final LocalDate fechaNacimiento;
    private final String correoApoderado; // puede ser null: el apoderado no informó correo
    private final String curso;

    public Alumno(int id, String rut, String nombre, LocalDate fechaNacimiento, String correoApoderado, String curso) {
        this.id = id;
        this.rut = rut;
        this.nombre = nombre;
        this.fechaNacimiento = fechaNacimiento;
        this.correoApoderado = correoApoderado;
        this.curso = curso;
    }

    public int getId() { return id; }
    public String getRut() { return rut; }
    public String getNombre() { return nombre; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public String getCorreoApoderado() { return correoApoderado; }
    public String getCurso() { return curso; }

    public boolean tieneCorreo() {
        return correoApoderado != null;
    }

    public int edadEn(LocalDate fecha) {
        return Period.between(fechaNacimiento, fecha).getYears();
    }

    @Override
    public String toString() {
        return String.format("%-4s %-16s %-13s %s", curso, nombre, rut,
                tieneCorreo() ? correoApoderado : "(sin correo)");
    }
}
