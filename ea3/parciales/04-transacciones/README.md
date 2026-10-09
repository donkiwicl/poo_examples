# EA3 · 04 · Transacciones — Cooperativa de Ahorro Los Andes

**Tema:** *autocommit*, `setAutoCommit(false)`, `commit()` y `rollback()`, atomicidad (todo o nada), una transacción = **una** `Connection`, `UPDATE` condicional (`WHERE saldo >= ?`) y filas afectadas, operaciones en lote (`addBatch`/`executeBatch`), excepciones de negocio frente a `SQLException`.
**Tipo:** consola + JUnit (H2) · **Tiempo estimado:** 1 a 2 bloques · **Sesión:** 3.1 (PPT 3.1.2, «Connection y transacciones»)

> La guía de resolución está en la rama `solucion`, en el archivo `GUIA.md` de esta carpeta.

## Problema

La **Cooperativa de Ahorro Los Andes** recibió un reclamo: una socia transfirió $50.000 a una cuenta mal escrita. El programa le mostró un error, pero el dinero **salió de su cuenta y no llegó a ninguna parte**.

El método `transferirSinTransaccion` ejecuta el cargo y el abono como dos sentencias independientes. JDBC trabaja por defecto en modo *autocommit*: cada sentencia se confirma apenas termina. Cuando el abono falla, el cargo ya quedó guardado.

Hay que reescribir la transferencia como una **transacción** y agregar el pago masivo de remuneraciones de una empresa socia, que debe ser **todo o nada**.

```
cuenta (numero PK, titular, saldo CHECK >= 0)
  └─< movimiento (id, numero_cuenta → cuenta, fecha_hora, tipo CARGO/ABONO, monto, glosa)
```

## Código inicial

| Archivo | Estado |
|---|---|
| `sql/*.sql`, `dao/ConexionBD`, `model/Movimiento`, `dao/OperacionRechazadaException` | Resueltos. |
| `dao/CuentaDao.java` | `saldo()`, `movimientos()` y `transferirSinTransaccion()` (con el error) resueltos. **`transferir()` y `pagarRemuneraciones()` por completar.** Las sentencias SQL ya están escritas como constantes. |
| `Main.java` | Resuelto. Muestra el error de la versión original y luego la versión con transacción. |
| `src/test/.../CuentaDaoTest.java` | 12 pruebas con H2. Una demuestra el error original y pasa desde el inicio. |

## Requerimientos

**R1. Validaciones previas.** En `transferir`, un monto menor o igual a cero, o el mismo número de origen y destino, lanzan `IllegalArgumentException` **sin abrir** una conexión.

**R2. La transacción.** Con **una sola** `Connection` y `setAutoCommit(false)`:
1. cargo al origen con `SQL_CARGO`, que descuenta solo si el saldo alcanza;
2. abono al destino con `SQL_ABONO`;
3. dos movimientos con `SQL_MOVIMIENTO` (`CARGO` en el origen y `ABONO` en el destino) con la misma fecha y hora;
4. `commit()`.

**R3. Rechazos sin cambios.** Si el cargo o el abono no afectan filas, lanza `OperacionRechazadaException`:
- `"La cuenta 9999 no existe."`, para el origen o el destino;
- `"Saldo insuficiente en la cuenta 2003."`, si la cuenta existe pero no alcanza.

Ante **cualquier** excepción, de negocio o `SQLException`, haz `rollback()` y relánzala. Después de un rechazo, los saldos y los movimientos deben quedar exactamente como estaban.

**R4. Remuneraciones en lote.** `pagarRemuneraciones(cuentaEmpresa, pagos, glosa)`, todo o nada:
- `pagos` relaciona cada número de cuenta con su monto. Si está vacío o tiene montos menores o iguales a cero: `IllegalArgumentException`.
- **Un solo cargo** a la empresa por el total (si no alcanza: `"Saldo insuficiente en la cuenta 1001."`).
- Los abonos se envían en **lote** (`addBatch`/`executeBatch`). Si alguno no afectó filas: `"La cuenta 2999 no existe."` y no se paga a nadie.
- Movimientos: un `CARGO` a la empresa con glosa `"Remuneraciones (3 pagos)"` y un `ABONO` por trabajador con la glosa recibida.
- Retorna el total pagado.

## Resultado esperado (`mvn compile exec:java`)

```
Saldos:  1001 $2.500.000  2001 $150.000  2002 $80.000  2003 $0

-- 1. Sin transacción: 2001 transfiere $50.000 a la cuenta 9999 (no existe)
Rechazada: La cuenta de destino 9999 no existe.
Saldos:  1001 $2.500.000  2001 $100.000  2002 $80.000  2003 $0      ← ¡se perdieron $50.000!

-- 2. Con transacción: 2002 transfiere $30.000 a la cuenta 9999
Rechazada: La cuenta 9999 no existe.
Saldos:  1001 $2.500.000  2001 $100.000  2002 $80.000  2003 $0      ← 2002 conserva su saldo
...
-- 5. Remuneraciones con una cuenta inexistente (no se paga a nadie)
Rechazada: La cuenta 2999 no existe.
...
-- 6. Remuneraciones corregidas
Total pagado: $1.750.000
OK
Saldos:  1001 $750.000  2001 $750.000  2002 $630.000  2003 $550.000
```

## Cómo ejecutar

```bash
mvn test                 # objetivo: 12 pruebas en verde (H2)
mvn compile exec:java    # MySQL: ejecuta antes crear-bd.sql, tablas.sql y datos.sql
```

## Preguntas para pensar

1. ¿Por qué todas las sentencias de una transacción deben usar la **misma** `Connection`? ¿Qué pasa si `transferir` llama a dos métodos que abren su propia conexión?
2. `SQL_CARGO` dice `WHERE numero = ? AND saldo >= ?`. ¿Por qué es mejor que consultar el saldo con un `SELECT`, compararlo en Java y luego hacer el `UPDATE`? Piensa en dos cajeros que operan la misma cuenta al mismo tiempo.
3. La tabla tiene `CHECK (saldo >= 0)`. Si ya está esa restricción, ¿para qué sirve la condición del `UPDATE`?
4. ¿Qué ventaja tiene enviar los abonos en lote frente a ejecutarlos uno por uno? ¿El lote por sí solo garantiza el «todo o nada»?
5. ¿Por qué el `rollback()` también se hace ante una `RuntimeException`?
