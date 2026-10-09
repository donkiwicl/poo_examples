package cl.dsy1102.ejemplos.club.model;

import java.time.LocalDate;

/**
 * Socio del club. El id es 0 mientras el socio no se ha guardado: lo asigna
 * la base de datos (AUTO_INCREMENT) al insertarlo.
 */
public class Socio {

    private int id;
    private String rut;
    private String nombre;
    private Categoria categoria;
    private int cuotaMensual;
    private LocalDate fechaIngreso;
    private boolean activo;

    public Socio(String rut, String nombre, Categoria categoria, int cuotaMensual, LocalDate fechaIngreso) {
        this(0, rut, nombre, categoria, cuotaMensual, fechaIngreso, true);
    }

    public Socio(int id, String rut, String nombre, Categoria categoria, int cuotaMensual,
                 LocalDate fechaIngreso, boolean activo) {
        this.id = id;
        this.rut = rut;
        setNombre(nombre);
        this.categoria = categoria;
        setCuotaMensual(cuotaMensual);
        this.fechaIngreso = fechaIngreso;
        this.activo = activo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getRut() { return rut; }
    public String getNombre() { return nombre; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public int getCuotaMensual() { return cuotaMensual; }
    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank() || nombre.length() > 60) {
            throw new IllegalArgumentException("El nombre es obligatorio y tiene hasta 60 caracteres.");
        }
        this.nombre = nombre.trim();
    }

    public void setCuotaMensual(int cuotaMensual) {
        if (cuotaMensual <= 0) {
            throw new IllegalArgumentException("La cuota mensual debe ser mayor que cero.");
        }
        this.cuotaMensual = cuotaMensual;
    }

    @Override
    public String toString() {
        return String.format("#%-2d %-28s %-12s %-8s $%,6d %s%s", id, nombre, rut, categoria, cuotaMensual,
                fechaIngreso, activo ? "" : " (inactivo)");
    }
}
