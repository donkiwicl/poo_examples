package cl.dsy1102.arriendo;

import cl.dsy1102.arriendo.model.ArriendoRechazadoException;
import cl.dsy1102.arriendo.model.Bicicleta;
import cl.dsy1102.arriendo.model.Recargable;
import cl.dsy1102.arriendo.model.ScooterElectrico;
import cl.dsy1102.arriendo.model.Vehiculo;

import java.util.List;

/**
 * Demostracion del modelo por consola (ya resuelto).
 * Ejecuta con: mvn compile exec:java
 */
public class Main {

    public static void main(String[] args) {
        Bicicleta bk01 = new Bicicleta("BK-01", "Oxford Urbana", 2_500, "Urbana", true);
        Bicicleta bk02 = new Bicicleta("BK-02", "Trek Marlin", 3_500, "Montaña", false);
        ScooterElectrico sc01 = new ScooterElectrico("SC-01", "Xiaomi 4 Pro", 3_000, 80, 45);
        ScooterElectrico sc02 = new ScooterElectrico("SC-02", "Segway E2", 2_800, 15, 25);

        arrendar(bk01, 2);
        arrendar(bk01, 1);
        arrendar(bk02, 5);
        arrendar(sc01, 3);
        arrendar(sc02, 1);
        arrendar(sc02, 9);

        System.out.println();
        for (Vehiculo vehiculo : List.of(bk01, bk02, sc01, sc02)) {
            String bateria = vehiculo instanceof Recargable r ? " | Bateria: " + r.getBateria() + "%" : "";
            System.out.println(vehiculo.getCodigo() + " | " + (vehiculo.isDisponible() ? "Disponible" : "Arrendado") + bateria);
        }
    }

    private static void arrendar(Vehiculo vehiculo, int horas) {
        try {
            System.out.println(vehiculo.arrendar(horas));
        } catch (ArriendoRechazadoException e) {
            System.out.println("Arriendo rechazado: " + e.getMessage());
        }
    }
}
