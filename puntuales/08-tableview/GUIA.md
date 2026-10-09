# Guía de resolución · 08 TableView y ObservableList

> Para comparar tu trabajo con la solución: `git diff main solucion -- puntuales/08-tableview`

## Idea central

```
Liga.equipos (ObservableList<Equipo>, con extractor)
   └─ FilteredList   ← predicado: texto + comuna
        └─ SortedList   ← comparator = el de la tabla (clic en columnas / sortOrder)
             └─ TableView.items
                  └─ cada TableColumn: cellValueFactory (QUÉ valor) + cellFactory (CÓMO se dibuja)
```

- **Los datos viven en una sola lista** (`Liga.equipos`). La tabla, los filtros y los `ComboBox` son **vistas** de esa lista: al agregar o eliminar en ella, todo se actualiza.
- **Cada celda observa una propiedad.** Cuando `ganados` cambia, la celda PG se redibuja sola (pregunta 2).

## Paso 1 — Columnas (R1)

```java
colGanados.setCellValueFactory(new PropertyValueFactory<>("ganados"));
colPuntos.setCellValueFactory(celda -> celda.getValue().puntosBinding().asObject());
```

- **Pregunta 1:** `PropertyValueFactory("golesFavor")` busca primero `golesFavorProperty()` y, si no existe, `getGolesFavor()`. Respeta mayúsculas: con `"golesfavor"` no encuentra nada y la columna queda **vacía sin error**. Solo aparece una advertencia en la consola. Si encuentra solo el *getter*, la celda muestra el valor pero **no se actualiza** cuando cambia.
- Lee los métodos por reflexión, y por eso `module-info.java` declara `opens ... to javafx.base`.
- Con un lambda se controla exactamente qué valor se muestra y se detectan los errores al compilar. `asObject()` convierte `IntegerBinding` (que es `ObservableValue<Number>`) en `ObservableValue<Integer>`, el tipo de la columna.

## Paso 2 — Orden inicial (R2)

```java
List.of(colPuntos, colDiferencia, colGolesFavor).forEach(c -> c.setSortType(TableColumn.SortType.DESCENDING));
tblEquipos.getSortOrder().setAll(List.of(colPuntos, colDiferencia, colGolesFavor));
```

`getSortOrder()` es la lista de columnas por las que se ordena, en orden de prioridad. Es el mismo mecanismo que usa el usuario con Shift+clic en los encabezados.

## Paso 3 — `FilteredList` + `SortedList` (R3)

```java
filtrados = new FilteredList<>(liga.getEquipos());
SortedList<Equipo> ordenados = new SortedList<>(filtrados);
ordenados.comparatorProperty().bind(tblEquipos.comparatorProperty());
tblEquipos.setItems(ordenados);
```

- `FilteredList` no copia datos: muestra los elementos de la original que cumplen el predicado. Cambiar el predicado recalcula el filtro.
- Sin `SortedList`, el clic en los encabezados no funciona. La tabla intenta ordenar sus `items`, pero una `FilteredList` no se puede reordenar. Al enlazar el `comparatorProperty`, la tabla le **pide** a la `SortedList` que ordene.
- **Pregunta 3:** `FilteredList` y `SortedList` son de **solo lectura**. Por eso se agrega y se elimina siempre en `liga.getEquipos()`.
- La lista de comunas se recalcula con un `ListChangeListener` sobre la lista original. `stream().map(...).distinct().sorted()` evita repetidos.

## Paso 4 — `setCellFactory` (R4)

`cellValueFactory` decide **qué** valor va en la celda y `cellFactory` decide **cómo** se dibuja.

```java
colPosicion.setCellFactory(columna -> new TableCell<>() {
    @Override
    protected void updateItem(Void item, boolean vacia) {
        super.updateItem(item, vacia);
        setText(vacia ? null : String.valueOf(getIndex() + 1));
    }
});
```

- **Pregunta 4:** la posición depende del orden y del filtro **actuales**. Si se guardara en `Equipo`, habría que recalcularla en cada cambio. En la celda, `getIndex()` siempre es correcto.
- La columna DIF quita las clases `dif-positiva` y `dif-negativa` **antes** de agregar la que corresponde. Las celdas se **reutilizan**: sin esa limpieza, una celda que mostró `+2` en verde y ahora muestra `-6` quedaría con ambas clases.
- `if (vacia || dif == null) { setText(null); return; }` evita textos fantasma en las filas vacías.

## Paso 5 — Registrar resultado (R5)

```java
liga.registrarResultado(cmbLocal.getValue(), cmbVisita.getValue(), golesLocal, golesVisita);
```

El controlador solo convierte texto a número. Las reglas (equipo contra sí mismo, goles negativos) están en `Liga` y llegan como `IllegalArgumentException`. Ni `refresh()` ni `setItems()`: `Equipo.registrarPartido` modifica propiedades, y las celdas que las observan se redibujan.

Los `ComboBox` usan `setItems(liga.getEquipos())` y muestran el `toString()` de `Equipo`. Un equipo nuevo aparece en ellos al instante.

## Paso 6 — Agregar y eliminar (R6)

Ambas operaciones van a `liga`. Como la `FilteredList` observa la lista original, la tabla, el contador y los `ComboBox` se actualizan sin más código.

## Paso 7 — Extractor (R7)

Una `ObservableList` normal avisa cuando **se agregan, quitan o reemplazan** elementos, pero **no** cuando cambia una propiedad **dentro** de un elemento. Las celdas se actualizan porque cada una observa su propia propiedad. La `SortedList`, en cambio, no se entera y no reordena.

```java
private final ObservableList<Equipo> equipos = FXCollections.observableArrayList(equipo -> new Observable[]{
        equipo.puntosBinding(), equipo.diferenciaBinding(), equipo.golesFavorProperty(),
        equipo.nombreProperty(), equipo.comunaProperty()});
```

El **extractor** le dice a la lista qué observar de cada elemento. Cuando una de esas propiedades cambia, la lista emite un evento de **actualización** y la `SortedList` reposiciona el elemento. Se incluyen el nombre y la comuna para que el filtro también reaccione si se editan.

## Verificación

1. Al abrir, el orden es el del README y DIF aparece con signo y color.
2. Comuna `Pirque` → `Mostrando 2 de 6 equipos`. Buscar `uni` → 1 equipo.
3. Registra `Juventud Las Vizcachas 5 - 0 Club Los Quillayes` → Juventud sube al 2.º lugar sin hacer clic.
4. El mismo equipo como local y visita, o goles `x` → `Alert`.
5. Agrega `Halcones de Pirque` → `Mostrando 7 de 7`. Agregarlo otra vez → `Ya existe un equipo...`.

## Errores frecuentes

| Síntoma | Causa |
|---|---|
| Columna vacía | Nombre mal escrito en `PropertyValueFactory`, o falta `opens ... to javafx.base`. |
| El clic en los encabezados no ordena | Falta `SortedList` con `comparatorProperty().bind(...)`. |
| `UnsupportedOperationException` al agregar | Se agregó a `tblEquipos.getItems()`, que es la `SortedList`. |
| Colores "pegados" en filas que no corresponden | La celda no quita las clases CSS antes de agregar la nueva. |
| La tabla se actualiza pero no se reordena | Falta el extractor (R7). |
| `incompatible types: IntegerBinding cannot be converted to ObservableValue<Integer>` | Falta `.asObject()`. |
