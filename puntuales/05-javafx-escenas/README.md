# 05 · JavaFX — Ciclo de vida, `Stage` y `Scene`

**Tema:** `Application` (`init`, `start`, `stop`), hilos de JavaFX, `Stage` y `Scene`, cambio de escena, layouts creados en código (`BorderPane`, `VBox`, `HBox`), eventos con lambdas, `module-info.java`.
**Tipo:** JavaFX sin FXML · **Tiempo estimado:** 1 bloque

> La guía de resolución está en la rama `solucion`, en el archivo `GUIA.md` de esta carpeta.

## Problema

La Ramada Los Copihues necesita controlar el aforo en la entrada. El encargado de puerta usará una aplicación con dos pantallas:

1. **Bienvenida:** ingresa el aforo máximo y pulsa *Comenzar*. Si el valor no es un entero positivo, se muestra un mensaje en rojo.
2. **Contador:** muestra en grande cuántas personas hay dentro, una barra de ocupación y cuántos lugares quedan. Tiene los botones *+ Entra*, *- Sale* y *Volver*.

Antes de usar FXML (caso 06) queremos entender qué hace JavaFX por debajo, así que **todo se construye con código Java**.

## Código inicial

| Archivo | Estado |
|---|---|
| `module-info.java` | Resuelto. |
| `Contador.java` | Resuelto. Modelo sin JavaFX: `entrar()`, `salir()`, `estaCompleto()`, `getOcupacion()`. |
| `AppAforo.java` | La escena de bienvenida está resuelta como ejemplo. **Ciclo de vida y escena del contador por completar.** |

## Requerimientos

**R1. Ciclo de vida.** Traza por consola `init()`, `start()` y `stop()` con el nombre del hilo. En `stop()` informa cuántas personas quedaron dentro.

**R2. Escena del contador** (`crearEscenaContador()`):
- `BorderPane` con un título arriba (`Aforo maximo: N`), al centro un `VBox` con el número de personas (fuente grande), una `ProgressBar` y un `Label` de estado, y abajo un `HBox` con los tres botones.
- Los botones modifican el `Contador` y luego **un solo método** `actualizar(...)` refresca la vista.
- Con el aforo completo: *+ Entra* se deshabilita y el estado muestra `AFORO COMPLETO` en rojo. Sin nadie dentro, *- Sale* se deshabilita. En los demás casos: `Quedan N lugares` en verde.

**R3. Cambio de escena.** *Volver* regresa a la bienvenida **en la misma ventana**. No uses `new Stage()`.

**R4. Ventana adaptable.** Fija un tamaño mínimo del `Stage` y comprueba que al agrandar la ventana la barra de progreso crece y los botones quedan centrados.

## Resultado esperado

Consola al abrir la aplicación, contar 3 personas y cerrar la ventana:

```
[Ciclo de vida] init()  - hilo: JavaFX-Launcher
[Ciclo de vida] start() - hilo: JavaFX Application Thread
[Ciclo de vida] stop()  - hilo: JavaFX Application Thread
Personas dentro al cerrar: 3
```

## Cómo ejecutar

```bash
mvn javafx:run
```

## Preguntas para pensar

1. ¿Quién crea el objeto `Stage` que recibe `start()`?
2. ¿Por qué `init()` y `start()` corren en hilos distintos? ¿Podrías crear un `Label` en `init()`?
3. ¿Qué diferencia hay entre cambiar la `Scene` del `Stage` y cambiar la raíz (`scene.setRoot(...)`)?
4. ¿Qué error aparece si quitas `exports` del `module-info.java`?
