# 09 · Navegación entre vistas — Pedidos de la Picada La Tía Rosa

**Tema:** varias vistas FXML en una misma ventana, clase `Navegador`, paso de parámetros entre controladores, estado compartido, ventana **modal** (`initOwner`, `initModality`, `showAndWait`) con valor de retorno, `Spinner`, doble clic.
**Tipo:** JavaFX con FXML · **Tiempo estimado:** 1 a 2 bloques

> La guía de resolución está en la rama `solucion`, en el archivo `GUIA.md` de esta carpeta.

## Problema

La Picada La Tía Rosa recibe pedidos para llevar. La aplicación tiene cuatro vistas:

```
┌──────────┐  Ver detalle / doble clic  ┌──────────┐
│  Carta   │ ─────────────────────────► │ Detalle  │  Spinner de cantidad, subtotal
│          │ ◄───────────────────────── │          │  Agregar al pedido / Volver
│          │                            └──────────┘
│          │  Ver pedido                ┌──────────┐   Confirmar   ┌──────────────┐
│          │ ─────────────────────────► │  Pedido  │ ────────────► │ Confirmación │ ventana MODAL
│          │ ◄───────────────────────── │          │ ◄──────────── │  (nombre)    │
└──────────┘  Seguir pidiendo / listo   └──────────┘   resultado   └──────────────┘
```

- La carta, el detalle y el pedido se muestran **en la misma ventana**. La confirmación es una **ventana aparte** que bloquea a la principal hasta cerrarse.
- El pedido se mantiene mientras el usuario navega. Al confirmar se vacía y la carta muestra `Pedido listo para Ana. Total: $12.100`.

## Código inicial

| Archivo | Estado |
|---|---|
| `model/` (`Plato`, `Pedido`, `LineaPedido`, `Carta`) | Resuelto. |
| `view/*.fxml`, `estilos.css` | Resueltos. |
| `AppPicada.java` | Resuelto. Crea **un** `Pedido` y se lo entrega a la carta. |
| `Navegador.java` | `navegar()` resuelto. **`abrirModal()` por completar.** |
| `CartaController.java` | `inicializar(Pedido)` resuelto como ejemplo de paso de parámetros. **Navegación por completar.** |
| `DetalleController`, `PedidoController`, `ConfirmacionController` | **Por completar.** |

## Requerimientos

**R1. Carta → Detalle → Carta.**
- *Ver detalle* navega con `Navegador.navegar(...)` y entrega al `DetalleController` el pedido y el plato seleccionado. Sin selección, se muestra un `Alert`.
- El detalle muestra el plato, un `Spinner` de 1 a 10 y el subtotal, que se actualiza al cambiar la cantidad.
- *Agregar al pedido* agrega las unidades y vuelve a la carta con `Agregaste 2 x Pastel de choclo.`. *Volver* regresa sin agregar.
- Al volver, la cabecera de la carta muestra la cantidad de ítems y el total **actualizados**.

**R2. Doble clic.** Hacer doble clic sobre un plato equivale a *Ver detalle*.

**R3. Carta → Pedido → Carta.** La vista del pedido muestra sus líneas y el total. Con el pedido vacío, *Confirmar pedido* queda deshabilitado. *Seguir pidiendo* vuelve conservando el pedido.

**R4. Ventana modal.**
- Implementa `Navegador.abrirModal(fxml, titulo, preparar)`: un `Stage` nuevo con dueño (`initOwner`) y modal (`initModality`) que se muestra con `showAndWait()` y retorna el controlador.
- La confirmación muestra el total y pide el nombre para el retiro. Con el nombre vacío, el error se ve **dentro de la modal**.
- *Confirmar* cierra **solo** la modal. Luego el `PedidoController` lee `isConfirmado()`, vacía el pedido y vuelve a la carta con el mensaje.
- *Cancelar* (o Esc) cierra la modal y deja todo igual.

## Prueba completa

1. *Ver detalle* sin selección → `Alert`.
2. Pastel de choclo, cantidad 2 → subtotal `$17.800` → *Agregar* → `Pedido: 2 ítems · $17.800`.
3. Doble clic en Empanada de pino → *Agregar*.
4. *Ver pedido* → dos líneas, total `$21.000`.
5. *Confirmar* → *Confirmar* sin nombre → error en la modal. Mientras la modal esté abierta, la ventana principal no responde.
6. Nombre `Ana` → la carta muestra `Pedido listo para Ana. Total: $21.000` y `Pedido: 0 ítems · $0`.

## Cómo ejecutar

```bash
mvn javafx:run
```

## Preguntas para pensar

1. Al volver a la carta, `navegar()` crea un `CartaController` **nuevo**. ¿Por qué el pedido no se pierde?
2. ¿Qué pasaría si `Pedido` fuera un atributo `static` de `AppPicada` al que todos acceden directamente? ¿Qué se gana pasándolo como parámetro?
3. ¿En qué línea de `onConfirmar()` se detiene el programa mientras la modal está abierta?
4. ¿Qué diferencia hay entre `Modality.WINDOW_MODAL` y `APPLICATION_MODAL`?
