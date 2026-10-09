# Guía de resolución · 10 Persistencia JSON

> Para comparar tu trabajo con la solución: `git diff main solucion -- puntuales/10-persistencia-json`

## Idea central

```
Main ──usa──► ContactoDao (interfaz) ◄──implementa── JsonContactoDao ──► ObjectMapper ──► data/contactos.json
                    │
                    └── lanza PersistenciaException (nunca IOException)
```

- **DAO** (*Data Access Object*): la única clase que sabe **dónde** y **cómo** se guardan los datos. El resto del programa conoce solo la interfaz (pregunta 4: si se cambia a una base de datos, se escribe `BdContactoDao` y se cambia **una línea** donde se crea el DAO).
- **Jackson** convierte objetos en JSON (*serializar*) y JSON en objetos (*deserializar*) usando los *getters* y *setters* (convención JavaBean).

## Paso 1 — Subtipos (R1)

```java
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "tipo")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ContactoPersonal.class, name = "PERSONAL"),
        @JsonSubTypes.Type(value = ContactoLaboral.class, name = "LABORAL")
})
public abstract class Contacto { ... }
```

**Pregunta 1:** sin las anotaciones, al **escribir** no hay error, pero el JSON queda sin `tipo`. Al **leer**, Jackson intenta crear un `Contacto` y falla: *"Cannot construct instance of `Contacto` (no Creators, like default constructor, exist): abstract types either need to be mapped to concrete types, have custom deserializer, or contain additional type information"*.

`Id.NAME` usa nombres propios (`PERSONAL`) en vez del nombre completo de la clase. Así, si se renombra o se mueve el paquete, los archivos existentes siguen funcionando.

## Paso 2 — Constructor sin parámetros (R2)

```java
protected Contacto() { }
protected ContactoPersonal() { }
```

Jackson crea el objeto vacío y llama a `setNombre`, `setEmail`... Como los *setters* validan, **un archivo editado a mano con un email inválido se rechaza al cargar**: el modelo se protege solo y no hace falta duplicar validaciones en el DAO.

Es `protected` para que no se use desde la aplicación (crearía un contacto sin nombre). Jackson puede invocarlo igual por reflexión.

> En un proyecto **con `module-info.java`** (los JavaFX), el paquete del modelo debe abrirse a Jackson: `opens ...model to com.fasterxml.jackson.databind;`. Este caso es de consola y no tiene módulos, así que no lo necesita.

## Paso 3 — `cargar()` (R3)

```java
private static final TypeReference<List<Contacto>> TIPO_LISTA = new TypeReference<>() { };

if (!Files.exists(archivo) || Files.size(archivo) == 0) {
    return new ArrayList<>();
}
return mapper.readValue(archivo.toFile(), TIPO_LISTA);
```

- **Pregunta 2:** por el *type erasure*, `List<Contacto>.class` no existe. Con `List.class`, Jackson no sabría qué contiene la lista y crearía `LinkedHashMap` en vez de contactos. `TypeReference` es una subclase anónima que **conserva** el tipo genérico completo.
- **Primera ejecución:** que no exista el archivo es normal, no un error.
- Se retorna `new ArrayList<>()` (modificable), no `List.of()`, porque quien llama probablemente agregará elementos.

**Traducción de excepciones (pregunta 3):**

```java
} catch (IOException | IllegalArgumentException e) {
    throw new PersistenciaException("No se pudieron leer los contactos de ...", e);
}
```

- La `IOException` es un detalle **del JSON**. Si el DAO la dejara pasar, `Main` o un controlador JavaFX tendría que conocer detalles del almacenamiento. `PersistenciaException` es parte del **contrato** del DAO y vale igual para JSON, CSV o una base de datos.
- Se pasa `e` como **causa**: el mensaje es para el usuario y la causa para depurar (`e.getCause()`).
- El JSON truncado llega como `JsonEOFException`, y el email inválido como `JsonMappingException`: Jackson **envuelve** la `IllegalArgumentException` del setter. Ambas son subclases de `IOException`. El `IllegalArgumentException` del `catch` cubre configuraciones de Jackson que no envuelven.

## Paso 4 — `guardar()` (R4)

```java
Path carpeta = archivo.toAbsolutePath().getParent();
if (carpeta != null) {
    Files.createDirectories(carpeta);
}
mapper.writerFor(TIPO_LISTA).withDefaultPrettyPrinter().writeValue(archivo.toFile(), contactos);
```

- `createDirectories` crea toda la ruta y **no falla** si ya existe (`createDirectory`, en singular, sí falla).
- `toAbsolutePath()` antes de `getParent()`: para `Path.of("contactos.json")`, `getParent()` es `null`.
- **`writerFor(TIPO_LISTA)`**: con `mapper.writeValue(archivo, contactos)` a secas, Jackson ve una `List` de `Object` (el tipo genérico se borra al compilar) y **omite el `tipo`**: el JSON queda `[{"nombre":"Ana",...}]` y no se puede volver a leer. Indicar el tipo de la lista lo resuelve. La prueba `elJsonEsLegibleEIncluyeElTipo` lo verifica.
- `withDefaultPrettyPrinter()`: JSON indentado y legible por personas, útil para depurar y revisar.

## Paso 5 — Las pruebas con `@TempDir`

```java
@TempDir
Path carpeta;
```

JUnit crea una carpeta temporal **nueva** para cada prueba y la borra al terminar. Las pruebas no dependen del orden, no se pisan entre sí y no ensucian `data/`.

## Verificación

```bash
mvn test                 # Tests run: 7, Failures: 0, Errors: 0
mvn compile exec:java    # los cinco escenarios del README
```

Revisa `data/contactos.json`: debe estar indentado y con `"tipo"` en cada objeto.

## Errores frecuentes

| Síntoma | Causa |
|---|---|
| `Cannot construct instance of Contacto ... abstract types` | Faltan `@JsonTypeInfo`/`@JsonSubTypes`, o el JSON no tiene `tipo`. |
| `Cannot construct instance of ContactoPersonal (no Creators, like default constructor, exist)` | Falta el constructor sin parámetros. |
| `Unrecognized field "xyz"` | El JSON tiene un atributo sin *setter* en la clase. Revisa nombres (mayúsculas) o agrega `@JsonIgnoreProperties(ignoreUnknown = true)`. |
| Al cargar aparecen `LinkedHashMap` en vez de contactos | Se usó `List.class` en vez de `TypeReference`. |
| `NoSuchFileException` al guardar | No se creó la carpeta `data/`. |
| Un atributo no aparece en el JSON | Le falta el *getter*, o el *getter* no sigue la convención (`getX()`, o `isX()` para `boolean`). |
| `InaccessibleObjectException ... does not "opens" ... to module com.fasterxml.jackson.databind` | Solo en proyectos con `module-info.java`: falta `opens`. |
