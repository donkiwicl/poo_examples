package cl.dsy1102.ejemplos.excepciones;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

/**
 * Procesa el archivo movimientos.txt sobre la Cuenta Kiwi.
 * Ejecuta con: mvn compile exec:java
 * Prueba tambien con otro archivo: mvn compile exec:java -Dexec.args="no-existe.txt"
 */
public class Main {

    public static void main(String[] args) {
        Path archivo = Path.of(args.length > 0 ? args[0] : "movimientos.txt");
        CuentaBancaria cuenta = new CuentaBancaria("1234-5", 500_000);
        ProcesadorMovimientos procesador = new ProcesadorMovimientos(cuenta);

        try {
            for (String resultado : procesador.procesarArchivo(archivo)) {
                System.out.println(resultado);
            }
        } catch (NoSuchFileException e) {
            // Subclase de IOException: va primero para dar un mensaje mas preciso.
            System.out.println("No existe el archivo " + archivo.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("No se pudo leer " + archivo + ": " + e.getMessage());
        }

        System.out.println("Lineas procesadas: " + procesador.getLineasProcesadas()
                + " | Saldo final: " + CuentaBancaria.pesos(cuenta.getSaldo()));
    }
}
