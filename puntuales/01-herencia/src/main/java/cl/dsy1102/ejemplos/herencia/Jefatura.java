package cl.dsy1102.ejemplos.herencia;

/**
 * Jefatura: es un Empleado con personas a cargo, por lo que recibe una
 * asignacion de responsabilidad.
 */
public class Jefatura extends Empleado {

    public static final double PORCENTAJE_ASIGNACION = 0.15;
    public static final int MONTO_POR_PERSONA = 10_000;
    public static final int MAXIMO_PERSONAS = 50;

    private int personasACargo;

    public Jefatura(String rut, String nombre, int sueldoBase, int personasACargo) {
        super(rut, nombre, sueldoBase);
        setPersonasACargo(personasACargo);
    }

    public int calcularAsignacion() {
        // getSueldoBase(): el atributo es privado en Empleado, se accede por su getter.
        return (int) Math.round(getSueldoBase() * PORCENTAJE_ASIGNACION) + personasACargo * MONTO_POR_PERSONA;
    }

    @Override
    public int calcularSueldo() {
        return super.calcularSueldo() + calcularAsignacion();
    }

    @Override
    public String obtenerDetalle() {
        return super.obtenerDetalle() + " | A cargo: " + personasACargo
                + " | Asignacion: " + pesos(calcularAsignacion());
    }

    public int getPersonasACargo() {
        return personasACargo;
    }

    public void setPersonasACargo(int personasACargo) {
        if (personasACargo < 1 || personasACargo > MAXIMO_PERSONAS) {
            throw new IllegalArgumentException("Las personas a cargo deben estar entre 1 y " + MAXIMO_PERSONAS + ".");
        }
        this.personasACargo = personasACargo;
    }
}
