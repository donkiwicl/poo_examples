# EA3 · 01 · Conexión JDBC — Camping Lago Ranco

**Tema:** driver JDBC en el `pom.xml` (y su `scope`), URL de conexión, `DriverManager`, archivo `db.properties`, clase de conexión centralizada, `DatabaseMetaData`, diagnóstico de `SQLException` (código y `SQLState`), cierre de recursos con `try-with-resources`.
**Tipo:** consola + JUnit (H2) · **Tiempo estimado:** 1 bloque · **Sesión:** 3.1 (PPT 3.1.2 y 3.1.3)

> La guía de resolución está en la rama `solucion`, en el archivo `GUIA.md` de esta carpeta.

## Problema

El **Camping Lago Ranco** guarda sus sitios (carpas y quinchos) en una base de datos MySQL. Antes de construir cualquier pantalla, el equipo necesita una base confiable para conectarse:

- los datos de acceso (URL, usuario y contraseña) deben estar en **un solo archivo**, `db.properties`, y no repartidos por el código;
- cuando la conexión falla, el programa debe decir **qué** revisar: la contraseña, el nombre de la base, el servidor o el driver. Un *stack trace* no sirve;
- ninguna operación puede dejar conexiones abiertas. Un servidor MySQL acepta un número limitado de conexiones y, si se acaban, la aplicación deja de funcionar.

## Código inicial

| Archivo | Estado |
|---|---|
| `pom.xml` | **Falta el driver de MySQL y H2** (R1). |
| `sql/camping.sql` | Resuelto. Crea la base `camping_ranco` con la tabla `sitio` y 3 filas. |
| `src/main/resources/db.properties` | **Por completar** (R2). |
| `ConexionBD.java` | **Por completar** (R3 y R4). |
| `DiagnosticoConexion.java` | **Por completar** (R5). |
| `ResumenCamping.java` | Funciona, pero **deja recursos abiertos** (R6). |
| `Main.java` | Resuelto. |
| `src/test/.../ConexionBDTest.java` | 13 pruebas con H2 en memoria. Compilan desde el inicio y fallan hasta terminar. |

## Requerimientos

**R1. Dependencias.** Agrega al `pom.xml`:
- `com.mysql:mysql-connector-j:9.7.0`, el driver de MySQL;
- `com.h2database:h2:2.3.232`, una base de datos en memoria que usan las pruebas, así que no necesitan un servidor.

Elige el `scope` de cada una. Pista: busca en el código un `import com.mysql...`. ¿Lo encuentras? ¿El JAR final necesita H2?

**R2. Base de datos y configuración.** Ejecuta `sql/camping.sql` en tu MySQL local y completa `db.properties` con la URL (`jdbc:mysql://<host>:<puerto>/<base>`) y tu contraseña.

**R3. `ConexionBD`: leer la configuración.**
- El constructor recibe un `Properties`. `db.url` y `db.usuario` son obligatorias: si faltan o están en blanco, lanza `IllegalStateException` con el nombre de la propiedad. `db.clave` puede estar vacía.
- `desdeRecurso("/db.properties")` lee el archivo desde `resources` con `getResourceAsStream`. Si el archivo no existe, lanza `IllegalStateException` con su nombre.

**R4. `ConexionBD.abrir()`.** Retorna una conexión nueva con `DriverManager.getConnection(url, usuario, clave)`. Quien la pide es quien la cierra.

**R5. `DiagnosticoConexion.explicar(SQLException)`.** Traduce el error a un mensaje que diga qué revisar:

| Situación | Cómo se reconoce | El mensaje debe contener |
|---|---|---|
| Usuario o contraseña incorrectos | código `1045` o `SQLState` `28000` | `Usuario o contraseña` |
| La base de datos no existe | código `1049` | `La base de datos no existe` |
| No hay un driver para la URL | el mensaje empieza con `No suitable driver` | `driver` |
| Servidor detenido o puerto incorrecto | `SQLState` que empieza con `08` | `servidor` |
| Otro | — | `código <n>: <mensaje>` |

**R6. Sin fugas.** `ResumenCamping.generar()` abre una conexión, un `Statement` y un `ResultSet` y no cierra ninguno. Reescríbelo con `try-with-resources`. La prueba `sinFugasDeConexiones` llama al método 5 veces y cuenta las sesiones abiertas en H2.

## Resultado esperado (`mvn compile exec:java`)

Con todo correcto:

```
=== Camping Lago Ranco · prueba de conexión ===
URL: jdbc:mysql://localhost:3306/camping_ranco
Conectado a MySQL 8.4.9 · 3 sitios · capacidad total 22 personas
```

Provoca cada error a propósito, cambiando `db.properties` o deteniendo MySQL:

```
No se pudo conectar: Usuario o contraseña incorrectos. Revisa db.usuario y db.clave en db.properties.
No se pudo conectar: La base de datos no existe. Ejecuta sql/camping.sql en tu servidor MySQL.
No se pudo conectar: No se pudo contactar al servidor. Revisa que MySQL esté iniciado y el host y el puerto de db.url.
No se pudo conectar: No hay un driver JDBC para la URL. Revisa la dependencia mysql-connector-j en el pom.xml y que db.url empiece con jdbc:mysql://
Configuración inválida: Falta la propiedad db.url en db.properties.
```

## Cómo ejecutar

```bash
mvn test                 # objetivo: 13 pruebas en verde (no necesita MySQL)
mvn compile exec:java    # necesita MySQL con sql/camping.sql ejecutado
```

## Preguntas para pensar

1. ¿Qué función cumple el driver? ¿Qué pasa si falta en el `pom.xml`: falla al compilar o al ejecutar? ¿Por qué?
2. ¿Por qué es mala práctica dejar la contraseña escrita en el código? ¿`db.properties` dentro de `resources` resuelve el problema por completo?
3. ¿Por qué dejar un `Connection` abierto es peor que dejar un `Scanner` abierto?
4. Si `try-with-resources` cierra todo solo, ¿en qué orden lo cierra? ¿Qué pasa si se lanza una excepción en medio?
