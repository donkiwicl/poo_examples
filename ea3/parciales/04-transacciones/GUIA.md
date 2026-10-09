# Guía de resolución · EA3 04 Transacciones

> Para comparar tu trabajo con la solución: `git diff main solucion -- ea3/parciales/04-transacciones`

## Idea central

```
autocommit (por defecto)        transacción
UPDATE cargo   → confirmado      setAutoCommit(false)
UPDATE abono   ✗ falla           UPDATE cargo   (pendiente)
                                 UPDATE abono   ✗ falla
el cargo ya no se puede          rollback()  → el cargo se deshace
deshacer                         (si todo sale bien: commit() confirma los 4 cambios juntos)
```

Una transacción agrupa varias sentencias en una unidad **atómica**: o se confirman todas o no se confirma ninguna. Mientras no hay `commit`, los cambios son provisionales y los demás usuarios no los ven.

## Paso 1 — Validaciones antes de abrir la conexión (R1)

```java
if (monto <= 0) throw new IllegalArgumentException("El monto debe ser mayor que cero.");
if (origen.equals(destino)) throw new IllegalArgumentException("...deben ser distintas.");
```

Lo que se puede decidir sin la base de datos se decide antes: no se abre una conexión para rechazar un monto negativo. Además, una transferencia a la misma cuenta pasaría todas las reglas y dejaría dos movimientos sin sentido.

## Paso 2 — La transacción (R2 y R3)

```java
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
        con.rollback();
        throw e;
    }
}
```

- **Pregunta 1:** la transacción vive **en la conexión**. Si el cargo y el abono se ejecutan en conexiones distintas, cada una tiene su propia transacción y el `rollback` de una no deshace la otra. Por eso los métodos auxiliares (`cargar`, `existe`) **reciben** la conexión en vez de abrir otra.
- **Dos `try` anidados:** el externo cierra la conexión, y el interno permite hacer `rollback` con la conexión todavía abierta. Si el `rollback` estuviera en el `catch` del `try` externo, la conexión ya estaría cerrada.
- **Pregunta 5:** un `NullPointerException` (una glosa nula, por ejemplo) también deja la transacción a medias. Por eso se captura `RuntimeException`. `throw e` relanza la excepción original: gracias al *precise rethrow* de Java, el método solo declara `SQLException` y `OperacionRechazadaException`.
- **¿Y si no se hace `rollback`?** Al cerrar una conexión con una transacción abierta, MySQL la deshace, pero el comportamiento depende del driver y con un *pool* la conexión no se cierra de verdad. El `rollback` explícito no deja nada al azar.
- **Con un *pool*** conviene restaurar `setAutoCommit(true)` en un `finally` antes de devolver la conexión. Aquí cada operación abre y cierra su propia conexión, así que no es necesario.
- `withNano(0)`: `DATETIME` guarda segundos. Sin esto, la hora en Java y la de la base de datos podrían diferir en los decimales.

### El `UPDATE` condicional (pregunta 2)

```sql
UPDATE cuenta SET saldo = saldo - ? WHERE numero = ? AND saldo >= ?
```

Con «`SELECT` saldo → comparar en Java → `UPDATE`», dos cajeros pueden leer el mismo saldo de $100.000, aprobar cada uno un retiro de $80.000 y dejar la cuenta en −$60.000. El `UPDATE` condicional **verifica y modifica en una sola operación atómica**: el segundo cajero afecta 0 filas.

Si afecta 0 filas, puede ser porque la cuenta no existe o porque no alcanza el saldo. `existe(con, numero)` distingue los dos casos **usando la misma conexión**.

**Pregunta 3:** el `CHECK (saldo >= 0)` es la última defensa: si un error de programación intenta dejar el saldo negativo, la base de datos lo rechaza. Pero lo informa como un error técnico (en MySQL, `SQLException` con código 3819). La condición del `UPDATE` permite dar un mensaje de negocio claro («Saldo insuficiente...»).

## Paso 3 — Remuneraciones en lote (R4)

```java
cargar(con, cargo, cuentaEmpresa, total);

for (Map.Entry<String, Long> pago : lista) {
    abono.setLong(1, pago.getValue());
    abono.setString(2, pago.getKey());
    abono.addBatch();
}
int[] filas = abono.executeBatch();
for (int i = 0; i < filas.length; i++) {
    if (filas[i] == 0) throw new OperacionRechazadaException("La cuenta " + lista.get(i).getKey() + " no existe.");
}
```

- **Primero el cargo por el total:** si la empresa no tiene fondos, se rechaza sin procesar ningún abono.
- `executeBatch()` retorna un arreglo con las filas afectadas por **cada** sentencia, en el orden en que se agregaron. Por eso se copia el mapa a una `List`: así hay un orden fijo para recorrerlo y para leer los resultados.
- **Pregunta 4:** el lote reduce los viajes de red (con 300 trabajadores, 1 envío en vez de 300). Pero **no** es una transacción: sin `setAutoCommit(false)`, cada abono del lote se confirmaría por separado. El «todo o nada» lo da la transacción. El lote solo aporta velocidad.
- Con la propiedad `rewriteBatchedStatements=true` de Connector/J (más rápida), el driver puede retornar `Statement.SUCCESS_NO_INFO` (−2) en vez del número de filas. En ese caso habría que verificar las cuentas antes con un `SELECT`. Este caso usa la configuración por defecto.

## Verificación

| Prueba | Resultado en la solución |
|---|---|
| `mvn test` (H2) | 12 pruebas en verde. Con el código inicial pasa solo la demostración. |
| `mvn test -Dbd.url=jdbc:mysql://...` (MySQL 8.4, InnoDB) | 12 pruebas en verde. |
| `mvn compile exec:java` | La salida del README: el escenario 1 pierde $50.000 y el 2 no. |

## Errores frecuentes

| Síntoma | Causa |
|---|---|
| Después de un rechazo, el origen quedó descontado | Falta `setAutoCommit(false)`, o el `rollback` no se ejecuta en ese camino (por ejemplo, solo se captura `SQLException` y no la excepción de negocio). |
| Los cambios no se guardan aunque no hay errores | Falta `con.commit()`. |
| `rollback` lanza «connection is closed» | El `rollback` está fuera del `try` donde la conexión sigue abierta. |
| El `rollback` no deshace el cargo | El cargo se ejecutó con otra `Connection` (un método auxiliar abrió la suya). |
| En MySQL el `rollback` no tiene efecto | La tabla se creó con `ENGINE=MyISAM`, que no soporta transacciones. Usa InnoDB, el motor por defecto. |
| `executeBatch` retorna −2 | `rewriteBatchedStatements=true` en la URL (ver paso 3). |
