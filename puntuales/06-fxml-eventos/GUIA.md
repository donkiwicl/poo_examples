# Guía de resolución · 06 FXML y eventos

> Para comparar tu trabajo con la solución: `git diff main solucion -- puntuales/06-fxml-eventos`

## Idea central

FXML separa **cómo se ve** la pantalla (XML editable en Scene Builder) de **cómo se comporta** (el controlador Java). `FXMLLoader` las une:

```
FXMLLoader.load()
 1. Lee el FXML y crea cada control.
 2. Crea el controlador indicado en fx:controller (constructor sin parámetros).
 3. Inyecta en cada atributo @FXML el control con el mismo fx:id.
 4. Enlaza los eventos: onAction="#onInscribir" → método onInscribir().
 5. Llama a initialize().
```

## Paso 1 — Inyección con `@FXML` (R1)

```java
@FXML private TextField txtNombre;          // fx:id="txtNombre"
@FXML private ComboBox<String> cmbNivel;
@FXML private ToggleGroup grpHorario;       // definido en <fx:define>
@FXML private ListView<Inscripcion> lstInscritos;
```

- `@FXML` permite que el atributo sea `private`. FXML lo asigna por reflexión, y por eso `module-info.java` declara `opens cl.dsy1102.ejemplos.inscripcion to javafx.fxml`.
- **Pregunta 1:** si el nombre no coincide con el `fx:id`, el atributo queda en `null` **sin aviso**, y el error aparece después como `NullPointerException`. Si falta el método de un `onAction`, la vista no carga: `LoadException: Error resolving onAction='#onLimpiar'`.

## Paso 2 — `initialize()` (R2)

```java
txtPareja.disableProperty().bind(chkPareja.selectedProperty().not());
lstInscritos.setItems(inscritos);
lblTotal.textProperty().bind(Bindings.format("Inscritos: %d", Bindings.size(inscritos)));
lstInscritos.getSelectionModel().selectedItemProperty().addListener((obs, anterior, actual) -> ...);
```

- **Pregunta 2:** en el constructor los `@FXML` todavía son `null` (el paso 3 ocurre después). `initialize()` se ejecuta cuando ya están inyectados.
- **Binding:** declara una relación permanente ("deshabilitado = NO marcado"). Con un evento tendrías que repetir la lógica al marcar, al desmarcar y al limpiar, y es fácil olvidar un caso.
- **`ObservableList`:** el `ListView` escucha la lista. Al agregar o quitar elementos la vista se actualiza sola, y nunca se agregan filas a mano al control.
- **Listener:** reacciona a cada cambio de una propiedad. `selectedItemProperty` cambia al hacer clic, al usar las flechas o al eliminar el seleccionado (en ese caso `actual` es `null`).
- El `ListView` muestra el `toString()` de cada `Inscripcion`.

## Paso 3 — `onInscribir()` (R3)

Se valida en **dos niveles** (pregunta 3):

| Nivel | Qué valida | Cómo informa |
|---|---|---|
| Controlador | Lo que depende de la interfaz: campo vacío, texto no numérico, opción sin elegir, nombre repetido en la lista. | Junta los errores en una lista y marca los campos. |
| Modelo | Las reglas del negocio: edad 8 a 99 y pareja con nombre. | Lanza `IllegalArgumentException`, que el controlador captura y muestra. |

Así la regla "edad entre 8 y 99" está escrita **una sola vez** y también protege al modelo si se usa desde otra pantalla.

```java
RadioButton horario = (RadioButton) grpHorario.getSelectedToggle();   // null si no hay ninguno
String pareja = chkPareja.isSelected() ? txtPareja.getText() : null;
```

Mostrar **todos** los errores juntos evita que el usuario corrija uno, reintente y descubra el siguiente.

## Paso 4 — Eliminar con confirmación (R4)

```java
if (confirmacion.showAndWait().filter(ButtonType.OK::equals).isPresent()) {
    inscritos.remove(seleccionada);
}
```

`showAndWait()` bloquea hasta que el usuario responde y retorna un `Optional<ButtonType>`. Se elimina de la **lista**, no del `ListView`.

## Paso 5 — CSS (R5)

```java
campo.getStyleClass().add("campo-error");                 // marca
campo.getStyleClass().remove("campo-error");              // desmarca antes de validar de nuevo
lblMensaje.getStyleClass().setAll("label", "mensaje-ok"); // reemplaza, conservando "label"
```

Usar clases CSS en vez de `setStyle(...)` deja los colores en un solo archivo. `setAll` evita que se acumulen `mensaje-ok` y `mensaje-error` al mismo tiempo.

## Paso 6 — Botón en Scene Builder (R6)

1. Abre `inscripcion-view.fxml` en Scene Builder.
2. Arrastra un `Button` al `HBox` de *Inscribir*, antes de este.
3. En *Code*: `fx:id` = `btnLimpiar`, *On Action* = `onLimpiar`.
4. En *Properties*: texto `Limpiar` y marca *Cancel Button*.
5. Crea el método `@FXML private void onLimpiar()` en el controlador. Si no existe, la vista no carga.

`defaultButton="true"` activa *Inscribir* con Enter y `cancelButton="true"` activa *Limpiar* con Esc.

## Verificación

Ejecuta la tabla de pruebas del README. En la solución, el resultado de la última fila es:

```
Ana Rojas
Edad: 21 años
Nivel: Intermedio
Horario: Tarde
Pareja: Bruno Diaz
```

## Errores frecuentes

| Síntoma | Causa |
|---|---|
| `NullPointerException` en `initialize()` | Un `@FXML` con nombre distinto del `fx:id`, o sin `@FXML` siendo `private`. |
| `LoadException: ... is not a valid type` | Falta el `<?import ...?>` del control en el FXML. |
| `IllegalAccessException ... does not open ... to javafx.fxml` | Falta `opens` en `module-info.java`. |
| `Location is not set` | La ruta de `getResource` no coincide con la carpeta en `resources`. Respeta mayúsculas y paquete. |
| El campo sigue rojo después de corregirlo | No se quita la clase `campo-error` antes de validar de nuevo. |
| `cannot bind ... A bound value cannot be set` | Se llamó `txtPareja.setDisable(...)` sobre una propiedad enlazada. |
