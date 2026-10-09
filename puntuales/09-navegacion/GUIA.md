# Guía de resolución · 09 Navegación entre vistas

> Para comparar tu trabajo con la solución: `git diff main solucion -- puntuales/09-navegacion`

## Idea central

Cada vista FXML tiene su **propio** controlador, y `FXMLLoader` crea uno nuevo cada vez que la vista se carga. Los controladores no se conocen entre sí ni guardan datos entre una navegación y otra. Por eso la navegación tiene tres piezas:

| Pieza | Responsabilidad |
|---|---|
| `Navegador` | Cargar el FXML, ponerlo en la ventana y **retornar el controlador** nuevo. |
| `inicializar(...)` de cada controlador | **Recibir** los datos que la vista necesita. |
| `Pedido` | El **estado compartido**. Se crea una sola vez y viaja de controlador en controlador. |

```java
DetalleController detalle = Navegador.navegar("detalle-view.fxml", plato.getNombre());
detalle.inicializar(pedido, plato);     // paso de parámetros
```

## Paso 1 — Carta → Detalle → Carta (R1)

En la carta:

```java
Plato plato = lstPlatos.getSelectionModel().getSelectedItem();
if (plato == null) { /* Alert */ return; }
DetalleController detalle = Navegador.navegar("detalle-view.fxml", plato.getNombre());
detalle.inicializar(pedido, plato);
```

En el detalle:

```java
spnCantidad.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Pedido.MAXIMO_POR_PLATO, 1));
spnCantidad.valueProperty().addListener((obs, antes, cantidad) -> actualizarSubtotal());
```

- La configuración del `Spinner` va en `inicializar` y no en `initialize()`, porque el subtotal necesita el **plato**, que todavía no existe cuando FXML llama a `initialize()`.
- El `Spinner` solo permite valores válidos. Aun así, `pedido.agregar` valida el máximo acumulado (8 + 5 > 10), y ese error se muestra con un `Alert`.

**Volver a la carta:**

```java
CartaController carta = Navegador.navegar("carta-view.fxml", "Picada La Tía Rosa");
carta.inicializar(pedido);
carta.mostrarMensaje("Agregaste " + cantidad + " x " + plato.getNombre() + ".");
```

**Pregunta 1:** el `CartaController` es nuevo, pero recibe **el mismo objeto** `Pedido`, porque Java pasa la referencia. Por eso la cabecera muestra el total actualizado. Si no se llama a `inicializar`, `pedido` queda en `null` y el siguiente clic lanza `NullPointerException`.

## Paso 2 — Doble clic (R2)

```java
lstPlatos.setOnMouseClicked(evento -> {
    if (evento.getClickCount() == 2 && lstPlatos.getSelectionModel().getSelectedItem() != null) {
        onVerDetalle();
    }
});
```

Se **reutiliza** el mismo método del botón, así no hay dos caminos con lógica distinta.

## Paso 3 — Vista del pedido (R3)

```java
lstLineas.setItems(pedido.getLineas());
btnConfirmar.setDisable(pedido.getLineas().isEmpty());
```

Es preferible deshabilitar el botón a mostrar un error después de que el usuario lo presiona.

## Paso 4 — Ventana modal (R4)

```java
Stage modal = new Stage();
modal.initOwner(stage);                      // dueña: se centra sobre la principal y se minimiza con ella
modal.initModality(Modality.WINDOW_MODAL);   // bloquea a su dueña
modal.setScene(new Scene(raiz));
modal.showAndWait();                         // ← el código se detiene AQUÍ
return controlador;
```

- **`show()` vs `showAndWait()`:** `show()` retorna de inmediato. `showAndWait()` **no retorna hasta que la ventana se cierra** (pregunta 3). Así se puede leer el resultado en la línea siguiente:

```java
ConfirmacionController confirmacion = Navegador.abrirModal("confirmacion-view.fxml", "Confirmar pedido",
        (ConfirmacionController c) -> c.inicializar(pedido));
if (confirmacion.isConfirmado()) { ... }
```

- El parámetro `preparar` (un `Consumer<T>`) existe porque los datos deben llegar **antes** de `showAndWait()`. Después ya sería tarde: la ventana estaría cerrada.
- **Cerrar solo la modal:** el controlador la obtiene desde cualquiera de sus nodos con `((Stage) txtNombre.getScene().getWindow()).close()`. Si usara `Platform.exit()` o el `Stage` principal, cerraría la aplicación.
- La modal no conoce a `PedidoController`. Solo expone `isConfirmado()`, y quien la abrió decide qué hacer. Es el mismo patrón que `Alert.showAndWait()`.

**Pregunta 4:** `WINDOW_MODAL` bloquea solo a la ventana dueña (y a sus ancestros). `APPLICATION_MODAL` bloquea todas las ventanas de la aplicación. Con una sola ventana principal el efecto es el mismo.

## Paso 5 — ¿Por qué no un `static`? (pregunta 2)

Un `public static Pedido pedido` en `AppPicada` también funcionaría, pero:

- cualquier clase podría modificarlo sin que se note en su firma (dependencia oculta);
- para probar un controlador habría que preparar un estado global;
- si mañana la aplicación atiende dos cajas, cada una con su pedido, hay que reescribir todo.

Pasarlo con `inicializar(pedido)` deja la dependencia **explícita** en la firma. Los casos completos aplican la misma idea con el `Repository`.

## Verificación

Ejecuta la prueba completa del README. Mientras la modal esté abierta, haz clic en la ventana principal: no debe responder.

## Errores frecuentes

| Síntoma | Causa |
|---|---|
| `NullPointerException` al usar `pedido` en un controlador | Se navegó sin llamar a `inicializar(...)` del controlador nuevo. |
| `ClassCastException: CartaController cannot be cast to DetalleController` | El FXML que se cargó no corresponde al controlador esperado (nombre de archivo equivocado). |
| Se abre una ventana nueva en cada navegación | Se usó `new Stage()` para vistas que deben ir en la principal. Usa `Navegador.navegar`. |
| La modal no bloquea la principal | Falta `initModality` o `initOwner`. |
| `isConfirmado()` siempre es `false` | Se usó `show()` en vez de `showAndWait()`: se leyó el resultado antes de que el usuario respondiera. |
| Confirmar cierra toda la aplicación | Se cerró el `Stage` principal o se llamó a `Platform.exit()`. |
| `IllegalStateException: Cannot set modality once window has been set visible` | `initModality` se llamó después de `show`. |
