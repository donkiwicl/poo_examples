# 07 · JavaFX Images — Catálogo de Parques Nacionales

**Tema:** `Image` e `ImageView`, recursos del proyecto con `getResource`, `preserveRatio` y ajuste al contenedor con *bindings*, imagen de reemplazo, celdas personalizadas en `ListView` (`setCellFactory`), `FileChooser`, carga en segundo plano con `ProgressIndicator`, ícono de la ventana.
**Tipo:** JavaFX con FXML · **Tiempo estimado:** 1 bloque

> La guía de resolución está en la rama `solucion`, en el archivo `GUIA.md` de esta carpeta.

## Problema

Un catálogo de Parques Nacionales de Chile muestra a la izquierda la lista de parques y a la derecha la imagen y la ficha del seleccionado. La ventana ya existe, pero **no muestra ninguna imagen**. Se necesita:

- mostrar la imagen de cada parque, guardada **dentro del proyecto** (no en una ruta de tu computador);
- que la imagen **no se deforme** y se ajuste al tamaño de la ventana;
- que un parque cuya imagen no existe (Pan de Azúcar) muestre una imagen de reemplazo en vez de fallar;
- miniaturas en la lista;
- permitir ver una imagen propia del disco del usuario, sin congelar la ventana con archivos grandes;
- un ícono propio para la ventana.

Las ilustraciones de `img/` son dibujos generados para este curso y se pueden usar libremente.

## Código inicial

| Archivo | Estado |
|---|---|
| `Parque.java`, `Catalogo.java` | Resueltos. Pan de Azúcar apunta a `pan-de-azucar.png`, que **no existe** a propósito. |
| `AppParques.java` | Resuelto, salvo el ícono (R6). |
| `parques-view.fxml`, `estilos.css` | Resueltos. |
| `resources/.../img/` | Cinco parques, `sin-imagen.png` e `icono.png`. |
| `ParquesController.java` | La lista y las fichas ya funcionan. **Imágenes por completar.** |

## Requerimientos

**R1. Imagen desde recursos.** Completa `cargarImagen(String archivo)` usando `getClass().getResource(...)`. No uses rutas como `C:\Users\...` ni `src/main/resources/...`.

**R2. Ajuste sin deformar.** El `ImageView` conserva la proporción de la imagen y se ajusta al marco cuando la ventana cambia de tamaño. Enlaza `fitWidth` y `fitHeight` al tamaño de `marcoImagen` y descuenta su *padding*.

**R3. Imagen de reemplazo.** Si el recurso no existe, `cargarImagen` retorna `sin-imagen.png` y escribe un aviso en `System.err`. La aplicación no debe lanzar excepciones.

**R4. Miniaturas.** Con `setCellFactory`, cada fila de la lista muestra una miniatura de 64×40 a la izquierda del nombre. Evita decodificar la misma imagen varias veces.

**R5. Imagen propia.** *Cargar imagen propia…* abre un `FileChooser` filtrado por imágenes (`png`, `jpg`, `jpeg`, `gif`, `bmp`):
- Si el usuario cancela, no pasa nada.
- La imagen se carga **en segundo plano** y el `ProgressIndicator` se muestra mientras carga.
- Si el archivo no es una imagen válida (prueba renombrando un `.txt` a `.png`), se muestra un `Alert` de error y se vuelve a la imagen del parque.
- *Restaurar* vuelve a la imagen del parque.

**R6. Ícono.** La ventana usa `img/icono.png` como ícono.

## Cómo ejecutar

```bash
mvn javafx:run
```

## Preguntas para pensar

1. ¿Por qué `new Image("src/main/resources/img/lauca.png")` funciona en tu IDE pero no al distribuir la aplicación?
2. `getResource` retorna `null` cuando el archivo no existe. ¿Qué excepción aparecería si no lo compruebas?
3. ¿Qué diferencia hay entre `Image` e `ImageView`? ¿Pueden dos `ImageView` mostrar el mismo objeto `Image`?
4. ¿Por qué `Image` no lanza una excepción cuando el archivo está dañado?
