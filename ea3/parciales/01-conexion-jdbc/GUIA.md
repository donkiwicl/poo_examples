# Guía de resolución · EA3 01 Conexión JDBC

> Para comparar tu trabajo con la solución: `git diff main solucion -- ea3/parciales/01-conexion-jdbc`

## Idea central

```
Main ──► ResumenCamping ──► ConexionBD.abrir() ──► DriverManager ──► driver (mysql-connector-j) ──► MySQL
                                 ▲
                         db.properties (url, usuario, clave)
```

- **JDBC** es un conjunto de **interfaces** (`Connection`, `Statement`, `ResultSet`...) que vienen en el JDK (`java.sql`). Cada motor publica un **driver** que las implementa.
- `DriverManager` recorre los drivers disponibles y le entrega la URL a cada uno. El que la reconoce (`jdbc:mysql:` → Connector/J, `jdbc:h2:` → H2) abre la conexión. Por eso el mismo código funciona con MySQL al ejecutar y con H2 en las pruebas: solo cambia la URL.

## Paso 1 — Dependencias (R1)

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>9.7.0</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.3.232</version>
    <scope>test</scope>
</dependency>
```

| Scope | Compila con él | Se ejecuta con él | Va en el JAR/distribución |
|---|---|---|---|
| `compile` (por defecto) | Sí | Sí | Sí |
| `runtime` | **No** | Sí | Sí |
| `test` | Solo las pruebas | Solo las pruebas | No |

- **Driver con `runtime`:** el código nunca importa `com.mysql...`, solo `java.sql`. Con `runtime`, si alguien escribe por error `import com.mysql.cj.jdbc.ConnectionImpl`, el proyecto **no compila** y el acoplamiento al motor se detecta de inmediato. `compile` también funciona, pero no protege de ese error.
- **Pregunta 1:** sin el driver, el proyecto **compila** (las interfaces están en el JDK), pero al ejecutar `DriverManager` lanza `SQLException: No suitable driver found for jdbc:mysql://...` (`SQLState 08001`).

## Paso 2 — `db.properties` (R2)

```properties
db.url=jdbc:mysql://localhost:3306/camping_ranco
db.usuario=root
db.clave=
```

`jdbc:mysql://localhost:3306/camping_ranco` se lee **protocolo** (`jdbc:mysql:`) + **host** (`localhost`) + **puerto** (`3306`, el de MySQL por defecto) + **base de datos** (`camping_ranco`).

**Pregunta 2:** separar la configuración evita tocar el código para cambiar de equipo o de servidor, pero `resources` queda **dentro del JAR**: cualquiera que lo descomprima ve la clave. En producción se usa un archivo externo, variables de entorno o un gestor de secretos. Para el curso, `resources` basta, pero conviene no subir claves reales a Git.

## Paso 3 — `ConexionBD` (R3 y R4)

```java
public ConexionBD(Properties propiedades) {
    this.url = obligatoria(propiedades, PROPIEDAD_URL);
    this.usuario = obligatoria(propiedades, PROPIEDAD_USUARIO);
    this.clave = propiedades.getProperty(PROPIEDAD_CLAVE, "");
}

public static ConexionBD desdeRecurso(String recurso) {
    try (InputStream entrada = ConexionBD.class.getResourceAsStream(recurso)) {
        if (entrada == null) {
            throw new IllegalStateException("No se encontró el archivo " + recurso + " en resources.");
        }
        Properties propiedades = new Properties();
        propiedades.load(entrada);
        return new ConexionBD(propiedades);
    } catch (IOException e) { ... }
}

public Connection abrir() throws SQLException {
    return DriverManager.getConnection(url, usuario, clave);
}
```

- **Falla temprano:** una URL vacía se detecta al crear `ConexionBD`, con un mensaje que nombra la propiedad. No se espera a la primera consulta, que daría un error confuso.
- `getResourceAsStream` retorna `null` (no lanza excepción) si el recurso no existe. Por eso se revisa explícitamente.
- **¿Por qué instancia y no `static`?** Las guías del curso usan `DatabaseConnection.getConnection()` estático, con la URL en una constante. Funciona, pero obliga a usar siempre la misma base. Con una instancia, las pruebas crean una `ConexionBD` con H2 y el programa una con MySQL. Es **inyección de dependencias**: quien usa la conexión la recibe en su constructor (`new ResumenCamping(conexion)`).
- `abrir()` crea una conexión **nueva** cada vez y no la guarda en un atributo. Una conexión compartida queda inválida si el servidor la corta, y no puede usarse desde dos hilos a la vez. En aplicaciones grandes se usa un *pool* (`DataSource`), que reutiliza conexiones de forma segura.

## Paso 4 — Diagnóstico (R5)

```java
if (e.getErrorCode() == 1045 || estado.equals("28000")) → "Usuario o contraseña incorrectos..."
if (e.getErrorCode() == 1049)                           → "La base de datos no existe..."
if (mensaje.startsWith("No suitable driver"))           → "No hay un driver JDBC para la URL..."
if (estado.startsWith("08"))                            → "No se pudo contactar al servidor..."
```

Errores reales de Connector/J 9.7 contra MySQL 8.4:

| Caso | Clase | `SQLState` | Código |
|---|---|---|---|
| Contraseña incorrecta | `SQLException` | `28000` | 1045 |
| Base inexistente | `SQLSyntaxErrorException` | `42000` | 1049 |
| Servidor detenido / puerto malo | `CommunicationsException` | `08S01` | 0 |
| URL sin driver (`jdbc:mysq://`) | `SQLException` | `08001` | 0 |

- `SQLState` es un **estándar** (las dos primeras letras indican la clase: `08` conexión, `23` restricción, `42` sintaxis). El **código** es propio de cada motor (1045 solo significa algo en MySQL).
- El orden importa: el driver faltante también tiene `SQLState 08001`, así que se revisa **antes** que la regla general `08`.
- El mensaje dice **qué hacer**, no solo qué pasó.

## Paso 5 — Sin fugas (R6)

```java
try (Connection con = conexion.abrir();
     Statement st = con.createStatement();
     ResultSet rs = st.executeQuery(SQL_RESUMEN)) {
    rs.next();
    ...
    return "Conectado a ...";
}
```

- **Pregunta 4:** al salir del bloque (con `return` o con una excepción), Java llama a `close()` en **orden inverso**: `rs`, `st` y `con`. Si `executeQuery` falla, `st` y `con` se cierran igual. Si además falla un `close()`, esa excepción queda como *suprimida* dentro de la principal (`e.getSuppressed()`).
- **Pregunta 3:** cada conexión es un *socket* y un hilo en el servidor. MySQL acepta 151 por defecto (`max_connections`). Una aplicación que pierde una por consulta **deja de funcionar** después de unos minutos, también para los demás usuarios. Un `Scanner` abierto solo afecta al propio programa.
- La prueba cuenta `INFORMATION_SCHEMA.SESSIONS` en H2. Con el código inicial quedan 6 sesiones (5 perdidas + la de la consulta), y con la solución solo 1.

## Verificación

| Prueba | Resultado en la solución |
|---|---|
| `mvn test` | 13 pruebas en verde. Con el `ResumenCamping` inicial falla solo `sinFugasDeConexiones`. |
| MySQL 8.4 con `sql/camping.sql` | `Conectado a MySQL 8.4.9 · 3 sitios · capacidad total 22 personas` |
| Contraseña mala, base `campin`, puerto 3399, URL `jdbc:mysq://` y URL vacía | Los cinco mensajes del README. |

## Errores frecuentes

| Síntoma | Causa |
|---|---|
| `No suitable driver found for jdbc:mysql://...` | Falta el driver en el `pom.xml`, o Maven no recargó el proyecto. También pasa con una URL mal escrita. |
| `Communications link failure` | MySQL detenido, puerto distinto al de la URL (algunas instalaciones usan 3307) o un *firewall*. |
| `Public Key Retrieval is not allowed` | Usuario con `caching_sha2_password` y conexión sin SSL. Agrega `?allowPublicKeyRetrieval=true&useSSL=false` a la URL (solo en desarrollo local). |
| `NullPointerException` en `properties.load` | `getResourceAsStream` retornó `null`: el nombre no empieza con `/` o el archivo no está en `src/main/resources`. |
| Las pruebas pasan pero `exec:java` no conecta | Las pruebas usan H2 (`db-prueba.properties`), no tu MySQL. Revisa `db.properties`. |
