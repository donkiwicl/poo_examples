package cl.dsy1102.ejemplos.excepciones;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Aplica a una cuenta los movimientos escritos en un archivo de texto.
 * Cada linea es un comando: DEPOSITAR monto, GIRAR monto o SALDO.
 */
public class ProcesadorMovimientos {

    private final CuentaBancaria cuenta;
    private int lineasProcesadas;

    public ProcesadorMovimientos(CuentaBancaria cuenta) {
        this.cuenta = cuenta;
    }

    /**
     * Procesa un comando y retorna el resultado. NUNCA lanza excepciones:
     * una linea mala no debe detener el resto del archivo.
     */
    public String procesar(String linea) {
        String limpia = linea.trim();
        try {
            ejecutar(limpia);
            return "OK | " + limpia + " | Saldo: " + CuentaBancaria.pesos(cuenta.getSaldo());
        } catch (OperacionRechazadaException e) {
            // Captura SaldoInsuficienteException y LimiteDiarioExcedidoException (subclases).
            return "RECHAZADO | " + limpia + " | " + e.getMessage();
        } catch (NumberFormatException e) {
            // Debe ir ANTES que IllegalArgumentException, porque es su subclase.
            return "ERROR | " + limpia + " | '" + limpia.split("\\s+")[1] + "' no es un monto valido";
        } catch (IllegalArgumentException e) {
            return "ERROR | " + limpia + " | " + e.getMessage();
        } finally {
            // Se ejecuta en todos los casos, incluso despues de cada return.
            lineasProcesadas++;
        }
    }

    private void ejecutar(String linea) throws OperacionRechazadaException {
        String[] partes = linea.split("\\s+");
        String comando = partes[0].toUpperCase();
        switch (comando) {
            case "SALDO" -> {
                // No modifica la cuenta, solo informa el saldo.
            }
            case "DEPOSITAR" -> cuenta.depositar(leerMonto(partes));
            case "GIRAR" -> cuenta.girar(leerMonto(partes));
            default -> throw new IllegalArgumentException("Comando desconocido: " + comando);
        }
    }

    private static int leerMonto(String[] partes) {
        if (partes.length < 2) {
            throw new IllegalArgumentException("Falta el monto");
        }
        return Integer.parseInt(partes[1]);   // puede lanzar NumberFormatException
    }

    /**
     * Lee el archivo y procesa cada linea que no este vacia ni empiece con #.
     *
     * @throws IOException si el archivo no existe o no se puede leer; Main
     *                     decide que mostrar al usuario
     */
    public List<String> procesarArchivo(Path archivo) throws IOException {
        List<String> resultados = new ArrayList<>();
        // try-with-resources: el lector se cierra solo, aunque ocurra una excepcion.
        try (BufferedReader lector = Files.newBufferedReader(archivo)) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                if (!linea.isBlank() && !linea.startsWith("#")) {
                    resultados.add(procesar(linea));
                }
            }
        }
        return resultados;
    }

    public int getLineasProcesadas() {
        return lineasProcesadas;
    }
}
