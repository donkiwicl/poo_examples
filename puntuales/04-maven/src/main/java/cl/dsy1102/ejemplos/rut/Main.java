package cl.dsy1102.ejemplos.rut;

/**
 * Valida los RUT recibidos como argumentos.
 *
 *   mvn compile exec:java -Dexec.args="12.345.678-5 11111111-2"
 *   java -jar target/rut-1.0.0.jar 12.345.678-5
 */
public class Main {

    public static void main(String[] args) {
        String[] ruts = args.length > 0 ? args : new String[]{"12.345.678-5", "123456785", "11.111.111-2", "10.000.013-k", "abc"};
        for (String rut : ruts) {
            if (ValidadorRut.esValido(rut)) {
                System.out.println("valido   " + ValidadorRut.formatear(rut));
            } else {
                System.out.println("invalido " + rut);
            }
        }
    }
}
