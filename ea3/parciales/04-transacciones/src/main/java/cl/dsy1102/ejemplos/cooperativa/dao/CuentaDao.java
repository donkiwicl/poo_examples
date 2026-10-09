package cl.dsy1102.ejemplos.cooperativa.dao;

import cl.dsy1102.ejemplos.cooperativa.model.Movimiento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Operaciones sobre las cuentas de la cooperativa.
 */
public class CuentaDao {

    private static final String SQL_CARGO = "UPDATE cuenta SET saldo = saldo - ? WHERE numero = ? AND saldo >= ?";
    private static final String SQL_ABONO = "UPDATE cuenta SET saldo = saldo + ? WHERE numero = ?";
    private static final String SQL_MOVIMIENTO =
            "INSERT INTO movimiento (numero_cuenta, fecha_hora, tipo, monto, glosa) VALUES (?, ?, ?, ?, ?)";

    private final ConexionBD conexion;

    public CuentaDao(ConexionBD conexion) {
        this.conexion = conexion;
    }

    /** Resuelto: saldo actual de la cuenta. */
    public long saldo(String numero) throws SQLException, OperacionRechazadaException {
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement("SELECT saldo FROM cuenta WHERE numero = ?")) {
            ps.setString(1, numero);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new OperacionRechazadaException("La cuenta " + numero + " no existe.");
                }
                return rs.getLong("saldo");
            }
        }
    }

    /** Resuelto: movimientos de la cuenta, del más antiguo al más nuevo. */
    public List<Movimiento> movimientos(String numero) throws SQLException {
        String sql = "SELECT fecha_hora, tipo, monto, glosa FROM movimiento WHERE numero_cuenta = ? ORDER BY id";
        try (Connection con = conexion.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, numero);
            try (ResultSet rs = ps.executeQuery()) {
                List<Movimiento> movimientos = new ArrayList<>();
                while (rs.next()) {
                    movimientos.add(new Movimiento(rs.getObject("fecha_hora", LocalDateTime.class),
                            rs.getString("tipo"), rs.getLong("monto"), rs.getString("glosa")));
                }
                return movimientos;
            }
        }
    }

    /**
     * Versión ORIGINAL, SIN transacción. Se deja para comparar: cada sentencia
     * se confirma apenas se ejecuta (autocommit). Si la cuenta de destino no
     * existe, el dinero ya salió del origen y no llega a ninguna parte.
     */
    public void transferirSinTransaccion(String origen, String destino, long monto, String glosa)
            throws SQLException, OperacionRechazadaException {
        try (Connection con = conexion.abrir();
             PreparedStatement cargo = con.prepareStatement(SQL_CARGO);
             PreparedStatement abono = con.prepareStatement(SQL_ABONO)) {
            cargo.setLong(1, monto);
            cargo.setString(2, origen);
            cargo.setLong(3, monto);
            if (cargo.executeUpdate() == 0) {
                throw new OperacionRechazadaException("Saldo insuficiente o cuenta de origen inexistente.");
            }
            abono.setLong(1, monto);
            abono.setString(2, destino);
            if (abono.executeUpdate() == 0) {
                throw new OperacionRechazadaException("La cuenta de destino " + destino + " no existe.");
            }
        }
    }

    /**
     * TODO R1 a R3: transfiere un monto entre dos cuentas como UNA transacción.
     *
     * Validaciones previas (sin tocar la base de datos):
     *  - monto mayor que cero, si no IllegalArgumentException;
     *  - origen distinto del destino, si no IllegalArgumentException.
     *
     * En la transacción (una sola Connection con setAutoCommit(false)):
     *  1. Cargo al origen con SQL_CARGO (descuenta solo si alcanza el saldo).
     *     Si no afectó filas: OperacionRechazadaException
     *     "La cuenta <n> no existe." o "Saldo insuficiente en la cuenta <n>."
     *  2. Abono al destino con SQL_ABONO. Si no afectó filas:
     *     OperacionRechazadaException("La cuenta <n> no existe.")
     *  3. Registra dos movimientos con SQL_MOVIMIENTO (CARGO en el origen y
     *     ABONO en el destino), con la misma fecha y hora y la glosa recibida.
     *  4. commit().
     * Ante cualquier excepción (de negocio o SQLException): rollback() y relanzarla.
     */
    public void transferir(String origen, String destino, long monto, String glosa)
            throws SQLException, OperacionRechazadaException {
        throw new UnsupportedOperationException("TODO R1 a R3");
    }

    /**
     * TODO R4: paga las remuneraciones desde la cuenta de la empresa, TODO O NADA.
     *  - pagos: número de cuenta → monto. Vacío o con montos <= 0: IllegalArgumentException.
     *  - Un solo cargo a la empresa por el total (si no alcanza:
     *    OperacionRechazadaException("Saldo insuficiente en la cuenta <n>.")).
     *  - Los abonos y sus movimientos se envían en LOTE (addBatch/executeBatch).
     *    Si algún abono no afectó filas (cuenta inexistente):
     *    OperacionRechazadaException("La cuenta <n> no existe.").
     *  - Movimientos: un CARGO a la empresa con glosa "Remuneraciones (<cantidad> pagos)"
     *    y un ABONO por trabajador con la glosa recibida.
     *  - Retorna el total pagado.
     */
    public long pagarRemuneraciones(String cuentaEmpresa, Map<String, Long> pagos, String glosa)
            throws SQLException, OperacionRechazadaException {
        throw new UnsupportedOperationException("TODO R4");
    }
}
