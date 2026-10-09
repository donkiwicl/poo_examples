package cl.dsy1102.ejemplos.polimorfismo;

/**
 * Jornada de ventas del Almacen Don Kiwi.
 * Ejecuta con: mvn compile exec:java
 */
public class Main {

    public static void main(String[] args) {
        Caja caja = new Caja();

        // Cada argumento es de una clase distinta, pero cobrar() recibe un MedioPago.
        System.out.println(caja.cobrar(12_345, new Efectivo("Ana Rojas")));
        System.out.println(caja.cobrar(8_990, new TarjetaDebito("Bruno Diaz")));
        System.out.println(caja.cobrar(60_000, new TarjetaCredito("Carla Soto", 3)));
        System.out.println(caja.cobrar(4_996, new Efectivo("Diego Pinto")));
        System.out.println(caja.cobrar(25_000, new Transferencia("Elena Mora")));

        System.out.println("Total recaudado: " + Formato.pesos(caja.getTotalRecaudado()));
        System.out.println("Puntos otorgados: " + caja.getPuntosOtorgados());
    }
}
