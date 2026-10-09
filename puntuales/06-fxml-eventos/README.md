# 06 · FXML y eventos — Inscripciones al Taller de Cueca

**Tema:** FXML, `fx:controller`, `fx:id` y `@FXML`, `initialize()`, eventos `onAction`, `ComboBox`, `RadioButton` con `ToggleGroup`, `CheckBox`, `ListView`, *bindings*, *listeners*, CSS, validación con mensajes al usuario, Scene Builder.
**Tipo:** JavaFX con FXML · **Tiempo estimado:** 1 a 2 bloques

> La guía de resolución está en la rama `solucion`, en el archivo `GUIA.md` de esta carpeta.

## Problema

El Centro Cultural Estación Yungay abre su Taller de Cueca y necesita registrar a los inscritos. El diseñador ya armó la pantalla en Scene Builder (`inscripcion-view.fxml`), pero **ningún control hace nada**: el controlador está vacío.

La pantalla debe permitir:

- ingresar nombre, edad (8 a 99), nivel (*Principiante*, *Intermedio*, *Avanzado*) y horario (*Mañana* o *Tarde*);
- marcar *Viene con pareja*. Solo entonces se habilita el campo del nombre de la pareja;
- inscribir con el botón o con **Enter**, rechazando datos incompletos, edades fuera de rango y nombres repetidos (sin distinguir mayúsculas);
- ver la lista de inscritos con un contador que se actualiza solo, y el detalle de la inscripción seleccionada;
- eliminar una inscripción después de confirmar.

## Código inicial

| Archivo | Estado |
|---|---|
| `AppInscripcion.java` | Resuelto. Carga el FXML con `FXMLLoader`. |
| `Inscripcion.java` | Resuelto. Modelo con validaciones que lanzan `IllegalArgumentException`. |
| `module-info.java` | Resuelto. Revisa el `opens ... to javafx.fxml`. |
| `inscripcion-view.fxml` | Resuelto. Ábrelo en Scene Builder y revisa cada `fx:id`. |
| `estilos.css` | Le faltan las clases de error y mensaje (R5). |
| `InscripcionController.java` | **Por completar.** Solo tiene los métodos de evento vacíos para que la vista cargue. |

## Requerimientos

**R1. Inyección.** Declara un atributo `@FXML private` por cada `fx:id` de la vista, con el **mismo nombre** y el tipo correcto (`TextField`, `ComboBox<String>`, `ToggleGroup`, `ListView<Inscripcion>`...).

**R2. `initialize()`**
- Carga los tres niveles en `cmbNivel`.
- `txtPareja` se habilita solo cuando `chkPareja` está marcado. Usa un **binding**, no un `if` dentro de un evento.
- `lstInscritos` muestra una `ObservableList<Inscripcion>` del controlador.
- `lblTotal` muestra `Inscritos: N` y se actualiza solo (`Bindings.size`).
- Al seleccionar un inscrito, `lblDetalle` muestra su `obtenerDetalle()` (*listener* sobre `selectedItemProperty`).

**R3. `onInscribir()`**
- Valida en el controlador lo que es de la interfaz: campos vacíos, edad no numérica, nivel u horario sin elegir y nombre repetido. Muestra **todos** los errores juntos en `lblMensaje` y marca en rojo los campos con error.
- Las reglas de rango las aplica el modelo: captura su `IllegalArgumentException` y muestra el mensaje.
- Si todo está bien, agrega la inscripción, limpia el formulario y muestra `Se inscribió a <nombre>.` en verde.

**R4. `onEliminar()`.** Sin selección: un `Alert` de advertencia. Con selección: un `Alert` de confirmación antes de borrar.

**R5. CSS.** Agrega a `estilos.css` las clases `.campo-error` (borde rojo), `.mensaje-ok` y `.mensaje-error`, y aplícalas desde el controlador con `getStyleClass()`.

**R6. Scene Builder.** Agrega a la vista un botón **Limpiar** (`fx:id="btnLimpiar"`, `onAction="#onLimpiar"`), a la izquierda de *Inscribir*, que se active también con la tecla **Esc** (`cancelButton`).

## Pruebas manuales

| Acción | Resultado esperado |
|---|---|
| *Inscribir* con todo vacío | Cuatro errores juntos; nombre, edad y nivel en rojo. |
| Ana Rojas, edad `5`, Intermedio, Tarde | `La edad debe estar entre 8 y 99 años.` |
| Edad `21`, marcar pareja sin nombre | `Si viene con pareja, indica su nombre.` |
| Pareja `Bruno Diaz` | `Se inscribió a Ana Rojas.` · `Inscritos: 1` |
| Inscribir `ana rojas` otra vez | `ana rojas ya está inscrito.` |
| Seleccionar a Ana en la lista | El detalle muestra edad, nivel, horario y pareja. |

## Cómo ejecutar

```bash
mvn javafx:run
```

## Preguntas para pensar

1. ¿Qué ocurre si un atributo `@FXML` se llama distinto de su `fx:id`? ¿Y si el método del `onAction` no existe?
2. ¿Por qué se usa `initialize()` y no el constructor del controlador para configurar los controles?
3. ¿Qué validaciones pertenecen al controlador y cuáles al modelo?
