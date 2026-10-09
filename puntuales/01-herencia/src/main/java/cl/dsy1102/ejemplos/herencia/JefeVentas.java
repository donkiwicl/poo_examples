package cl.dsy1102.ejemplos.herencia;

/**
 * Desafio R4. Herencia de varios niveles: Empleado -> Vendedor -> JefeVentas.
 *
 * Un jefe de ventas cobra todo lo de un vendedor y, si cumple la meta del mes,
 * un bono fijo.
 */
public class JefeVentas extends Vendedor {

    public static final int BONO_META = 150_000;

    private final int metaMes;

    public JefeVentas(String rut, String nombre, int sueldoBase, int ventasMes, int metaMes) {
        super(rut, nombre, sueldoBase, ventasMes);
        if (metaMes <= 0) {
            throw new IllegalArgumentException("La meta del mes debe ser mayor que 0.");
        }
        this.metaMes = metaMes;
    }

    public boolean cumpleMeta() {
        return getVentasMes() >= metaMes;
    }

    @Override
    public int calcularSueldo() {
        // super.calcularSueldo() es el de Vendedor, que a su vez llama al de Empleado.
        return super.calcularSueldo() + (cumpleMeta() ? BONO_META : 0);
    }

    @Override
    public String obtenerDetalle() {
        return super.obtenerDetalle() + " | Meta: " + (cumpleMeta() ? "cumplida" : "no cumplida");
    }

    public int getMetaMes() {
        return metaMes;
    }
}
