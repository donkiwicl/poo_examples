# Guía de resolución · 05 JavaFX: ciclo de vida, Stage y Scene

> Para comparar tu trabajo con la solución: `git diff main solucion -- puntuales/05-javafx-escenas`

## Idea central

Una aplicación JavaFX se organiza como un **teatro**:

```
Stage (ventana)  →  Scene (escenario)  →  raíz: Parent (BorderPane, VBox...)  →  nodos (Label, Button...)
```

- El `Stage` es la ventana del sistema operativo. JavaFX crea el principal y lo entrega a `start()`.
- La `Scene` contiene **un** grafo de nodos y tiene tamaño. Un `Stage` muestra una `Scene` a la vez.
- Los *layouts* (`VBox`, `HBox`, `BorderPane`, `GridPane`) calculan la posición de sus hijos. Así la interfaz se adapta al tamaño de la ventana sin coordenadas fijas.

## Paso 1 — Ciclo de vida (R1)

```
main() → launch() → constructor → init() → start(Stage) → ... usuario ... → stop()
                     JavaFX-Launcher        JavaFX Application Thread
```

| Método | Hilo | Para qué |
|---|---|---|
| `init()` | `JavaFX-Launcher` | Preparar datos que no sean de interfaz (leer configuración, conectar). |
| `start(Stage)` | `JavaFX Application Thread` | Construir y mostrar la interfaz. |
| `stop()` | `JavaFX Application Thread` | Liberar recursos y guardar al cerrar la última ventana. |

**Pregunta 2:** los nodos visibles solo pueden crearse y modificarse en el *JavaFX Application Thread*. Crear un `Label` suelto en `init()` funciona, pero mostrar un `Alert` o un `Stage` ahí lanza `IllegalStateException: Not on FX application thread`. Por eso los mensajes al usuario se muestran desde `start()` en adelante.

**Pregunta 1:** el `Stage` lo crea el *runtime* de JavaFX dentro de `launch()`. Por eso se **guarda** en un atributo (`this.stage = stage`) para cambiar de escena después.

## Paso 2 — Escena del contador (R2)

```java
BorderPane raiz = new BorderPane(centro);  // centro: VBox(lblPersonas, barra, lblEstado)
raiz.setTop(titulo);
raiz.setBottom(botones);                   // HBox(btnEntra, btnSale, btnVolver)
```

- `BorderPane` tiene cinco zonas (`top`, `bottom`, `left`, `right`, `center`). El **centro** ocupa todo el espacio que sobra, así que la barra crece al agrandar la ventana (R4).
- `barra.setMaxWidth(Double.MAX_VALUE)` permite que la `ProgressBar` se estire. Por defecto usa su ancho preferido.
- `new VBox(12, a, b, c)`: el primer argumento es el espacio entre hijos.

**Un solo punto de actualización:**

```java
btnEntra.setOnAction(e -> {
    contador.entrar();          // 1. el evento cambia el MODELO
    actualizar(...);            // 2. la VISTA se redibuja desde el modelo
});
```

Esta es la semilla de MVC (caso 08 y casos completos). El botón no escribe directamente `lblPersonas.setText(...)`. Toda la lógica de "¿está completo?" vive en `Contador`, y `actualizar` solo traduce el estado a texto, color y botones habilitados. Si mañana se agrega un botón *Reiniciar*, basta con llamar a `actualizar`.

`setDisable(true)` es mejor que mostrar un error: impide la acción inválida antes de que ocurra.

## Paso 3 — Cambio de escena (R3)

```java
btnVolver.setOnAction(e -> stage.setScene(crearEscenaBienvenida()));
```

Se reutiliza el **mismo** `Stage`, solo cambia su `Scene`. Abrir otro `new Stage()` dejaría dos ventanas abiertas.

**Pregunta 3:** `stage.setScene(nueva)` reemplaza la escena completa, incluido su tamaño y sus hojas de estilo. `stage.getScene().setRoot(nuevaRaiz)` conserva la escena y solo cambia el contenido, de modo que la ventana mantiene su tamaño. El `Navegador` de los casos 09 y completos usa la segunda forma.

## Paso 4 — `module-info.java`

```java
module cl.dsy1102.ejemplos.escenas {
    requires javafx.controls;           // Application, Stage, controles...
    exports cl.dsy1102.ejemplos.escenas; // JavaFX debe poder crear AppAforo
}
```

**Pregunta 4:** sin `exports`, al ejecutar aparece `IllegalAccessException: class com.sun.javafx.application.LauncherImpl (in module javafx.graphics) cannot access class ...AppAforo`. JavaFX crea `AppAforo` por reflexión desde **su** módulo, y un paquete no exportado es invisible para otros módulos.

## Verificación

1. `mvn javafx:run` muestra la bienvenida.
2. Ingresa `abc` → *Ingresa un numero entero.* · `0` → *El aforo debe ser mayor que 0.*
3. Ingresa `3`, pulsa *+ Entra* tres veces → `AFORO COMPLETO` en rojo y *+ Entra* deshabilitado.
4. *Volver* → misma ventana con la bienvenida.
5. Cierra la ventana → la consola muestra `stop()` y las personas dentro.

## Errores frecuentes

| Síntoma | Causa |
|---|---|
| `Error: JavaFX runtime components are missing` | Se ejecutó `Main` con *Run* del IDE sin módulos. Usa `mvn javafx:run`. |
| `Module javafx.controls not found` | La versión de JavaFX no coincide con el JDK o Maven no descargó dependencias. |
| `NullPointerException` en `stage.setScene` | No se guardó el `Stage` en el atributo dentro de `start()`. |
| Los botones no reaccionan | Falta `setOnAction`, o el lambda modifica el modelo pero no llama a `actualizar`. |
| La barra no crece | Falta `setMaxWidth(Double.MAX_VALUE)`. |
