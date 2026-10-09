package cl.dsy1102.ejemplos.herencia;

/**
 * Vendedor: es un Empleado que ademas gana una comision por sus ventas del mes.
 */
public class Vendedor extends Empleado {

    public static final double PORCENTAJE_COMISION = 0.03;

    private int ventasMes;

    public Vendedor(String rut, String nombre, int sueldoBase, int ventasMes) {
        // super(...) debe ser la primera instruccion: inicializa la parte "Empleado".
        super(rut, nombre, sueldoBase);
        setVentasMes(ventasMes);
    }

    public int calcularComision() {
        return (int) Math.round(ventasMes * PORCENTAJE_COMISION);
    }

    @Override
    public int calcularSueldo() {
        // Reutiliza el calculo de la superclase y le suma lo propio.
        return super.calcularSueldo() + calcularComision();
    }

    @Override
    public String obtenerDetalle() {
        return super.obtenerDetalle() + " | Comision: " + pesos(calcularComision());
    }

    public int getVentasMes() {
        return ventasMes;
    }

    public void setVentasMes(int ventasMes) {
        if (ventasMes < 0) {
            throw new IllegalArgumentException("Las ventas del mes no pueden ser negativas.");
        }
        this.ventasMes = ventasMes;
    }
}
