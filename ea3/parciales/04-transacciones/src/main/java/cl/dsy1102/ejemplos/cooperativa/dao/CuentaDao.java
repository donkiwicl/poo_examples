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
     * Transfiere un monto entre dos cuentas como UNA transacción: o se hacen
     * los cuatro cambios (cargo, abono y dos movimientos), o ninguno.
     */
    public void transferir(String origen, String destino, long monto, String glosa)
            throws SQLException, OperacionRechazadaException {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero.");
        }
        if (origen.equals(destino)) {
            throw new IllegalArgumentException("La cuenta de origen y la de destino deben ser distintas.");
        }
        // Todas las sentencias de una transacción usan la MISMA conexión.
        try (Connection con = conexion.abrir()) {
            con.setAutoCommit(false);
            try (PreparedStatement cargo = con.prepareStatement(SQL_CARGO);
                 PreparedStatement abono = con.prepareStatement(SQL_ABONO);
                 PreparedStatement movimiento = con.prepareStatement(SQL_MOVIMIENTO)) {
                cargar(con, cargo, origen, monto);
                abonar(abono, destino, monto);

                LocalDateTime ahora = LocalDateTime.now().withNano(0);
                registrar(movimiento, origen, ahora, Movimiento.CARGO, monto, glosa);
                movimiento.executeUpdate();
                registrar(movimiento, destino, ahora, Movimiento.ABONO, monto, glosa);
                movimiento.executeUpdate();

                con.commit();
            } catch (SQLException | OperacionRechazadaException | RuntimeException e) {
                con.rollback(); // deshace todo lo ejecutado desde setAutoCommit(false)
                throw e;
            }
        }
    }

    /**
     * Paga las remuneraciones desde la cuenta de la empresa, todo o nada.
     * Retorna el total pagado.
     */
    public long pagarRemuneraciones(String cuentaEmpresa, Map<String, Long> pagos, String glosa)
            throws SQLException, OperacionRechazadaException {
        if (pagos.isEmpty()) {
            throw new IllegalArgumentException("No hay pagos que realizar.");
        }
        // Se fija un orden para recorrer los pagos y para leer los resultados del lote.
        List<Map.Entry<String, Long>> lista = new ArrayList<>(pagos.entrySet());
        long total = 0;
        for (Map.Entry<String, Long> pago : lista) {
            if (pago.getValue() <= 0) {
                throw new IllegalArgumentException("El pago a la cuenta " + pago.getKey() + " debe ser mayor que cero.");
            }
            total += pago.getValue();
        }

        try (Connection con = conexion.abrir()) {
            con.setAutoCommit(false);
            try (PreparedStatement cargo = con.prepareStatement(SQL_CARGO);
                 PreparedStatement abono = con.prepareStatement(SQL_ABONO);
                 PreparedStatement movimiento = con.prepareStatement(SQL_MOVIMIENTO)) {
                cargar(con, cargo, cuentaEmpresa, total);

                // Lote: los abonos se acumulan y viajan juntos al servidor.
                for (Map.Entry<String, Long> pago : lista) {
                    abono.setLong(1, pago.getValue());
                    abono.setString(2, pago.getKey());
                    abono.addBatch();
                }
                int[] filas = abono.executeBatch(); // filas afectadas por cada sentencia, en orden
                for (int i = 0; i < filas.length; i++) {
                    if (filas[i] == 0) {
                        throw new OperacionRechazadaException("La cuenta " + lista.get(i).getKey() + " no existe.");
                    }
                }

                LocalDateTime ahora = LocalDateTime.now().withNano(0);
                registrar(movimiento, cuentaEmpresa, ahora, Movimiento.CARGO, total,
                        "Remuneraciones (" + lista.size() + " pagos)");
                movimiento.addBatch();
                for (Map.Entry<String, Long> pago : lista) {
                    registrar(movimiento, pago.getKey(), ahora, Movimiento.ABONO, pago.getValue(), glosa);
                    movimiento.addBatch();
                }
                movimiento.executeBatch();

                con.commit();
                return total;
            } catch (SQLException | OperacionRechazadaException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /** Descuenta el monto solo si alcanza el saldo; si no, explica por qué. */
    private void cargar(Connection con, PreparedStatement cargo, String numero, long monto)
            throws SQLException, OperacionRechazadaException {
        cargo.setLong(1, monto);
        cargo.setString(2, numero);
        cargo.setLong(3, monto);
        if (cargo.executeUpdate() == 0) {
            throw new OperacionRechazadaException(existe(con, numero)
                    ? "Saldo insuficiente en la cuenta " + numero + "."
                    : "La cuenta " + numero + " no existe.");
        }
    }

    private void abonar(PreparedStatement abono, String numero, long monto)
            throws SQLException, OperacionRechazadaException {
        abono.setLong(1, monto);
        abono.setString(2, numero);
        if (abono.executeUpdate() == 0) {
            throw new OperacionRechazadaException("La cuenta " + numero + " no existe.");
        }
    }

    private void registrar(PreparedStatement movimiento, String numero, LocalDateTime fechaHora,
                           String tipo, long monto, String glosa) throws SQLException {
        movimiento.setString(1, numero);
        movimiento.setObject(2, fechaHora);
        movimiento.setString(3, tipo);
        movimiento.setLong(4, monto);
        movimiento.setString(5, glosa);
    }

    /** Usa la conexión de la transacción en curso. */
    private boolean existe(Connection con, String numero) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM cuenta WHERE numero = ?")) {
            ps.setString(1, numero);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
