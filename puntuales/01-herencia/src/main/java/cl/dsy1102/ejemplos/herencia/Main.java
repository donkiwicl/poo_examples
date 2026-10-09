package cl.dsy1102.ejemplos.herencia;

/**
 * Planilla de remuneraciones de la Pyme "Ferreteria El Tornillo".
 * Ejecuta con: mvn compile exec:java
 */
public class Main {

    public static void main(String[] args) {
        Empleado[] planilla = {
                new Empleado("11.111.111-1", "Ana Rojas", 650_000),
                // TODO R1: descomenta cuando exista la clase Vendedor.
                // new Vendedor("22.222.222-2", "Bruno Diaz", 560_000, 4_000_000),
                // TODO R2: descomenta cuando exista la clase Jefatura.
                // new Jefatura("33.333.333-3", "Carla Soto", 1_200_000, 6),
                // TODO R4 (desafio): descomenta cuando exista la clase JefeVentas.
                // new JefeVentas("44.444.444-4", "Diego Pinto", 900_000, 12_000_000, 10_000_000),
        };

        int total = 0;
        for (Empleado empleado : planilla) {
            System.out.println(empleado);
            total += empleado.calcularSueldo();
        }
        System.out.println("Total planilla: " + Empleado.pesos(total));
    }
}
