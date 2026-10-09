package cl.dsy1102.ejemplos.herencia;

import java.util.Locale;

/**
 * Superclase de la planilla. Contiene lo que comparten todos los empleados:
 * identificacion, sueldo base y el calculo del sueldo sin extras.
 *
 * Esta clase ya esta resuelta. No la modifiques: Vendedor y Jefatura deben
 * reutilizarla mediante herencia.
 */
public class Empleado {

    /** Ingreso minimo mensual de referencia. */
    public static final int SUELDO_MINIMO = 539_000;

    private static final Locale CHILE = Locale.of("es", "CL");

    private final String rut;
    private String nombre;
    private int sueldoBase;

    public Empleado(String rut, String nombre, int sueldoBase) {
        if (rut == null || rut.isBlank()) {
            throw new IllegalArgumentException("El RUT es obligatorio.");
        }
        this.rut = rut.trim();
        setNombre(nombre);
        setSueldoBase(sueldoBase);
    }

    /** Sueldo del mes. Las subclases lo amplian con sus extras. */
    public int calcularSueldo() {
        return sueldoBase;
    }

    /** Ficha de una linea. Las subclases agregan sus datos al final. */
    public String obtenerDetalle() {
        return rut + " | " + nombre + " | Base: " + pesos(sueldoBase);
    }

    @Override
    public String toString() {
        return obtenerDetalle() + " | Sueldo: " + pesos(calcularSueldo());
    }

    /** Formatea un monto como $1.234.567, igual en cualquier equipo. */
    protected static String pesos(int monto) {
        return String.format(CHILE, "$%,d", monto);
    }

    public String getRut() {
        return rut;
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

    public int getSueldoBase() {
        return sueldoBase;
    }

    public void setSueldoBase(int sueldoBase) {
        if (sueldoBase < SUELDO_MINIMO) {
            throw new IllegalArgumentException("El sueldo base no puede ser menor que " + pesos(SUELDO_MINIMO) + ".");
        }
        this.sueldoBase = sueldoBase;
    }
}
