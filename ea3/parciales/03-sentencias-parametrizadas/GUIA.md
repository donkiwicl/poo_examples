# Guía de resolución · EA3 03 Sentencias parametrizadas

> Para comparar tu trabajo con la solución: `git diff main solucion -- ea3/parciales/03-sentencias-parametrizadas`

## Idea central

```
Statement:          "... WHERE nombre LIKE '%" + texto + "%'"   → el texto del usuario se vuelve CÓDIGO SQL
PreparedStatement:  "... WHERE LOWER(nombre) LIKE LOWER(?)"     → el texto del usuario es siempre un DATO
                     ps.setString(1, "%" + texto + "%");
```

Con `PreparedStatement`, el SQL se envía y se analiza **antes** de conocer los valores. Los valores viajan después, por separado. No importa qué contengan: nunca pueden cambiar la estructura de la consulta.

## La vulnerabilidad (pregunta 1)

Con `texto = "' OR '1'='1"`, la versión insegura arma:

```sql
SELECT ... FROM socio WHERE nombre LIKE '%' OR '1'='1%' ORDER BY nombre
```

`nombre LIKE '%'` es verdadero para **todas** las filas: el usuario cambió la lógica del `WHERE`. Con un texto distinto podría leer otras tablas (`UNION SELECT ...`) o, si el driver permite varias sentencias, borrar datos. Con `O'Higgins` la comilla cierra el texto antes de tiempo y el SQL queda mal formado (`SQLSyntaxErrorException`).

**Pregunta 2:** escapar a mano (`texto.replace("'", "''")`) parece funcionar, pero hay que hacerlo en **cada** consulta y conocer todas las reglas del motor (barras invertidas, codificaciones, comentarios). Basta con olvidar un caso. Con `PreparedStatement`, la protección no depende de que el programador se acuerde.

## Paso 1 — Búsqueda segura (R1)

```java
String sql = "SELECT " + COLUMNAS + " FROM socio WHERE LOWER(nombre) LIKE LOWER(?) ORDER BY nombre";
ps.setString(1, "%" + texto + "%");
```

- Los `%` son parte del **valor**. `LIKE '%?%'` no funciona: dentro de comillas, `?` es un signo de interrogación y no un parámetro.
- `LOWER` en ambos lados: MySQL compara sin distinguir mayúsculas por su *collation* (`utf8mb4_0900_ai_ci`), pero H2, PostgreSQL y otros sí distinguen. Así el código se comporta igual en cualquier motor.
- Un `%` escrito por el usuario sigue siendo comodín (buscar `%` retorna todos). Es inofensivo, porque no cambia la estructura del SQL. Si importara, se escapa con `LIKE ? ESCAPE '!'`.

## Paso 2 — Insertar con clave generada (R2)

```java
try (Connection con = conexion.abrir();
     PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
    ps.setString(1, socio.getRut());
    ...
    ps.setObject(5, socio.getFechaIngreso());
    ps.setBoolean(6, socio.isActivo());
    ps.executeUpdate();
    try (ResultSet claves = ps.getGeneratedKeys()) {
        if (claves.next()) {
            socio.setId(claves.getInt(1));
        }
    }
} catch (SQLIntegrityConstraintViolationException e) {
    throw new SocioDuplicadoException("Ya existe un socio con el RUT " + socio.getRut() + ".", e);
}
```

- **Pregunta 4:** «contar y sumar 1» falla si se borró un socio (repite un `id`) o si dos usuarios insertan al mismo tiempo (ambos calculan el mismo). `AUTO_INCREMENT` es atómico: la base de datos garantiza que cada `id` es único.
- `getGeneratedKeys()` se lee por **posición** (`getInt(1)`). Es la excepción a la regla de leer por nombre: el nombre de esa columna depende del driver (`GENERATED_KEY` en MySQL, `ID` en H2).
- **El enum** se guarda con `name()` y se lee con `Categoria.valueOf(...)`. No se usa `ordinal()`: si alguien reordena el enum, cambiarían todas las categorías guardadas.
- **La excepción:** MySQL (`1062`, `SQLState 23000`) y H2 (`23505`) lanzan `SQLIntegrityConstraintViolationException`, una subclase estándar de JDBC. Capturarla sirve en los dos motores. En esta tabla, la única restricción que un `INSERT` puede violar es `UNIQUE(rut)`: los `NOT NULL` y `CHECK` ya los cubre el modelo. Ojo: MySQL informa un `CHECK` violado como `SQLException` genérica (código 3819), **no** como esta subclase.
- La prueba `insertarGuardaElTextoTalCual` guarda el nombre `Robert'); DROP TABLE socio;--` (el famoso [Bobby Tables](https://xkcd.com/327/)). Con parámetros queda guardado como texto y la tabla sigue existiendo.

## Paso 3 — Actualizar y eliminar (R3 y R4)

```java
String sql = "UPDATE socio SET nombre = ?, categoria = ?, cuota_mensual = ?, activo = ? WHERE id = ?";
...
ps.setInt(5, socio.getId());
return ps.executeUpdate() == 1;
```

- `executeUpdate()` retorna las **filas afectadas**. Un `UPDATE` o `DELETE` con un `id` inexistente **no es un error para SQL**: afecta 0 filas.
- **Pregunta 5:** el DAO informa el hecho (`false`) y no decide la política. En una pantalla, `false` puede significar «otro usuario lo eliminó mientras lo editabas» y mostrar un aviso. En otro contexto puede ser normal.
- Los parámetros se numeran por **posición en el SQL**, no por el orden de las columnas de la tabla: el `id` es el 5 porque es el quinto `?`.
- El `UPDATE` no modifica el `rut` ni `fecha_ingreso`: son datos que no deberían cambiar después de inscribirse.

## Paso 4 — Un solo `UPDATE` (R5)

```java
String sql = "UPDATE socio SET cuota_mensual = ROUND(cuota_mensual * CAST(? AS DECIMAL(6,3)))"
        + " WHERE categoria = ? AND activo = TRUE";
ps.setDouble(1, 1 + porcentaje / 100);
ps.setString(2, categoria.name());
return ps.executeUpdate();
```

- **Un solo `UPDATE`** en vez de `listarTodos()` + un `actualizar()` por socio: es un solo viaje a la base de datos y la operación es **atómica**. Si falla, no queda la mitad de los socios reajustados.
- **`CAST(? AS DECIMAL(6,3))`:** sin él, H2 deduce que el `?` es `INT` (el tipo de `cuota_mensual`) y **trunca** `1.1` a `1`. El `UPDATE` afecta 2 filas, pero no cambia nada. MySQL no tiene este problema, pero un `CAST` explícito deja el tipo claro en cualquier motor. Lo detecta la prueba `reajustarCuotasDeUnaCategoria`.
- `DECIMAL` hace exacto el cálculo: 9.000 × 1,055 = 9.495, sin errores de punto flotante.

## Paso 5 — Parámetros de distinto tipo (R6)

```java
ps.setString(1, categoria.name());
ps.setObject(2, desde);   // LocalDate → DATE
```

`setObject` con `LocalDate` (JDBC 4.2) envía una fecha de verdad, no un texto. No depende del formato (`dd-MM-yyyy` o `yyyy-MM-dd`) ni de la configuración regional del servidor.

## Pregunta 3 — Otras ventajas de `PreparedStatement`

| Ventaja | Por qué |
|---|---|
| **Seguridad** | Los valores nunca se interpretan como SQL. |
| **Tipos** | `setInt`, `setObject(LocalDate)`, `setBoolean`: no hay que formatear números con punto o coma ni fechas como texto. |
| **Legibilidad** | El SQL queda completo y legible, sin `"' + x + '"`. |
| **Rendimiento** | El motor puede reutilizar el plan de ejecución si la misma sentencia se ejecuta muchas veces con otros valores (por ejemplo, en lote). |

## Verificación

| Prueba | Resultado en la solución |
|---|---|
| `mvn test` (H2) | 14 pruebas en verde. |
| `mvn test -Dbd.url=jdbc:mysql://...` (MySQL 8.4) | 14 pruebas en verde. |
| `mvn compile exec:java` | La salida del README. |

## Errores frecuentes

| Síntoma | Causa |
|---|---|
| `Parameter index out of range (1 > number of parameters, which is 0)` | Se escribió `'%?%'` o `'?'` entre comillas: el `?` dejó de ser parámetro. |
| `No value specified for parameter 2` | Falta un `setX` para alguno de los `?`. |
| El `id` del socio sigue en 0 después de insertar | Falta `RETURN_GENERATED_KEYS` o leer `getGeneratedKeys()`. |
| `Can not issue data manipulation statements with executeQuery()` | `INSERT`/`UPDATE`/`DELETE` van con `executeUpdate()`. |
| La búsqueda `tapia` no encuentra nada en H2 pero sí en MySQL | Falta `LOWER`: H2 distingue mayúsculas. |
| El reajuste dice «2 socios» pero las cuotas no cambian | El `?` se interpretó como `INT` (ver paso 4). |
| Los nombres con tilde aparecen como `MatÃ­as` | Los datos se cargaron sin `SET NAMES utf8mb4` (o con un cliente en otra codificación). |
