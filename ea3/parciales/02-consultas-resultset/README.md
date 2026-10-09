# EA3 · 02 · Consultas y ResultSet — Notas del Liceo Bicentenario de Talca

**Tema:** `SELECT` con `WHERE`, `ORDER BY`, `JOIN` y `LEFT JOIN`, funciones de agregación (`COUNT`, `AVG`, `MIN`, `MAX`), `GROUP BY` y `HAVING`, recorrido de un `ResultSet`, **mapeo de filas a objetos** por nombre de columna, alias, valores `NULL` (`wasNull`), fechas (`LocalDate`), `Optional`.
**Tipo:** consola + JUnit (H2) · **Tiempo estimado:** 1 a 2 bloques · **Sesión:** 3.1 (PPT 3.1.1 y 3.1.4)

> La guía de resolución está en la rama `solucion`, en el archivo `GUIA.md` de esta carpeta.

## Problema

La UTP del **Liceo Bicentenario de Talca** tiene las notas en MySQL y necesita reportes: la nómina por curso, el promedio de cada alumno, quiénes están en riesgo de repetir y cómo va cada asignatura. La conexión ya está resuelta (`ConexionBD`, como en el caso 01). Falta la capa que **consulta** y convierte cada fila en un objeto Java.

```
curso (id, nombre, profesor_jefe)
  └─< alumno (id, rut, nombre, fecha_nacimiento, correo_apoderado NULL, curso_id → curso)
        └─< nota (id, alumno_id → alumno, asignatura, valor DECIMAL(2,1))
```

Datos de prueba (`sql/datos.sql`): 2 cursos y 6 alumnos. Dos apoderados no informaron correo, y **Agustín Díaz** llegó transferido y aún no tiene notas.

## Código inicial

| Archivo | Estado |
|---|---|
| `sql/crear-bd.sql`, `tablas.sql`, `datos.sql` | Resueltos. |
| `dao/ConexionBD.java` | Resuelto. |
| `model/Alumno`, `PromedioAlumno`, `ResumenAsignatura` | Resueltos. |
| `dao/ConsultasLiceoDao.java` | `contarAlumnos()` resuelto como ejemplo. **R1 a R6 por completar.** |
| `Main.java` | Resuelto. Imprime todos los reportes. |
| `src/test/.../ConsultasLiceoDaoTest.java` | 13 pruebas con H2 que ejecutan los mismos scripts de `sql/`. |

## Requerimientos

Para todos los métodos:
- usa `try-with-resources` para `Connection`, `PreparedStatement` y `ResultSet`;
- lee las columnas **por nombre** (`rs.getString("rut")`), no por posición (`rs.getString(2)`);
- los valores que llegan por parámetro van en un `?`.

**R1. `listarAlumnos()`.** Todos los alumnos con el **nombre de su curso**, ordenados por curso y luego por nombre. Escribe un método privado `mapearAlumno(ResultSet)` que reutilizarás en R2 y R3.
- `alumno` y `curso` tienen las dos una columna `nombre`: usa un **alias**.
- La fecha se lee con `rs.getObject("fecha_nacimiento", LocalDate.class)`.
- `correo_apoderado` puede ser `NULL`.

**R2. `buscarPorRut(String)`.** Retorna `Optional<Alumno>`: vacío si no existe.

**R3. `listarPorCurso(String)`.** Los alumnos del curso, por nombre. Si el curso no existe, retorna una lista vacía.

**R4. `promediosDelCurso(String)`.** Promedio de cada alumno con un decimal, del mayor al menor. Los alumnos **sin notas también aparecen**, al final, con promedio `null` y 0 notas. Pistas: `LEFT JOIN`, `GROUP BY` y `ROUND(AVG(...), 1)`. ¿Qué retorna `getDouble` cuando el valor es `NULL`?

**R5. `alumnosEnRiesgo(double limite)`.** Alumnos de todos los cursos con promedio menor que el límite, del menor al mayor promedio. Un alumno sin notas **no** está en riesgo. Pista: `HAVING`.

**R6. `resumenPorAsignatura()`.** Por asignatura, en orden alfabético: cantidad de notas, promedio con un decimal, nota mínima y nota máxima.

## Resultado esperado (`mvn compile exec:java`)

```
=== Liceo Bicentenario de Talca · 6 alumnos ===

-- Nómina
3°A  Benjamín Soto    21.456.789-K  (sin correo)
3°A  Martina Pérez    21.567.890-1  lperez@correo.cl
...
-- Promedios 3°B
3°B  Isidora Muñoz    6,8       (3 notas)
3°B  Tomás Fuentes    3,7       (4 notas)
3°B  Agustín Díaz     sin notas (0 notas)

-- En riesgo (promedio menor que 4,0)
3°B  Tomás Fuentes    3,7       (4 notas)
3°A  Benjamín Soto    3,8       (4 notas)

-- Por asignatura
Historia    6 notas · promedio 5,1 · mínima 3,5 · máxima 6,9
Lenguaje    6 notas · promedio 5,2 · mínima 3,0 · máxima 6,6
Matemática  7 notas · promedio 5,4 · mínima 3,2 · máxima 7,0
```

## Cómo ejecutar

```bash
mvn test                 # objetivo: 13 pruebas en verde (H2, no necesita MySQL)
mvn compile exec:java    # necesita MySQL con los 3 scripts de sql/ ejecutados en orden

# Opcional: las mismas pruebas contra tu MySQL (reinician las tablas de liceo_talca)
mvn test -Dbd.url=jdbc:mysql://localhost:3306/liceo_talca -Dbd.usuario=root -Dbd.clave=tu_clave
```

## Preguntas para pensar

1. ¿Por qué leer columnas por posición hace frágil el código? ¿Qué pasa si alguien agrega una columna a la tabla?
2. ¿En qué se diferencian `WHERE` y `HAVING`? ¿Podrías escribir R5 con `WHERE AVG(valor) < ?`?
3. ¿Por qué R4 usa `LEFT JOIN` y R5 un `JOIN` normal?
4. Calcular el promedio en SQL o traer todas las notas y calcularlo en Java: ¿qué cambia si el liceo tiene 1.200 alumnos con 40 notas cada uno?
5. ¿En qué capa debe ocurrir el mapeo de `ResultSet` a objetos? ¿Por qué no en el controlador o en `Main`?
