# Guía de resolución · Caso completo 02 Biblioteca Gabriela Mistral

Esta guía acompaña la rama `solucion`. La arquitectura es la misma de la Fonda y del caso completo 01. Aquí se detallan sobre todo las **diferencias** y los puntos donde es fácil equivocarse.

> Comparar tu avance: `git diff main solucion -- completos/02-biblioteca`

---

## 0. Mapa de requisitos

| Requisito | Dónde se cumple |
|---|---|
| R1 Proyecto y ciclo de vida | `pom.xml`, `module-info.java`, `AppFX` |
| R2 Modelo para JSON y tabla | `model/Material`, `Libro`, `Revista` |
| R3 Vista principal | `principal-view.fxml`, `PrincipalController` |
| R4 Formulario | `formulario-view.fxml`, `FormularioController` |
| R5 Préstamo | `prestamo-view.fxml`, `PrestamoController`, `Material.prestar` |
| R6 Devolución | `PrincipalController.onDevolver`, `Material.devolver` |
| R7 Persistencia | `dao/*`, `repository/*` |
| R8 Validación y navegación | Controladores, `Alertas`, `Navegador` |

```
Vista (FXML) → Controlador → Repository (interfaz) → MaterialRepository → MaterialDao (interfaz) → JsonMaterialDao → data/materiales.json
                    └──────────→ Modelo (prestar, devolver, copiar, ConsultaEnSala) ←──────────┘
```

---

## Paso 1 — Ciclo de vida (R1)

Idéntico al caso 01. `AppFX` arma `new MaterialRepository(new JsonMaterialDao(Path.of("data", "materiales.json")))`, muestra la vista principal, le entrega el repositorio y luego carga los datos.

## Paso 2 — Modelo y Jackson (R2)

```java
@JsonIgnoreProperties({"disponibles", "diasMaximos"})
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "tipo")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Libro.class, name = "LIBRO"),
        @JsonSubTypes.Type(value = Revista.class, name = "REVISTA")
})
public abstract class Material {
    @JsonProperty("prestados")
    private int prestados;
    protected Material() { }
```

### La trampa de los valores calculados

Jackson serializa **todo getter público**: `getDisponibles()` y `getDiasMaximos()` también. Sin `@JsonIgnoreProperties`, el archivo queda así:

```json
{ "tipo" : "LIBRO", ..., "diasMaximos" : 14, "disponibles" : 2, "prestados" : 0 }
```

Guardar funciona. Pero **al reiniciar**, Jackson busca un *setter* para `diasMaximos`, no lo encuentra y falla:

```
UnrecognizedPropertyException: Unrecognized field "diasMaximos" (class ...Libro), not marked as ignorable
```

La aplicación muestra el `Alert` de archivo dañado y **pierde todos los datos** al siguiente guardado. Por eso la prueba 6 revisa el contenido del archivo. Hay tres formas de resolverlo:

| Opción | Cuándo |
|---|---|
| `@JsonIgnoreProperties({...})` en la clase (la solución) | Se ven juntas todas las propiedades excluidas. |
| `@JsonIgnore` sobre cada *getter* calculado | Cuando son pocas. |
| Renombrar a `calcularDisponibles()` | Si no se necesita como columna con `PropertyValueFactory` (que exige `getX`). |

Además, guardar un valor derivado es **redundante**: si alguien edita el JSON y pone `disponibles: 5` con `ejemplares: 2`, ¿cuál vale?

### Campos sin *setter*

`prestados` (y `soloSala` en `Revista`) solo cambian mediante métodos con reglas: `prestar`, `devolver` y `restringirASala`. No se agrega un *setter* público, porque permitiría saltarse esas reglas. Jackson igual puede restaurarlos: como existe el *getter* (`getPrestados()`, `isSoloSala()`), escribe directamente en el campo privado. `@JsonProperty` en el campo deja esa decisión explícita y es **obligatorio** si el *getter* no sigue la convención (ver el caso 01).

### Validación cruzada

```java
public void setEjemplares(int ejemplares) {
    ...
    if (ejemplares < prestados) throw new IllegalArgumentException("Hay 2 ejemplares prestados: no puedes dejar menos.");
}
```

Al editar se trabaja sobre `original.copiar()`, que ya trae `prestados`, así que esta regla funciona sin código extra en el controlador (prueba 5).

> **Orden en el JSON:** Jackson escribe `prestados` al final porque es un campo anotado (después de las propiedades con *getter*). Al leer, `setEjemplares` se ejecuta **antes** de asignar `prestados` (que todavía vale 0), así que la validación cruzada no molesta al cargar.

## Paso 3 — Vista principal (R3)

### Columna *Disponibles*: valor numérico, texto compuesto

```java
colDisponibles.setCellValueFactory(new PropertyValueFactory<>("disponibles"));   // Integer: ordena bien
colDisponibles.setCellFactory(columna -> new TableCell<>() {
    @Override
    protected void updateItem(Integer disponibles, boolean vacia) {
        super.updateItem(disponibles, vacia);
        getStyleClass().remove("agotado");
        Material material = vacia || getTableRow() == null ? null : getTableRow().getItem();
        ...
        setText(disponibles + " de " + material.getEjemplares());
        if (disponibles == 0) getStyleClass().add("agotado");
    }
});
```

- El **valor** de la celda es un `Integer`. Así el clic en el encabezado ordena numéricamente. Si fuera el texto `"2 de 3"`, se ordenaría alfabéticamente.
- El **texto** se compone con datos de la fila (`getTableRow().getItem()`). Este patrón sirve siempre que una celda necesita más de un dato.
- `PropertyValueFactory("disponibles")` encuentra `getDisponibles()` aunque no exista un atributo con ese nombre: solo busca el *getter*.

### Filtro combinado

```java
filtrados.setPredicate(material ->
        (material.getTitulo().toLowerCase().contains(texto) || material.getCodigo().toLowerCase().contains(texto))
                && (TODOS.equals(tipo) || material.obtenerTipo().equals(tipo)));
```

Los dos *listeners* (texto y `ComboBox`) llaman al mismo `actualizarFiltro()`, que lee **ambos** controles. Si cada *listener* armara su propio predicado, el último borraría el filtro del otro.

### *Préstamo* por la interfaz

```java
if (material instanceof ConsultaEnSala sala && sala.isSoloSala()) return "Solo sala";
return "Hasta " + material.getDiasMaximos() + " días";
```

## Paso 4 — Formulario (R4)

Igual que el caso 01, con dos detalles:

- `chkSoloSala.setDisable(revista.isSoloSala())`: la interfaz `ConsultaEnSala` solo ofrece `restringirASala()`. La interfaz gráfica no debe ofrecer una acción que el modelo no permite.
- Al crear una revista restringida, se usa el constructor y luego `restringirASala()`. No hay un constructor con `soloSala`, para que exista **una sola** forma de restringir.

## Paso 5 — Préstamo con `Spinner` (R5)

```java
spnDias.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
        1, material.getDiasMaximos(), material.getDiasMaximos()));
```

- El máximo sale del modelo por **polimorfismo**: el controlador no tiene un `if (material instanceof Libro) 14 else 3`.
- El `Spinner` impide elegir días fuera de rango, pero `prestar` vuelve a validar. El modelo no confía en la interfaz gráfica.
- Tras un préstamo exitoso, `material = copia` y se refresca la ficha. Así se pueden registrar varios préstamos seguidos sobre el estado actualizado.

## Paso 6 — Devolución con `TextInputDialog` (R6)

```java
TextInputDialog dialogo = new TextInputDialog("0");
Optional<String> respuesta = dialogo.showAndWait();
if (respuesta.isEmpty()) return;                 // canceló
int atraso = Integer.parseInt(respuesta.get().trim());   // NumberFormatException → Alert
Material copia = seleccionado.copiar();
int multa = copia.devolver(atraso);              // IllegalArgumentException (negativo) o PrestamoRechazadoException
repositorio.actualizar(seleccionado, copia);
```

- `TextInputDialog` es un `Dialog<String>`. `showAndWait()` retorna un `Optional` vacío si el usuario cancela o cierra el diálogo.
- Hay tres excepciones distintas con tres significados: dato mal escrito (controlador), atraso negativo (argumento inválido) y "no hay ejemplares prestados" (regla de negocio). Cada una tiene su mensaje.

## Paso 7 — Persistencia (R7)

`JsonMaterialDao` y `MaterialRepository` son los del caso 01 con otros nombres. Copia de la lista antes de guardar, `createDirectories`, `writerFor(TypeReference)` y traducción a `PersistenciaException`. `agregar` rechaza códigos repetidos con `IllegalArgumentException`.

## Paso 8 — Validación y navegación (R8)

| Qué | Dónde | Cómo se informa |
|---|---|---|
| Campos vacíos, números mal escritos, tipo sin elegir | `FormularioController` | Un `Alert` con todos los errores y campos en rojo. |
| Código, año, ejemplares, páginas, número | Setters del modelo | `IllegalArgumentException` → `Alert`. |
| Ejemplares por debajo de los prestados | `Material.setEjemplares` | `IllegalArgumentException` → `Alert`. |
| Lector, días, ejemplares disponibles, sala | `Material.prestar` | `PrestamoRechazadoException` → texto rojo. |
| Atraso negativo o sin préstamos | `Material.devolver` | `Alert`. |
| Archivo | `JsonMaterialDao` | `PersistenciaException` → `Alert`. |

---

## Verificación

| Prueba | Resultado en la solución |
|---|---|
| Año `2999` | `El año debe estar entre 1450 y 2026.` |
| Páginas `cien` | `El campo Páginas debe ser un número entero.` |
| Prestar sin lector | `Préstamo rechazado: indica el nombre del lector.` |
| Tercer préstamo de *Desolación* | `no quedan ejemplares disponibles de "Desolación".` |
| Prestar *Anales* | `"Anales de la Universidad de Chile" es de consulta en sala.` |
| Devolver con 3 días | `Multa a pagar: $600 (3 días de atraso).` |
| Dejar 1 ejemplar con 2 prestados | `Hay 2 ejemplares prestados: no puedes dejar menos.` |
| Reiniciar | Se conservan los préstamos, y el JSON no contiene `disponibles` ni `diasMaximos`. |

## Errores frecuentes

| Síntoma | Causa |
|---|---|
| La segunda vez que se abre la aplicación aparece "archivo dañado" | Valores calculados en el JSON (ver paso 2). |
| La columna *Disponibles* ordena 10 antes que 2 | El valor de la celda es `String` en vez de `Integer`. |
| Elegir *Revista* borra la búsqueda por texto | Cada *listener* arma su propio predicado. |
| `NullPointerException` en la celda *Disponibles* | No se comprueba `getTableRow() == null` en celdas vacías. |
| Después de editar, los préstamos vuelven a 0 | Se creó un objeto nuevo en vez de editar `original.copiar()`. |
| La revista "solo sala" vuelve a prestarse al reiniciar | `soloSala` no llega al JSON: el *getter* no sigue la convención (por ejemplo, `esSoloSala()`) y el campo no tiene `@JsonProperty`. |
