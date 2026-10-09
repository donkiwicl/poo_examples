# Guía de resolución · Caso completo 03 Clínica Veterinaria Patitas del Sur

Esta guía acompaña la rama `solucion`. La arquitectura es la misma de la Fonda y de los casos completos 01 y 02. Aquí se explica sobre todo lo **nuevo**: enums con datos, composición (historial anidado), fechas y la vista maestro-detalle.

> Comparar tu avance: `git diff main solucion -- completos/03-veterinaria`

---

## 0. Mapa de requisitos

| Requisito | Dónde se cumple |
|---|---|
| R1 Proyecto y ciclo de vida | `pom.xml` (`jackson-datatype-jsr310`), `module-info.java`, `AppFX` |
| R2 Modelo para JSON | `model/Paciente`, `Perro`, `Gato`, `Exotico`, `Atencion` |
| R3 Vista principal | `principal-view.fxml`, `PrincipalController` |
| R4 Formulario | `formulario-view.fxml`, `FormularioController` |
| R5 Ficha clínica | `ficha-view.fxml`, `FichaController`, `Paciente.registrarAtencion` |
| R6 Persistencia | `dao/JsonPacienteDao` (`JavaTimeModule`), `repository/*` |
| R7 Validación y navegación | Controladores, `Alertas`, `Navegador` |

```
Vista (FXML) → Controlador → Repository (interfaz) → PacienteRepository → PacienteDao (interfaz) → JsonPacienteDao → data/pacientes.json
                    └──────────→ Modelo (registrarAtencion, copiar, Vacunable, enums) ←──────────┘
```

---

## Paso 1 — Fechas en Maven y en módulos (R1)

`LocalDate` (paquete `java.time`) **no** es un *bean*: no tiene constructor vacío ni *setters*. Jackson no sabe convertirlo sin ayuda:

```
InvalidDefinitionException: Java 8 date/time type `java.time.LocalDate` not supported by default:
add Module "com.fasterxml.jackson.datatype:jackson-datatype-jsr310" to enable handling
```

Se resuelve en tres lugares:

| Dónde | Qué |
|---|---|
| `pom.xml` | Dependencia `com.fasterxml.jackson.datatype:jackson-datatype-jsr310` (misma versión que `jackson-databind`). |
| `module-info.java` | `requires com.fasterxml.jackson.datatype.jsr310;` |
| `JsonPacienteDao` | `mapper.registerModule(new JavaTimeModule())` |

## Paso 2 — Modelo (R2)

### Enums con atributos

```java
public enum TipoAtencion {
    CONSULTA("Consulta general", 15_000),
    VACUNACION("Vacunación", 12_000),
    ...;
    private final String nombre;
    private final int precioBase;
    TipoAtencion(String nombre, int precioBase) { ... }
    @Override public String toString() { return nombre; }
}
```

- Un `enum` es una clase con un número **fijo** de instancias. Cada constante puede llevar datos (nombre visible y precio) y métodos.
- Así el precio de la vacunación está en **un solo lugar**. Sin el enum habría `if (tipo.equals("Vacunación")) precio = 12000` repartidos por el código.
- `toString()` es lo que muestran `ComboBox` y `TableView`: `cmbTipoAtencion.getItems().setAll(TipoAtencion.values())` basta para llenar el combo.
- **Pregunta del R2:** Jackson guarda los enums por su **nombre** (`"VACUNACION"`, `"GRANDE"`) y los lee con `valueOf`, sin configuración. Por eso nunca se renombra una constante: los archivos antiguos dejarían de leerse.

### Composición: el historial vive dentro del paciente

```java
@JsonProperty("atenciones")
private List<Atencion> atenciones = new ArrayList<>();

public List<Atencion> getAtenciones() {
    return Collections.unmodifiableList(atenciones);   // vista de solo lectura
}
```

- Las atenciones **pertenecen** al paciente: si se elimina el paciente, se elimina su historial. En el diagrama es un rombo negro (`*--`). En el JSON queda anidado, como un arreglo dentro de cada objeto.
- `getAtenciones()` retorna una lista **no modificable**. Si alguien intenta `paciente.getAtenciones().add(...)`, obtiene `UnsupportedOperationException`. La única forma de agregar es `registrarAtencion`, que aplica las reglas.
- Jackson **no** usa ese *getter* para leer, porque tiene `@JsonProperty` en el campo y escribe directamente en la `ArrayList` privada.
- `Atencion` es **inmutable** (sin *setters*): una atención registrada no se edita. Jackson la reconstruye con el constructor `protected Atencion()` y llenando los campos, porque cada uno tiene su *getter*.
- `copiarEn` crea `new ArrayList<>(atenciones)`. Si la copia compartiera la **misma** lista con el original, registrar una atención en la copia también la agregaría al original, aunque el guardado fallara. Como las atenciones son inmutables, basta con copiar la lista y no cada atención.

### Valores calculados: la trampa de la interfaz

```java
@JsonIgnoreProperties({"edadAnios", "ultimaAtencion", "pesoActual", "totalAtenciones", "diasEntreVacunas"})
```

`getDiasEntreVacunas()` viene de la interfaz `Vacunable` y Jackson lo ve como un *getter* más de `Perro` y `Gato`. Sin ignorarlo, el JSON incluye `"diasEntreVacunas" : 21` y al reiniciar falla con `UnrecognizedPropertyException`, el mismo problema del caso 02. Como `@JsonIgnoreProperties` está en `Paciente`, se hereda en las tres subclases.

### La regla de vacunación usa la interfaz

```java
if (!(this instanceof Vacunable vacunable)) {
    throw new AtencionRechazadaException(nombre + " (" + obtenerEspecie() + ") no se vacuna en esta clínica.");
}
... if (dias < vacunable.getDiasEntreVacunas()) ...
```

El plazo entre vacunas lo define cada clase (21 o 28 días). El exótico, al no implementar `Vacunable`, queda excluido sin un `if (especie.equals("Conejo"))`.

## Paso 3 — Vista principal (R3)

### Columna de fecha que ordena bien

```java
colUltimaAtencion.setCellValueFactory(celda -> {
    Atencion ultima = celda.getValue().getUltimaAtencion();
    return new ReadOnlyObjectWrapper<>(ultima == null ? null : ultima.getFecha());   // valor: LocalDate
});
colUltimaAtencion.setCellFactory(columna -> new TableCell<>() {
    ... setText(fecha == null ? "Sin atenciones" : fecha.format(Atencion.FORMATO_FECHA));   // texto
});
```

Si el valor fuera el texto `"08-10-2026"`, el orden sería alfabético: `01-12-2026` quedaría antes que `08-10-2026`. Con `LocalDate` como valor, el orden es cronológico y el formato es solo presentación. Es el mismo patrón de *Disponibles* del caso 02.

### Filtro de especie con un grupo

```java
return EXOTICOS.equals(especie) ? paciente instanceof Exotico : paciente.obtenerEspecie().equals(especie);
```

`obtenerEspecie()` de un exótico retorna `"Conejo"`, `"Hurón"` o `"Ave"`. La opción *Exóticos* los agrupa preguntando por la **clase**. Es uno de los pocos `instanceof` de clase concreta en la solución, y se justifica porque la categoría "exótico" **es** la clase.

## Paso 4 — Formulario y `DatePicker` (R4)

```java
dpNacimiento.setConverter(new LocalDateStringConverter(Atencion.FORMATO_FECHA, Atencion.FORMATO_FECHA));
dpNacimiento.setDayCellFactory(selector -> new DateCell() {
    @Override
    public void updateItem(LocalDate fecha, boolean vacia) {
        super.updateItem(fecha, vacia);
        setDisable(vacia || fecha.isAfter(LocalDate.now()));
    }
});
```

- `getValue()` del `DatePicker` es un `LocalDate`, así que no hace falta convertir texto.
- **Formato:** por defecto depende del sistema operativo (`8/10/2026` en un equipo en inglés). El `LocalDateStringConverter` fija `dd-MM-yyyy` para toda la aplicación.
- **`setDayCellFactory`** es el mismo concepto de las celdas de `TableView` y `ListView`, aplicado a los días del calendario. Deshabilitar los días futuros evita el error antes de que ocurra.
- Igual se puede **escribir** una fecha futura a mano. Por eso el modelo vuelve a validar: la interfaz ayuda y el modelo protege.
- Si el texto escrito no es una fecha válida, `getValue()` queda en `null` y el controlador lo informa como campo obligatorio.
- `cmbTamano.getItems().setAll(Tamano.values())`: un `ComboBox<Tamano>` trabaja con el enum directamente y no requiere conversión.

## Paso 5 — Ficha clínica maestro-detalle (R5)

```java
private final ObservableList<Atencion> historial = FXCollections.observableArrayList();
...
tblAtenciones.setItems(historial);
colFecha.setSortType(TableColumn.SortType.DESCENDING);
tblAtenciones.getSortOrder().add(colFecha);
```

- **Maestro** (el paciente, arriba) y **detalle** (sus atenciones, en la tabla). El detalle se recarga desde el maestro con `historial.setAll(paciente.getAtenciones())`.
- La lista de la tabla es **del controlador**, no del repositorio. El repositorio guarda pacientes, y las atenciones viajan dentro de ellos.

**Registrar con copia:**

```java
Paciente copia = paciente.copiar();
String mensaje = copia.registrarAtencion(dpFecha.getValue(), cmbTipoAtencion.getValue(), peso, txtObservacion.getText());
repositorio.actualizar(paciente, copia);   // guarda la lista de pacientes completa, con historiales
mostrar(copia);                            // maestro y detalle desde la copia ya guardada
```

- Hay tres tipos de error con mensajes distintos: `NumberFormatException` (peso mal escrito, en el controlador), `IllegalArgumentException` (peso fuera de rango, del constructor de `Atencion`) y `AtencionRechazadaException` (regla clínica).
- El costo estimado (`paciente.calcularCosto(tipo)`) se recalcula con un *listener* del `ComboBox`. Es polimorfismo: el mismo servicio cuesta distinto a un perro grande y a un conejo.
- El peso aceptado en `4,2` se convierte con `replace(',', '.')` antes de `Double.parseDouble`.

## Paso 6 — Persistencia con fechas (R6)

```java
private final ObjectMapper mapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
```

| Configuración | `LocalDate.of(2026, 10, 8)` se escribe como |
|---|---|
| Sin módulo | `InvalidDefinitionException` |
| Solo con `JavaTimeModule` | `[2026,10,8]` |
| Con módulo y sin `WRITE_DATES_AS_TIMESTAMPS` | `"2026-10-08"` |

La tercera opción es legible y es el formato estándar ISO-8601.

El resto (copia de la lista antes de guardar, `createDirectories`, `writerFor(TypeReference)`, `PersistenciaException`) es igual a los casos anteriores. `PacienteRepository.agregar` rechaza un paciente con el mismo nombre **y** el mismo tutor, porque no hay un código único.

## Paso 7 — Validación y navegación (R7)

| Qué | Dónde | Cómo se informa |
|---|---|---|
| Campos vacíos, fecha sin elegir, tipo, tamaño o especie | `FormularioController` | Un `Alert` con todos los errores y los campos en rojo. |
| Teléfono, fecha de nacimiento (futura o de hace más de 40 años) | Setters de `Paciente` | `IllegalArgumentException` → `Alert`. |
| Paciente repetido para el mismo tutor | `PacienteRepository.agregar` | `IllegalArgumentException` → `Alert`. |
| Peso mal escrito | `FichaController` | Texto rojo. |
| Peso fuera de rango, observación larga | Constructor de `Atencion` | Texto rojo. |
| Fecha futura o anterior al nacimiento, vacunas | `Paciente.registrarAtencion` | `AtencionRechazadaException` → texto rojo. |
| Archivo | `JsonPacienteDao` | `PersistenciaException` → `Alert`. |

---

## Verificación

| Prueba | Resultado en la solución |
|---|---|
| Vacuna de Rocky hace 10 días | `Atención registrada: Vacunación para Rocky \| Costo: $18.000` |
| Otra vacuna hoy | `ya tiene una vacuna el ...; deben pasar 21 días entre vacunas.` |
| Consulta de Rocky | `$22.500` (15.000 × 1,5) |
| Vacuna de Copito | `Copito (Conejo) no se vacuna en esta clínica.` |
| Desparasitación de Copito | `$11.700` (9.000 × 1,3) |
| Fecha de mañana | `la fecha no puede ser futura.` |
| Reiniciar | El historial está completo, la atención más reciente aparece primero y las fechas son ISO en el JSON. |

## Errores frecuentes

| Síntoma | Causa |
|---|---|
| `Java 8 date/time type java.time.LocalDate not supported by default` | Falta registrar `JavaTimeModule` (o la dependencia en el `pom.xml`). |
| `module not found: com.fasterxml.jackson.datatype.jsr310` | La dependencia no está en el `pom.xml` o falta recargar el proyecto Maven en el IDE. |
| Fechas `[2026,10,8]` en el JSON | Falta `disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)`. |
| "Archivo dañado" al reiniciar, `Unrecognized field "diasEntreVacunas"` | Falta ignorar el *getter* que viene de la interfaz. |
| Al reiniciar los pacientes no tienen historial | `atenciones` no llega al JSON: no hay `@JsonProperty` ni un *getter* convencional. |
| Registrar una atención que falla al guardar igual aparece en la ficha | La copia comparte la misma lista que el original (falta `new ArrayList<>(...)` en `copiarEn`). |
| `UnsupportedOperationException` al agregar una atención | Se usó `getAtenciones().add(...)`. Usa `registrarAtencion`. |
| La última atención ordena mal | La columna tiene `String` como valor en vez de `LocalDate`. |
| El `DatePicker` muestra `8/10/2026` | Falta el `LocalDateStringConverter`. |
