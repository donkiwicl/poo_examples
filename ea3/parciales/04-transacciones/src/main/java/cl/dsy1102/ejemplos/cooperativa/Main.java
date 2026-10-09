package cl.dsy1102.ejemplos.cooperativa;

import cl.dsy1102.ejemplos.cooperativa.dao.ConexionBD;
import cl.dsy1102.ejemplos.cooperativa.dao.CuentaDao;
import cl.dsy1102.ejemplos.cooperativa.dao.OperacionRechazadaException;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Demostración de transacciones. Ejecuta con: mvn compile exec:java
 * (reinicia antes la base con sql/tablas.sql y sql/datos.sql para ver la misma salida).
 */
public class Main {

    private static CuentaDao dao;

    public static void main(String[] args) throws SQLException {
        dao = new CuentaDao(ConexionBD.desdeRecurso("/db.properties"));
        System.out.println("=== Cooperativa de Ahorro Los Andes ===");
        saldos();

        System.out.println("\n-- 1. Sin transacción: 2001 transfiere $50.000 a la cuenta 9999 (no existe)");
        intentar(() -> dao.transferirSinTransaccion("2001", "9999", 50_000, "Arriendo"));
        saldos();

        System.out.println("\n-- 2. Con transacción: 2002 transfiere $30.000 a la cuenta 9999");
        intentar(() -> dao.transferir("2002", "9999", 30_000, "Arriendo"));
        saldos();

        System.out.println("\n-- 3. 2002 transfiere $30.000 a 2003");
        intentar(() -> dao.transferir("2002", "2003", 30_000, "Cuota del curso"));
        System.out.println("-- 4. 2003 transfiere $90.000 a 2001");
        intentar(() -> dao.transferir("2003", "2001", 90_000, "Préstamo"));
        saldos();

        System.out.println("\n-- 5. Remuneraciones con una cuenta inexistente (no se paga a nadie)");
        Map<String, Long> pagos = new LinkedHashMap<>();
        pagos.put("2001", 650_000L);
        pagos.put("2002", 580_000L);
        pagos.put("2999", 520_000L);
        intentar(() -> dao.pagarRemuneraciones("1001", pagos, "Sueldo octubre"));
        saldos();

        System.out.println("\n-- 6. Remuneraciones corregidas");
        pagos.remove("2999");
        pagos.put("2003", 520_000L);
        intentar(() -> System.out.printf("Total pagado: $%,d%n",
                dao.pagarRemuneraciones("1001", pagos, "Sueldo octubre")));
        saldos();

        System.out.println("\n-- Cartola de la cuenta 2002");
        dao.movimientos("2002").forEach(System.out::println);
    }

    private interface Operacion {
        void ejecutar() throws SQLException, OperacionRechazadaException;
    }

    private static void intentar(Operacion operacion) {
        try {
            operacion.ejecutar();
            System.out.println("OK");
        } catch (OperacionRechazadaException e) {
            System.out.println("Rechazada: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Error de base de datos: " + e.getMessage());
        }
    }

    private static void saldos() {
        StringBuilder linea = new StringBuilder("Saldos:");
        for (String numero : new String[]{"1001", "2001", "2002", "2003"}) {
            try {
                linea.append(String.format("  %s $%,d", numero, dao.saldo(numero)));
            } catch (SQLException | OperacionRechazadaException e) {
                linea.append("  ").append(numero).append(" ?");
            }
        }
        System.out.println(linea);
    }
}
