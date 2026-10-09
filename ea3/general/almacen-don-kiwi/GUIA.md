# Guía de resolución · EA3 Caso general · Almacén Don Kiwi

Esta guía acompaña la rama `solucion`. Los temas puntuales (conexión, `ResultSet`, parámetros, transacciones y refactorización) se explican en detalle en los casos parciales 01 a 05. Aquí se ve cómo se **integran** y cuáles son las decisiones propias de este caso.

> Comparar tu avance: `git diff main solucion -- ea3/general/almacen-don-kiwi`

---

## 0. Mapa de requisitos

| Requisito | Dónde se cumple |
|---|---|
| R1 Proyecto y conexión | `pom.xml`, `module-info.java`, `db.properties`, `dao/ConexionBD` |
| R2 Tablas | `sql/tablas.sql` |
| R3 Migrar productos | `dao/JdbcProductoDao`, una línea de `AppFX` |
| R4 No se cae | `dao/ErroresSql`, `AppFX`, botón **Recargar** (ya existía) |
| R5 Ventas | `dao/JdbcVentaDao.registrar`, `repository/VentaRepository`, `VentaController`, `venta-view.fxml` |
| R6 Reportes | `JdbcVentaDao` (3 consultas), `ReportesController`, `reportes-view.fxml` |
| R7 Migración | `MigrarDatos` |

```
git diff main solucion --stat -- ea3/general/almacen-don-kiwi/src/main/java
  AppFX.java                       ← cambia el DAO y agrega VentaRepository
  controller/PrincipalController   ← solo los botones Nueva venta y Reportes (R5/R6)
  controller/VentaController       ← nuevo
  controller/ReportesController    ← nuevo
  dao/ConexionBD, ErroresSql, JdbcProductoDao, JdbcVentaDao   ← nuevos
  repository/VentaRepository       ← nuevo
  MigrarDatos                      ← nuevo
  (FormularioController, ProductoRepository y el modelo: SIN cambios)
```

## Paso 1 — Proyecto (R1)

| Dependencia | Scope | Por qué |
|---|---|---|
| `mysql-connector-j` | `runtime` | El código usa solo `java.sql`. El driver se necesita al ejecutar. |
| `h2` | `test` | Base de datos en memoria para las pruebas. No viaja con la aplicación. |
| `junit-jupiter` | `test` | Pruebas. |

En `module-info.java` basta con `requires java.sql;`. El driver **no** se declara: `DriverManager` lo encuentra con `ServiceLoader`, y el sistema de módulos lo incluye solo porque el JAR declara que *provee* `java.sql.Driver`. Se comprobó con `mvn javafx:run` (modo modular).

## Paso 2 — Diseño de las tablas (R2)

```sql
CREATE TABLE producto (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    codigo            VARCHAR(6)  NOT NULL UNIQUE,
    nombre            VARCHAR(60) NOT NULL,
    tipo              VARCHAR(12) NOT NULL CHECK (tipo IN ('PERECIBLE', 'NO_PERECIBLE')),
    categoria         VARCHAR(12) NOT NULL,
    precio            INT         NOT NULL CHECK (precio > 0),
    stock             INT         NOT NULL CHECK (stock >= 0),
    fecha_vencimiento DATE        NULL,
    marca             VARCHAR(40) NULL
);
CREATE TABLE venta (... fecha_hora DATETIME NOT NULL, total INT NOT NULL CHECK (total > 0));
CREATE TABLE detalle_venta (
    ...
    FOREIGN KEY (venta_id) REFERENCES venta (id),
    FOREIGN KEY (producto_id) REFERENCES producto (id)
);
```

### Herencia en una sola tabla

El modelo tiene una clase abstracta con dos subclases. Hay tres formas de llevarlo a tablas:

| Estrategia | Cómo | Ventaja | Desventaja |
|---|---|---|---|
| **Una tabla para la jerarquía** (la solución) | `producto` con columna `tipo` y columnas propias que admiten `NULL` | Consultas simples, sin `JOIN`. `detalle_venta` apunta a una sola tabla. | Columnas en `NULL`. La BD no obliga a que un perecible tenga fecha (lo obliga el modelo). |
| Una tabla por subclase concreta | `producto_perecible` y `producto_no_perecible` | Sin `NULL`. | `detalle_venta` necesitaría dos FK, o ninguna. Los reportes necesitan `UNION`. |
| Tabla base + tablas hijas | `producto` + `perecible(producto_id, fecha)` + `no_perecible(producto_id, marca)` | Normalizada. | Un `JOIN` por consulta y dos `INSERT` (en transacción) por producto. |

Con dos subclases que difieren en **una columna** cada una, una sola tabla es lo más simple. Si cada subclase tuviera diez atributos propios, convendría la tercera opción.

### Otras decisiones

- **Orden de los `DROP`:** primero `detalle_venta` (tiene las FK), después `venta` y `producto`. Al revés, MySQL rechaza borrar una tabla referenciada por otra.
- **`categoria` como texto** con el `name()` del enum: legible en la BD y estable aunque se reordene el enum. Una tabla `categoria` con FK también sería válida si las categorías se administraran desde la aplicación.
- **`precio_unitario` en `detalle_venta`:** el precio cobrado se guarda en la línea. Si mañana sube el arroz, las ventas pasadas no cambian, y los reportes suman lo que realmente se cobró (con descuentos).
- **`total` en `venta`** es redundante (se puede calcular desde el detalle), pero se guarda como el total de la boleta emitida. Es una decisión consciente, no un descuido.
- **Los `CHECK`** son la última defensa: el modelo valida primero y da mejores mensajes. En MySQL 8.0.16+ los `CHECK` sí se aplican; en versiones antiguas se ignoraban.

## Paso 3 — `JdbcProductoDao` (R3)

```java
private Producto mapear(ResultSet rs) throws SQLException {
    ...
    Producto producto = switch (tipo) {
        case PERECIBLE -> new ProductoPerecible(codigo, nombre, categoria, precio, stock,
                rs.getObject("fecha_vencimiento", LocalDate.class));
        case NO_PERECIBLE -> new ProductoNoPerecible(codigo, nombre, categoria, precio, stock,
                rs.getString("marca"));
        default -> throw new SQLException("Tipo de producto desconocido en la base de datos: " + tipo);
    };
    producto.setId(rs.getInt("id"));
    return producto;
}

private void asignarDatos(PreparedStatement ps, Producto producto) throws SQLException {
    ...
    if (producto instanceof ProductoPerecible perecible) {
        ps.setObject(7, perecible.getFechaVencimiento());
        ps.setNull(8, Types.VARCHAR);
    } else if (producto instanceof ProductoNoPerecible noPerecible) {
        ps.setNull(7, Types.DATE);
        ps.setString(8, noPerecible.getMarca());
    }
}
```

- La columna `tipo` cumple la misma función que el atributo `"tipo"` de `@JsonTypeInfo` en el JSON: indica qué subclase crear.
- Los objetos se crean con los **constructores del modelo**, que validan. Si alguien edita la BD a mano y deja un precio en 0, la carga falla con un mensaje claro en vez de mostrar datos inválidos.
- `setNull(i, Types.X)` en vez de `setObject(i, null)`: algunos drivers necesitan saber el tipo SQL del `NULL`.
- **Traducción de errores según la operación:** en `insertar`/`actualizar`, una `SQLIntegrityConstraintViolationException` es el `UNIQUE(codigo)`. En `eliminar` es la **clave foránea** de `detalle_venta`: el producto tiene ventas. El DAO sabe qué operación hizo, así que da el mensaje correcto sin interpretar códigos de error propios de cada motor.
- **La FK protege la historia:** sin ella, eliminar el arroz dejaría ventas apuntando a un producto inexistente, y los reportes con `JOIN` dejarían de contarlas en silencio.

### El contrato se cumple: los controladores no cambian

`ProductoRepository` llama a `dao.insertar(p)` y luego `productos.add(p)`. Con JSON, el `id` lo asignaba `max + 1`, y con MySQL `AUTO_INCREMENT`. El repositorio no se entera. **Esa es la prueba de que el diseño por capas funcionó:** la migración tocó una línea de `AppFX`.

```java
// Antes (EA2)
Repository<Producto> productos = new ProductoRepository(new JsonProductoDao(ARCHIVO_DATOS));
// Después (EA3)
Repository<Producto> productos = new ProductoRepository(new JdbcProductoDao(conexion));
```

## Paso 4 — Errores de base de datos (R4)

`ErroresSql.traducir` es compartido por los dos DAO: un `SQLState` que empieza con `08` (conexión) da el mensaje «No hay conexión con la base de datos...», y cualquier otro error incluye la acción que falló. Los controladores ya capturaban `PersistenciaException` desde EA2. **No fue necesario agregar ni un `catch`**: el contrato de errores también se respetó.

`AppFX` muestra la ventana **antes** de cargar. Si MySQL está detenido, el `Alert` aparece sobre la aplicación abierta, y **Recargar** (que existía desde EA2) vuelve a intentar.

## Paso 5 — Ventas en una transacción (R5)

```java
public void registrar(Venta venta) throws PersistenciaException {
    try (Connection con = conexion.abrir()) {
        con.setAutoCommit(false);
        try {
            int id = insertarVenta(con, venta);
            descontarStockYGuardarDetalle(con, id, venta);
            con.commit();
            venta.setId(id);
        } catch (SQLException | PersistenciaException | RuntimeException e) {
            con.rollback();
            throw e;
        }
    } catch (SQLException e) {
        throw ErroresSql.traducir("registrar la venta", e);
    }
}
```

- **Todo o nada:** la venta, los descuentos de stock y las líneas son una unidad. Si la tercera línea no tiene stock, se deshacen la venta y los descuentos de las dos primeras (prueba `siOtraVentaSeLlevoElStockNoSeGuardaNada`).
- **¿Por qué validar el stock de nuevo, si `Venta.agregar` ya lo hizo?** Porque hay **dos cajas**. Entre que se arma el carro y se confirma, la otra caja puede vender. El modelo valida contra el stock que se cargó al abrir la vista. La base de datos valida contra el stock **real**, en el mismo `UPDATE` que descuenta (`WHERE stock >= ?`).
- `venta.setId(id)` va **después** del `commit`: si se asignara antes y el `commit` fallara, la venta tendría el id de una fila que no existe.
- **Los dos `catch`:** el interno hace `rollback` con la conexión abierta y relanza. El externo traduce las `SQLException` (también las de abrir la conexión o del propio `rollback`). La `PersistenciaException` de stock pasa sin traducir.
- Las líneas se insertan en **lote** (`addBatch`/`executeBatch`): un viaje a la BD para todas.

### `VentaRepository`: volver a leer

```java
public void registrar(Venta venta) throws PersistenciaException {
    try {
        dao.registrar(venta);
    } finally {
        recargarProductos();
    }
}
```

Después de una venta, el stock en memoria quedó viejo. Se vuelve a leer desde la BD, que es la fuente de verdad, **también si la venta falló**: si otra caja se llevó el stock, el cajero debe ver cuánto queda realmente. El `ComboBox` de la caja es una `FilteredList` sobre la lista del repositorio, así que se actualiza solo.

Si la recarga falla después de una venta exitosa, **no** se informa como error de la venta, porque la venta sí quedó registrada. Decirle al cajero «error» haría que la cobrara dos veces.

### La vista

- `venta.agregar(...)` (modelo de EA1) concentra las reglas: cantidad, stock, vencidos, descuento y sumar al mismo producto. El controlador solo muestra su mensaje o su excepción.
- `lineas.setAll(venta.getLineas())`: `getLineas()` es de solo lectura, así que la tabla usa una `ObservableList` propia que se sincroniza después de cada cambio.

## Paso 6 — Reportes con SQL (R6)

```sql
SELECT p.categoria, SUM(d.cantidad) AS unidades, SUM(d.cantidad * d.precio_unitario) AS total
FROM detalle_venta d
JOIN venta v ON v.id = d.venta_id
JOIN producto p ON p.id = d.producto_id
WHERE v.fecha_hora >= ? AND v.fecha_hora < ?
GROUP BY p.categoria
ORDER BY total DESC
```

- **El período semiabierto:** `>= desde 00:00` y `< (hasta + 1 día) 00:00`. Con `BETWEEN desde AND hasta`, el día «hasta» se cortaría a las 00:00:00 y se perderían todas sus ventas. La prueba `elPeriodoIncluyeElDiaCompleto` lo verifica.
- **`LIMIT ?`** también acepta parámetro en MySQL y H2.
- **`GROUP BY p.id, p.codigo, p.nombre`** en «más vendidos»: se agrupa por `id` (dos productos podrían llamarse igual) y se listan también las columnas que se muestran. MySQL aceptaría agrupar solo por `p.id`, porque detecta que `codigo` y `nombre` dependen de la clave primaria, pero otros motores, como SQL Server u Oracle, exigen listarlas.
- **Los records no tienen `getX()`:** `PropertyValueFactory("total")` busca `getTotal()` y no lo encuentra. Por eso las columnas de `ReportesController` usan *lambdas*: `c -> new ReadOnlyObjectWrapper<>(c.getValue().total())`.

## Paso 7 — Migración (R7)

```java
ProductoDao origen = new JsonProductoDao(archivo);
ProductoDao destino = new JdbcProductoDao(ConexionBD.desdeRecurso("/db.properties"));
for (Producto producto : origen.listarTodos()) {
    producto.setId(0);
    try { destino.insertar(producto); ... } catch (PersistenciaException e) { ... omitido ... }
}
```

- **Polimorfismo en acción:** el mismo tipo (`ProductoDao`) con dos implementaciones. `MigrarDatos` no sabe nada de JSON ni de SQL.
- `setId(0)`: el `id` del JSON no significa nada en MySQL, que asigna uno nuevo.
- Cada producto se inserta por separado y los duplicados se **omiten**. Así el programa se puede ejecutar dos veces sin duplicar nada. La alternativa «todo o nada» (una transacción para toda la migración) también es válida, pero necesitaría un método nuevo en el DAO.
- Al migrar `datos-ejemplo/productos.json` sobre la base recién cargada, los ids nuevos son 14, 15 y 16, no 13. El `INSERT` fallido de AB-001 **consumió** el 13: InnoDB no reutiliza valores de `AUTO_INCREMENT`. Por eso nunca se debe suponer que los `id` son consecutivos.

## Verificación

| Prueba | Resultado en la solución |
|---|---|
| `mvn test` (H2) | 17 pruebas en verde (10 de productos y 7 de ventas). |
| `mvn test -Dbd.url=jdbc:mysql://...` (MySQL 8.4) | 17 pruebas en verde. |
| Prueba 5 (caja) | Yogur a $273, queso rechazado (`vencido desde el ...`), `Stock insuficiente de Aceite maravilla 1 L: quedan 4.`, `Boleta N° 4 por $12.525.`, el aceite queda con stock 0. |
| Prueba 6 (reportes) | `3 ventas · $28.350`. Abarrotes con 7 unidades y $8.430. |
| Prueba 4 (eliminar el arroz) | `No se puede eliminar el producto: tiene ventas registradas. Si ya no se vende, deja su stock en 0.` |
| Prueba 7 (sin servidor) | `Alert` con «No hay conexión...» y `0 de 0 productos`. Reportes: `Alert` y `Sin datos`. |
| Prueba 8 (migración) | `3 producto(s) migrado(s), 1 omitido(s).` |
| `mvn javafx:run` (modular) | Arranca sin errores: el driver se carga como servicio. |

## Errores frecuentes

| Síntoma | Causa |
|---|---|
| `datos.sql` falla con `Unknown column` | `tablas.sql` no usa exactamente los nombres de columna del diseño. |
| `Cannot drop table 'venta' referenced by a foreign key constraint` al ejecutar `tablas.sql` dos veces | Los `DROP TABLE` están en el orden equivocado: primero las tablas hijas. |
| Todos los productos se cargan como no perecibles | `mapear` no lee la columna `tipo`, o se guarda `obtenerTipo()` («No perecible», con espacio) en vez de `NO_PERECIBLE`. |
| La venta falla pero el stock bajó igual | Falta `setAutoCommit(false)`, o cada operación abre su propia `Connection`. |
| Después de vender, la tabla principal muestra el stock anterior | Falta recargar los productos después de registrar. |
| El reporte de «hoy» sale vacío aunque se vendió hoy | `BETWEEN ? AND ?` con fechas: se corta a las 00:00 del día «hasta». |
| `PropertyValueFactory` deja columnas vacías en los reportes | Los records se leen con `categoria()`, no con `getCategoria()`. |
| Las ventas de `datos.sql` aparecen con otra hora | `datos.sql` usa el reloj del servidor MySQL y la aplicación el del PC. Son iguales en una instalación local, pero no en un contenedor en UTC. |
| `No suitable driver` solo con `javafx:run` | Falta `requires java.sql`, o el driver quedó con `scope test`. |
