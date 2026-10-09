# Guía de resolución · 04 Maven

> Para comparar tu trabajo con la solución: `git diff main solucion -- puntuales/04-maven/pom.xml`

## Idea central

Maven reemplaza los pasos manuales ("descargar el `.jar` de JUnit, agregarlo al *classpath*, compilar, copiar...") por una **descripción declarativa** del proyecto: el `pom.xml`. Tú dices **qué** necesitas y Maven resuelve **cómo**. Además, cualquier IDE o servidor de integración continua construye el proyecto exactamente igual.

## R1 — Coordenadas GAV

```xml
<groupId>cl.dsy1102.ejemplos</groupId>   <!-- organización, dominio invertido -->
<artifactId>rut</artifactId>             <!-- nombre del proyecto -->
<version>1.0.0</version>                 <!-- versión del artefacto -->
```

Las tres juntas (`cl.dsy1102.ejemplos:rut:1.0.0`) identifican el artefacto de forma única en cualquier repositorio Maven. El JAR se llama `artifactId-version.jar`: `rut-1.0.0.jar`. El sufijo `-SNAPSHOT` indica una versión en desarrollo que puede cambiar. Una versión sin sufijo es una entrega fija.

## R2 — Propiedades

```xml
<properties>
    <maven.compiler.release>25</maven.compiler.release>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <clase.principal>cl.dsy1102.ejemplos.rut.Main</clase.principal>
    <junit.version>5.12.2</junit.version>
</properties>
```

- `maven.compiler.release` y `project.build.sourceEncoding` son propiedades **que leen los plugins** (compiler y resources).
- `clase.principal` y `junit.version` son **propias**: se usan con `${...}` para no repetir valores. Si cambia la clase principal, se modifica en un solo lugar.

## R3 — Dependencias y `scope`

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>${junit.version}</version>
    <scope>test</scope>
</dependency>
```

| `scope` | Disponible en `src/main` | En `src/test` | Viaja con la aplicación |
|---|---|---|---|
| `compile` (por defecto) | Sí | Sí | Sí |
| `test` | **No** | Sí | No |
| `provided` | Sí | Sí | No (lo aporta el servidor) |
| `runtime` | No | Sí | Sí |

Con `scope test`, si alguien escribe `import org.junit...` en `src/main`, no compila. JUnit tampoco se incluye al distribuir la aplicación (pregunta 4).

`junit-jupiter` es un **agregador**: trae `junit-jupiter-api` (las anotaciones) y `junit-jupiter-engine` (el motor) como dependencias **transitivas**. Para verlas: `mvn dependency:tree`.

**Sin surefire 3.x:** Maven 3.8 usa por defecto `maven-surefire-plugin` 2.12.4, que solo conoce JUnit 4. El resultado es `Tests run: 0` y un `BUILD SUCCESS` engañoso. Por eso se fija la versión.

## R4 — `exec-maven-plugin`

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <version>3.5.0</version>
    <configuration>
        <mainClass>${clase.principal}</mainClass>
    </configuration>
</plugin>
```

`exec:java` es un *goal* suelto que **no** pertenece al ciclo de vida. Por eso se escribe `mvn compile exec:java`: primero la fase `compile` y luego el goal. Los argumentos se pasan con `-Dexec.args="..."`.

## R5 — JAR ejecutable

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-jar-plugin</artifactId>
    <version>3.4.2</version>
    <configuration>
        <archive>
            <manifest>
                <mainClass>${clase.principal}</mainClass>
            </manifest>
        </archive>
    </configuration>
</plugin>
```

`java -jar` busca la línea `Main-Class:` dentro de `META-INF/MANIFEST.MF`. Para comprobarla: `unzip -p target/rut-1.0.0.jar META-INF/MANIFEST.MF`.

> Este JAR funciona solo porque la aplicación no tiene dependencias en tiempo de ejecución. Si usara Jackson, por ejemplo, `java -jar` fallaría con `NoClassDefFoundError`. Se necesitaría un *fat jar* (`maven-shade-plugin`) o `mvn javafx:run`/`jlink` en el caso de JavaFX.

## R6 — Ciclo de vida

1. `mvn package` ejecuta **todas** las fases anteriores en orden: `validate → compile → test-compile → test → package`. Sí, ejecuta las pruebas, y si una falla no se genera el JAR. Para omitirlas existe `-DskipTests`, pero no se recomienda como costumbre.
2. En `~/.m2/repository/org/junit/jupiter/junit-jupiter-api/5.12.2/`. La ruta replica el `groupId` (los puntos se convierten en carpetas), el `artifactId` y la `version`.
3. `-o` (*offline*) usa solo `~/.m2` y no intenta descargar nada. La EP2 se rinde sin internet: si una dependencia o un **plugin** no está en `~/.m2`, la compilación falla. Por eso los proyectos del curso fijan las versiones de `clean`, `resources` y `surefire`, y conviene ejecutar `mvn package` una vez con conexión antes de la evaluación.
4. Por el `scope test`.

## Verificación

```bash
mvn clean package                       # Tests run: 4, Failures: 0 · BUILD SUCCESS
java -jar target/rut-1.0.0.jar          # salida del README
mvn compile exec:java -Dexec.args="7.654.321-6"
```

## Errores frecuentes

| Síntoma | Causa |
|---|---|
| `package org.junit.jupiter.api does not exist` | Falta la dependencia, o se escribió `scope compile` en una prueba ubicada en `src/main`. |
| `Tests run: 0` | Surefire antiguo (2.12.4) o clases de prueba que no terminan en `Test`. |
| `no main manifest attribute, in target/rut-1.0.0.jar` | Falta `<archive><manifest><mainClass>`. |
| `The parameters 'mainClass' for goal ...exec:java are missing` | Falta la configuración de exec. |
| `Cannot access central in offline mode` | Ese plugin o dependencia nunca se descargó. Ejecuta el mismo comando una vez con internet. |
| `release version 25 not supported` | El JDK instalado es anterior a 25. Instala JDK 25 o baja `maven.compiler.release`. |
