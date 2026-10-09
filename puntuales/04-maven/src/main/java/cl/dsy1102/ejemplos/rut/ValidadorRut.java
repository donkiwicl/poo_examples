package cl.dsy1102.ejemplos.rut;

/**
 * Valida y formatea RUT chilenos con el algoritmo modulo 11.
 *
 * Esta clase ya esta resuelta: en este caso el trabajo esta en el pom.xml.
 */
public final class ValidadorRut {

    private ValidadorRut() {
    }

    /** true si el RUT tiene formato valido y su digito verificador es correcto. */
    public static boolean esValido(String rut) {
        String limpio = limpiar(rut);
        if (!limpio.matches("\\d{1,8}[0-9K]")) {
            return false;
        }
        int cuerpo = Integer.parseInt(limpio.substring(0, limpio.length() - 1));
        return calcularDigitoVerificador(cuerpo) == limpio.charAt(limpio.length() - 1);
    }

    /**
     * Digito verificador: se multiplican los digitos de derecha a izquierda por
     * 2, 3, 4, 5, 6, 7, 2, 3...; se suman y se calcula 11 - (suma % 11).
     * 11 equivale a '0' y 10 a 'K'.
     */
    public static char calcularDigitoVerificador(int cuerpo) {
        if (cuerpo <= 0) {
            throw new IllegalArgumentException("El cuerpo del RUT debe ser positivo");
        }
        int suma = 0;
        int factor = 2;
        for (int resto = cuerpo; resto > 0; resto /= 10) {
            suma += (resto % 10) * factor;
            factor = factor == 7 ? 2 : factor + 1;
        }
        int dv = 11 - (suma % 11);
        return switch (dv) {
            case 11 -> '0';
            case 10 -> 'K';
            default -> (char) ('0' + dv);
        };
    }

    /** "123456785" o "12345678-5" -> "12.345.678-5". */
    public static String formatear(String rut) {
        if (!esValido(rut)) {
            throw new IllegalArgumentException("RUT invalido: " + rut);
        }
        String limpio = limpiar(rut);
        String cuerpo = limpio.substring(0, limpio.length() - 1);
        StringBuilder conPuntos = new StringBuilder();
        for (int i = 0; i < cuerpo.length(); i++) {
            if (i > 0 && (cuerpo.length() - i) % 3 == 0) {
                conPuntos.append('.');
            }
            conPuntos.append(cuerpo.charAt(i));
        }
        return conPuntos + "-" + limpio.charAt(limpio.length() - 1);
    }

    private static String limpiar(String rut) {
        return rut == null ? "" : rut.replace(".", "").replace("-", "").trim().toUpperCase();
    }
}
