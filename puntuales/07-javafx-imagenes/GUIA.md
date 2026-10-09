# Guía de resolución · 07 JavaFX Images

> Para comparar tu trabajo con la solución: `git diff main solucion -- puntuales/07-javafx-imagenes`

## Idea central

| Clase | Qué es | Analogía |
|---|---|---|
| `Image` | Los **píxeles** decodificados de un archivo. No es un nodo y no se ve por sí sola. | La foto impresa. |
| `ImageView` | Un **nodo** que dibuja una `Image` con cierto tamaño, recorte o rotación. | El marco donde se pone la foto. |

**Pregunta 3:** una misma `Image` puede mostrarse en muchos `ImageView` a la vez. La solución lo aprovecha: la miniatura de la lista y la vista grande comparten el objeto, guardado en un `Map` que sirve de caché.

## Paso 1 — Recursos con `getResource` (R1)

```java
URL url = getClass().getResource("/cl/dsy1102/ejemplos/imagenes/img/" + archivo);
Image imagen = new Image(url.toExternalForm());
```

- Maven copia `src/main/resources` a `target/classes`, y luego al JAR. Desde ahí la imagen está **en el classpath**, junto a las clases.
- La ruta que empieza con `/` es absoluta desde la raíz del classpath. Sin `/`, se resuelve relativa al paquete de la clase. Por eso `AppParques` puede usar solo `"img/icono.png"`.
- `toExternalForm()` convierte la URL en el texto que espera `Image`: `file:/...` en el IDE y `jar:file:/...!/...` dentro de un JAR.

**Pregunta 1:** `"src/main/resources/..."` es una ruta relativa a la carpeta desde donde se ejecuta el programa. En el IDE coincide con el proyecto, pero en un JAR distribuido esa carpeta no existe.

## Paso 2 — Ajuste sin deformar (R2)

```java
imgParque.setPreserveRatio(true);
imgParque.fitWidthProperty().bind(marcoImagen.widthProperty().subtract(20));
imgParque.fitHeightProperty().bind(marcoImagen.heightProperty().subtract(20));
```

- `ImageView` **no es un layout**: no crece solo. `fitWidth` y `fitHeight` definen la caja máxima, y con `preserveRatio` la imagen ocupa el mayor tamaño posible dentro de ella sin deformarse.
- Se restan 20 px por el `-fx-padding: 10` del marco (10 por lado).
- En el FXML, el `StackPane` tiene `minWidth="0" minHeight="0"`. Sin eso, el tamaño de la imagen fija el mínimo del marco y la ventana **no puede achicarse**: la imagen empuja al marco y el marco a la imagen.

## Paso 3 — Imagen de reemplazo (R3)

```java
if (url == null) {
    System.err.println("No se encontro la imagen " + archivo + ", se usa la de reemplazo.");
    url = getClass().getResource(RUTA_IMAGENES + SIN_IMAGEN);
}
```

**Pregunta 2:** sin la comprobación, `url.toExternalForm()` lanza `NullPointerException` y, como ocurre dentro de `initialize()`, la vista completa no carga (`LoadException`).

> **Cuidado con `computeIfAbsent`:** una versión recursiva como `cache.computeIfAbsent(a, x -> cargarImagen(SIN_IMAGEN))` modifica el mapa mientras lo está modificando, y Java lanza `ConcurrentModificationException`. Por eso la solución usa `get` y `put` explícitos.

## Paso 4 — Miniaturas con `setCellFactory` (R4)

```java
lstParques.setCellFactory(lista -> new ListCell<>() {
    private final ImageView miniatura = crearMiniatura();

    @Override
    protected void updateItem(Parque parque, boolean vacia) {
        super.updateItem(parque, vacia);
        if (vacia || parque == null) {
            setText(null);
            setGraphic(null);
        } else {
            miniatura.setImage(cargarImagen(parque.getArchivoImagen()));
            setText(parque.getNombre());
            setGraphic(miniatura);
        }
    }
});
```

- El `ListView` crea solo las celdas **visibles** y las **reutiliza** al desplazarse. `updateItem` se llama cada vez que una celda recibe otro elemento.
- Siempre hay que limpiar la celda cuando está vacía. Si no, al desplazarse aparecen miniaturas "fantasma" en filas vacías.
- El `ImageView` de la celda se crea **una vez por celda**, no en cada `updateItem`.
- La caché evita decodificar el PNG cada vez que una celda se reutiliza.

## Paso 5 — `FileChooser` y carga en segundo plano (R5)

```java
File archivo = selector.showOpenDialog(btnCargar.getScene().getWindow());
if (archivo == null) return;                                  // canceló

Image imagen = new Image(archivo.toURI().toString(), true);   // true = segundo plano
prgCarga.progressProperty().bind(imagen.progressProperty());
prgCarga.visibleProperty().bind(imagen.progressProperty().lessThan(1));
imagen.errorProperty().addListener((obs, antes, hayError) -> { if (hayError) informarError(...); });
```

- `showOpenDialog` recibe la ventana dueña: el diálogo queda encima de ella y la bloquea.
- `archivo.toURI().toString()` produce `file:/home/...`. Sirve con espacios y tildes en la ruta. Concatenar `"file:" + ruta` falla con esos caracteres.
- Con `backgroundLoading = true`, el constructor retorna de inmediato y la imagen se decodifica en otro hilo. `progressProperty` avanza de 0 a 1 y los *bindings* muestran y ocultan el indicador sin código adicional.
- **Pregunta 4:** como la carga puede ser asíncrona, una excepción no tendría a quién llegar. Por eso `Image` informa los fallos con `errorProperty()` y `getException()`. La solución revisa ambos casos: el error inmediato (`isError()`) y el error posterior (listener).
- Antes de volver a usar `setVisible`, se llama a `unbind()`. Una propiedad enlazada no acepta `set`.

## Paso 6 — Ícono (R6)

```java
stage.getIcons().add(new Image(AppParques.class.getResource("img/icono.png").toExternalForm()));
```

`getIcons()` es una lista. Se pueden agregar varios tamaños (16, 32, 64, 128) y el sistema operativo elige el más adecuado.

## Verificación

1. Al iniciar, Torres del Paine aparece seleccionado con su imagen y la lista muestra miniaturas.
2. Al seleccionar *Pan de Azúcar* aparece la imagen de reemplazo y en la consola `No se encontro la imagen pan-de-azucar.png...`.
3. Al agrandar y achicar la ventana, la imagen se ajusta sin deformarse.
4. *Cargar imagen propia…* con una foto grande muestra el indicador y luego la imagen. Con un `.txt` renombrado a `.png` aparece el `Alert` de error.
5. *Restaurar* vuelve a la imagen del parque.

## Errores frecuentes

| Síntoma | Causa |
|---|---|
| `NullPointerException` en `getResource(...).toExternalForm()` | La ruta no coincide: mayúsculas, falta la `/` inicial o la carpeta no está bajo `resources`. Revisa `target/classes`. |
| `IllegalArgumentException: Invalid URL or resource not found` | Se pasó una ruta de archivo (`"img/lauca.png"`) directo a `new Image(...)` en vez de la URL. |
| La imagen se ve estirada | Falta `setPreserveRatio(true)`. |
| La ventana no se puede achicar | El contenedor de la imagen no tiene `minWidth/minHeight = 0`. |
| La imagen nueva no aparece en el JAR | Se agregó en `src/main/java` en vez de `src/main/resources`. |
| `A bound value cannot be set` | `setVisible` sobre `prgCarga` sin `unbind()` previo. |
