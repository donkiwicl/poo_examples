# 04 · Maven — Validador de RUT

**Tema:** `pom.xml`, coordenadas GAV, propiedades, dependencias y `scope`, plugins, ciclo de vida (`compile`, `test`, `package`), JAR ejecutable, trabajo sin internet.
**Tipo:** consola · **Tiempo estimado:** 1 bloque

> La guía de resolución está en la rama `solucion`, en el archivo `GUIA.md` de esta carpeta.

## Problema

Un compañero escribió un validador de RUT chileno (algoritmo módulo 11) con sus pruebas unitarias, pero dejó el `pom.xml` a medias. Hoy el proyecto compila, pero:

- `mvn test` falla con `package org.junit.jupiter.api does not exist`;
- `mvn exec:java` no sabe qué clase ejecutar;
- `java -jar target/rut-...jar` responde `no main manifest attribute`;
- Maven advierte que la codificación depende de la plataforma.

**En este caso no se escribe Java:** todo el trabajo está en el `pom.xml`.

## Código inicial

| Archivo | Estado |
|---|---|
| `ValidadorRut.java` | Resuelto: `esValido`, `calcularDigitoVerificador`, `formatear`. |
| `Main.java` | Resuelto: valida los RUT recibidos como argumentos. |
| `ValidadorRutTest.java` | Resuelto. No compila hasta que JUnit esté en el `pom.xml`. |
| `pom.xml` | **Por completar.** Busca los `TODO`. |

## Requerimientos

**R1. Coordenadas.** Explica en un comentario qué identifican `groupId`, `artifactId` y `version`. Cambia la versión a `1.0.0`.

**R2. Codificación.** Agrega la propiedad `project.build.sourceEncoding` con `UTF-8` y verifica que desaparezca la advertencia `Using platform encoding`.

**R3. Pruebas.**
- Agrega la dependencia `org.junit.jupiter:junit-jupiter:5.12.2` con `scope` **test**. Declara la versión como una propiedad `junit.version`.
- Fija `maven-surefire-plugin` en `3.5.4`. Comprueba qué pasa **sin** este paso: ¿cuántas pruebas ejecuta `mvn test`?
- `mvn test` debe terminar con `Tests run: 4, Failures: 0`.

**R4. Ejecutar desde Maven.** Configura `exec-maven-plugin` 3.5.0 con la clase principal:
```bash
mvn compile exec:java -Dexec.args="12.345.678-5 10.000.013-K"
```

**R5. JAR ejecutable.** Configura `maven-jar-plugin` 3.4.2 para que escriba `Main-Class` en el `MANIFEST.MF`:
```bash
mvn clean package
java -jar target/rut-1.0.0.jar 12.345.678-5
```
Evita repetir el nombre de la clase en R4 y R5: usa una propiedad propia, por ejemplo `${clase.principal}`.

**R6. Ciclo de vida.** Responde en el README de tu fork:
1. ¿Qué fases ejecuta `mvn package`? ¿Se ejecutan las pruebas?
2. ¿Dónde guarda Maven JUnit en tu computador? Búscalo en `~/.m2/repository`.
3. Ejecuta `mvn -o clean package`. ¿Qué significa `-o` y por qué importa en la Evaluación Parcial 2?
4. ¿Por qué JUnit **no** queda dentro del JAR?

## Resultado esperado

```
$ java -jar target/rut-1.0.0.jar
valido   12.345.678-5
valido   12.345.678-5
invalido 11.111.111-2
valido   10.000.013-K
invalido abc
```

## Cómo ejecutar

```bash
mvn compile      # hoy funciona
mvn test         # hoy falla: es lo que debes arreglar
mvn package
```
