# Guía de resolución · EA3 02 Consultas y ResultSet

> Para comparar tu trabajo con la solución: `git diff main solucion -- ea3/parciales/02-consultas-resultset`

## Idea central

```
SQL (tablas, filas)  ──executeQuery──►  ResultSet (cursor)  ──mapearAlumno──►  List<Alumno> (objetos)
```

- Un `ResultSet` es un **cursor**: empieza *antes* de la primera fila y cada `next()` avanza una. Retorna `false` cuando no quedan filas.
- **Mapear** es copiar la fila actual a un objeto del dominio. Se hace **en el DAO** (pregunta 5): el resto de la aplicación trabaja con `Alumno`, sin saber que existe una tabla.

## Paso 1 — `listarAlumnos` y `mapearAlumno` (R1)

```java
private static final String SELECT_ALUMNO =
        "SELECT a.id, a.rut, a.nombre, a.fecha_nacimiento, a.correo_apoderado, c.nombre AS curso"
        + " FROM alumno a JOIN curso c ON c.id = a.curso_id";

private Alumno mapearAlumno(ResultSet rs) throws SQLException {
    return new Alumno(
            rs.getInt("id"),
            rs.getString("rut"),
            rs.getString("nombre"),
            rs.getObject("fecha_nacimiento", LocalDate.class),
            rs.getString("correo_apoderado"),
            rs.getString("curso"));
}
```

- **Alias:** sin `AS curso`, el resultado tiene dos columnas `nombre` y `rs.getString("nombre")` retorna la **primera**. Con el alias, cada columna tiene un nombre único.
- **Columnas explícitas, no `SELECT *`:** el DAO declara qué necesita. Si mañana se agrega una columna `foto BLOB` a la tabla, la consulta no la trae.
- **Pregunta 1:** con `rs.getString(3)`, agregar una columna o cambiar el orden del `SELECT` hace que el código lea otro dato **sin dar error**. Por nombre, el cambio no afecta o falla con un mensaje claro (`Column 'x' not found`).
- `getObject(columna, LocalDate.class)` (JDBC 4.2) convierte el `DATE` directamente. La forma antigua, `rs.getDate(...).toLocalDate()`, lanza `NullPointerException` si la fecha es `NULL`.
- `getString` y `getObject` retornan `null` si la columna es `NULL`. Los tipos primitivos (`getInt`, `getDouble`) **no pueden** retornar `null`: retornan 0 (ver paso 4).
- `mapearAlumno` **no** llama a `next()`: solo lee la fila actual. Quien recorre decide cuántas filas leer (`while` en R1 y R3, `if` en R2).

## Paso 2 — `buscarPorRut` (R2)

```java
try (Connection con = conexion.abrir();
     PreparedStatement ps = con.prepareStatement(SELECT_ALUMNO + " WHERE a.rut = ?")) {
    ps.setString(1, rut);
    try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? Optional.of(mapearAlumno(rs)) : Optional.empty();
    }
}
```

- El `ResultSet` se crea **después** de asignar los parámetros, por eso va en un `try` interno.
- `Optional` hace explícito que el alumno puede no existir. Retornar `null` obliga a quien llama a recordar revisarlo.
- La prueba `buscarPorRutNoSeDejaEnganar` envía `x' OR '1'='1`. Con `?` es un RUT que no existe. Concatenado, retornaría a todos los alumnos (el caso 03 profundiza en esto).

## Paso 3 — `listarPorCurso` (R3)

Igual que R2 con `WHERE c.nombre = ? ORDER BY a.nombre` y un `while`. Un curso inexistente no es un error: es una consulta sin filas, y retorna una lista vacía.

## Paso 4 — Promedios con `LEFT JOIN` y `NULL` (R4)

```sql
SELECT a.nombre, c.nombre AS curso, ROUND(AVG(n.valor), 1) AS promedio, COUNT(n.id) AS cantidad
FROM alumno a
JOIN curso c ON c.id = a.curso_id
LEFT JOIN nota n ON n.alumno_id = a.id
WHERE c.nombre = ?
GROUP BY a.id, a.nombre, c.nombre
ORDER BY promedio DESC, a.nombre
```

- **`LEFT JOIN`** conserva los alumnos sin notas: sus columnas de `nota` vienen en `NULL`. Con `JOIN`, Agustín desaparece del reporte (pregunta 3).
- **`COUNT(n.id)`** y no `COUNT(*)`: `COUNT(*)` cuenta filas, y Agustín tiene **una** fila (con nulos), así que mostraría 1 nota. `COUNT(columna)` ignora los `NULL`.
- `AVG` de un grupo sin valores retorna `NULL`. Al ordenar `DESC`, MySQL y H2 dejan los `NULL` al final.
- **`GROUP BY a.id, ...`:** se agrupa por `id`, no solo por nombre, para no mezclar a dos alumnos que se llamen igual.

**La trampa de `getDouble`:**

```java
double valor = rs.getDouble("promedio");      // 0.0 si es NULL
Double promedio = rs.wasNull() ? null : valor;
```

`wasNull()` responde por la **última columna leída**, así que se consulta justo después de `getDouble`. Sin esto, Agustín aparece con promedio 0,0, como si hubiera sacado puros unos. La prueba `alumnoSinNotasTienePromedioNuloYVaAlFinal` lo detecta. Una alternativa es `rs.getObject("promedio")`, que retorna `null`, pero su tipo (`BigDecimal`) depende del motor.

## Paso 5 — `HAVING` (R5)

```sql
... JOIN nota n ON n.alumno_id = a.id
GROUP BY a.id, a.nombre, c.nombre
HAVING AVG(n.valor) < ?
ORDER BY promedio, a.nombre
```

- **Pregunta 2:** `WHERE` filtra **filas antes** de agrupar, y `HAVING` filtra **grupos después** de agrupar. `WHERE AVG(valor) < ?` es un error de sintaxis, porque cuando se evalúa `WHERE` todavía no existe el promedio.
- Aquí basta un `JOIN` normal: un alumno sin notas no tiene promedio que comparar.
- `HAVING` compara el promedio **sin redondear**. Martina (5,875) está en riesgo con límite 6,0, aunque se muestre 5,9.

## Paso 6 — Resumen por asignatura (R6)

```sql
SELECT asignatura, COUNT(*) AS cantidad, ROUND(AVG(valor), 1) AS promedio,
       MIN(valor) AS minima, MAX(valor) AS maxima
FROM nota GROUP BY asignatura ORDER BY asignatura
```

`valor` es `DECIMAL(2,1)`, así que el promedio se calcula sin errores de punto flotante (5,05 redondea a 5,1 en MySQL y en H2). `getDouble` convierte el resultado al final.

**Pregunta 4:** con 1.200 alumnos × 40 notas = 48.000 filas. Calcular en Java obliga a traerlas todas por la red y crear 48.000 objetos para mostrar 3 números. En SQL viajan 3 filas. La base de datos está optimizada para agregar: aprovéchala.

## Verificación

| Prueba | Resultado en la solución |
|---|---|
| `mvn test` (H2) | 13 pruebas en verde. |
| `mvn test -Dbd.url=jdbc:mysql://...` (MySQL 8.4) | 13 pruebas en verde con los mismos scripts. |
| `mvn compile exec:java` | La salida del README. |

## Errores frecuentes

| Síntoma | Causa |
|---|---|
| El curso de todos los alumnos es su propio nombre | Falta el alias `c.nombre AS curso`. |
| Agustín no aparece en los promedios | `JOIN` en vez de `LEFT JOIN`. |
| Agustín aparece con 1 nota | `COUNT(*)` en vez de `COUNT(n.id)`. |
| Agustín aparece con promedio 0,0 | `getDouble` sin `wasNull()`. |
| `SQLException: Column 'nombre' not found` en H2 o MySQL | El `ResultSet` no tiene esa columna. Revisa los alias del `SELECT`. |
| `SQLException: Before start of result set` | Se leyó una columna sin llamar antes a `rs.next()`. |
| `Invalid use of group function` | Se usó `AVG` en el `WHERE` en vez de en el `HAVING`. |
