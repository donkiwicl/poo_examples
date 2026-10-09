# 10 · Persistencia JSON — Agenda de contactos con Jackson y DAO

**Tema:** Jackson (`ObjectMapper`), serialización de clases abstractas con `@JsonTypeInfo` y `@JsonSubTypes`, constructor sin parámetros, `TypeReference`, *pretty print*, patrón **DAO** (interfaz + implementación), traducción de `IOException` a una excepción propia, casos borde de archivos, pruebas con `@TempDir`.
**Tipo:** consola · **Tiempo estimado:** 1 bloque

> La guía de resolución está en la rama `solucion`, en el archivo `GUIA.md` de esta carpeta.

## Problema

Una agenda guarda contactos **personales** (apodo, favorito) y **laborales** (empresa, cargo). Al cerrar el programa los datos se pierden. Se necesita guardarlos en `data/contactos.json` y que al volver a abrirlo:

- cada contacto vuelva con **su clase real** (`ContactoPersonal` o `ContactoLaboral`) y todos sus datos;
- la primera ejecución (sin archivo) no sea un error;
- un archivo dañado, o con datos que el modelo no acepta, se informe con un mensaje comprensible y sin detener el programa.

El resto del programa debe trabajar con la interfaz `ContactoDao`, sin saber que los datos están en un JSON.

## Código inicial

| Archivo | Estado |
|---|---|
| `model/Contacto` (abstracta), `ContactoPersonal`, `ContactoLaboral` | Reglas y validaciones resueltas. **Faltan las preparaciones para Jackson.** |
| `dao/ContactoDao.java`, `PersistenciaException.java` | Resueltos. |
| `dao/JsonContactoDao.java` | **Por completar.** |
| `Main.java` | Resuelto. Recorre los cinco escenarios del problema. |
| `src/test/.../JsonContactoDaoTest.java` | Compila desde el inicio. **6 de 7 pruebas fallan** hasta terminar. |

## Requerimientos

**R1. Subtipos.** `Contacto` es abstracta: Jackson no sabe qué clase crear al leer. Anótala para que cada objeto del JSON incluya `"tipo" : "PERSONAL"` o `"tipo" : "LABORAL"`.

**R2. Constructores para Jackson.** Agrega un constructor **sin parámetros** `protected` en las tres clases. Jackson crea el objeto vacío y luego llama a los *setters*. ¿Qué ventaja tiene eso para las validaciones?

**R3. `cargar()`**
- Archivo inexistente o vacío → lista vacía.
- Lee con `ObjectMapper` y un `TypeReference<List<Contacto>>`.
- Cualquier problema de lectura o de datos → `PersistenciaException` con un mensaje comprensible y la excepción original como **causa**.

**R4. `guardar(List<Contacto>)`**
- Crea la carpeta `data/` si no existe.
- Escribe con formato legible (*pretty print*) y con el `tipo` de cada contacto.
- `IOException` → `PersistenciaException`.

## Resultado esperado (`mvn compile exec:java`)

```
== 1. Primera ejecucion (el archivo no existe)
0 contacto(s)

== 2. Guardar tres contactos
Guardado en .../data/contactos.json
[ {
  "tipo" : "PERSONAL",
  "nombre" : "Ana Rojas",
  "telefono" : "+56911112222",
  "email" : "ana.rojas@mail.cl",
  "apodo" : "Anita",
  "favorito" : true
}, ...

== 3. Cargar con un DAO nuevo (como si se reiniciara el programa)
3 contacto(s)
  [Personal] Ana Rojas | +56911112222 | ana.rojas@mail.cl | "Anita" | ★
  [Personal] Bruno Diaz | +56933334444 | bruno@mail.cl
  [Laboral]  Carla Soto | 225551234 | csoto@kiwi.cl | Jefa de TI en Kiwi SpA

== 4. Archivo danado
ERROR: No se pudieron leer los contactos de danado.json: el archivo esta danado o tiene datos invalidos.

== 5. Archivo con un dato que el modelo rechaza
ERROR: No se pudieron leer los contactos de invalido.json: el archivo esta danado o tiene datos invalidos.
```

## Cómo ejecutar

```bash
mvn test                 # objetivo: 7 pruebas en verde
mvn compile exec:java
```

## Preguntas para pensar

1. Sin `@JsonTypeInfo`, ¿qué error aparece al **leer**? ¿Y al **escribir**?
2. ¿Por qué se usa `TypeReference<List<Contacto>>` y no `List.class`?
3. ¿Por qué `cargar()` no deja escapar la `IOException` tal como viene?
4. Si mañana los contactos se guardan en una base de datos, ¿qué clases cambian?
