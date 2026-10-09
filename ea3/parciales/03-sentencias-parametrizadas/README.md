# EA3 · 03 · Sentencias parametrizadas — Socios del Club Deportivo Los Halcones

**Tema:** `Statement` frente a `PreparedStatement`, *placeholders* `?`, `setString`/`setInt`/`setDouble`/`setBoolean`/`setObject`, `executeQuery` frente a `executeUpdate` (filas afectadas), **inyección SQL**, `LIKE` con parámetros, claves generadas (`RETURN_GENERATED_KEYS`), restricción `UNIQUE` y `SQLIntegrityConstraintViolationException`.
**Tipo:** consola + JUnit (H2) · **Tiempo estimado:** 1 a 2 bloques · **Sesión:** 3.1 (PPT 3.1.4 y 3.1.5)

> La guía de resolución está en la rama `solucion`, en el archivo `GUIA.md` de esta carpeta.

## Problema

El **Club Deportivo Los Halcones** administra sus socios con una pequeña aplicación. Un socio que se llama **Bernardo O'Higgins** no puede ser encontrado por el buscador: el programa se cae con un error de SQL. Al revisar, el equipo descubrió algo peor: si en el buscador se escribe `' OR '1'='1`, aparecen **todos** los socios.

La causa es `buscarPorNombreInseguro`, que **concatena** el texto del usuario dentro del SQL. Hay que reescribir el acceso a datos con `PreparedStatement` y completar el CRUD.

```
socio (id AUTO_INCREMENT, rut UNIQUE, nombre, categoria, cuota_mensual, fecha_ingreso, activo)
```

## Código inicial

| Archivo | Estado |
|---|---|
| `sql/*.sql`, `dao/ConexionBD`, `model/Socio`, `Categoria` | Resueltos. |
| `dao/SocioDao.java` | `listarTodos()` resuelto y `buscarPorNombreInseguro()` **vulnerable** (para comparar). **R1 a R6 por completar.** |
| `dao/SocioDuplicadoException.java` | Resuelto. |
| `Main.java` | Resuelto. Compara las dos búsquedas y recorre el CRUD. |
| `src/test/.../SocioDaoTest.java` | 14 pruebas con H2. Dos demuestran la vulnerabilidad y pasan desde el inicio. |

## Requerimientos

**R1. `buscarPorNombre(String)`.** La misma búsqueda, con `PreparedStatement` y sin distinguir mayúsculas (`tapia` encuentra a *Florencia Tapia*). ¿Dónde van los `%` del `LIKE`?

**R2. `insertar(Socio)`.**
- Inserta con parámetros y **asigna al socio el `id` generado** por la base de datos (`Statement.RETURN_GENERATED_KEYS` y `getGeneratedKeys()`).
- Un RUT repetido viola la restricción `UNIQUE`. Tradúcelo a `SocioDuplicadoException("Ya existe un socio con el RUT <rut>.")` y conserva la excepción original como causa.

**R3. `actualizar(Socio)`.** Actualiza nombre, categoría, cuota y estado según el `id`. Retorna `true` si modificó una fila y `false` si el `id` no existe (usa lo que retorna `executeUpdate`).

**R4. `eliminar(int id)`.** Retorna `true` si el socio existía.

**R5. `reajustarCuotas(Categoria, double porcentaje)`.** Con **un solo `UPDATE`**, sube la cuota de los socios **activos** de la categoría (10 = +10 %, redondeado al peso). Retorna cuántos socios cambiaron.

**R6. `filtrar(Categoria, LocalDate desde)`.** Socios de la categoría que ingresaron desde esa fecha (inclusive), del más antiguo al más nuevo. Envía la fecha con `setObject`.

## Resultado esperado (`mvn compile exec:java`)

```
-- Búsqueda con el texto: ' OR '1'='1
Insegura: 6 socios (¡todos!)
Segura:   0 socios

-- Búsqueda: O'Higgins
Insegura: error de SQL (SQLSyntaxErrorException)
Segura:   [#1  Bernardo O'Higgins Riquelme  12.345.678-5 SENIOR   $12.000 2019-03-01]

-- Insertar
Insertado con id 7
Rechazado: Ya existe un socio con el RUT 20.111.222-3.

-- Actualizar y eliminar
Actualizar #7: true
Eliminar #7: true
Eliminar #7 otra vez: false

-- Reajuste de 10 % a INFANTIL (solo activos)
1 socio(s) reajustado(s)
```

## Cómo ejecutar

```bash
mvn test                 # objetivo: 14 pruebas en verde (H2)
mvn compile exec:java    # MySQL: ejecuta antes crear-bd.sql, tablas.sql y datos.sql
```

## Preguntas para pensar

1. Escribe el SQL que arma `buscarPorNombreInseguro` con el texto `' OR '1'='1`. ¿Por qué retorna todos los socios?
2. ¿Por qué `PreparedStatement` no se confunde con la comilla de *O'Higgins*? ¿«Escapar» las comillas a mano es igual de seguro?
3. Además de la seguridad, ¿qué otras dos ventajas tiene `PreparedStatement`?
4. ¿Por qué el `id` lo genera la base de datos y no Java (por ejemplo, contando los socios y sumando 1)?
5. `actualizar` retorna `false` si no encuentra el `id`. ¿Debería lanzar una excepción? ¿Quién decide qué hacer?
