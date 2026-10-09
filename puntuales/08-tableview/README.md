# 08 · TableView y ObservableList — Tabla de posiciones de la Liga Vecinal

**Tema:** `TableView`, `TableColumn`, `PropertyValueFactory` y lambdas en `setCellValueFactory`, propiedades JavaFX (`StringProperty`, `IntegerProperty`, *bindings*), `ObservableList`, `FilteredList`, `SortedList`, orden por varias columnas, `setCellFactory`, *extractor*.
**Tipo:** JavaFX con FXML · **Tiempo estimado:** 1 a 2 bloques

> La guía de resolución está en la rama `solucion`, en el archivo `GUIA.md` de esta carpeta.

## Problema

La Liga Vecinal Cordillera de baby fútbol lleva su tabla de posiciones en una pizarra. Quieren una aplicación que:

- muestre la tabla con **#, Equipo, Comuna, PJ, PG, PE, PP, GF, GC, DIF y PTS**;
- ordene por puntos, luego por diferencia de goles y luego por goles a favor, y que permita reordenar haciendo clic en cualquier columna;
- filtre **en tiempo real** por nombre y por comuna;
- registre el resultado de un partido y actualice la tabla **sin recargarla**;
- agregue y elimine equipos.

Puntaje: ganado = 3, empatado = 1, perdido = 0.

## Código inicial

| Archivo | Estado |
|---|---|
| `Equipo.java` | Resuelto. Usa **propiedades JavaFX**. PJ, PTS y DIF son *bindings* calculados. |
| `Liga.java` | Resuelto: reglas de la liga y datos de ejemplo. |
| `liga-view.fxml`, `estilos.css` | Resueltos. |
| `AppLiga.java` | Resuelto. Entrega la `Liga` al controlador con `setLiga(...)`. |
| `LigaController.java` | Atributos `@FXML` declarados. **Lógica por completar.** Hoy la tabla muestra filas vacías. |

## Requerimientos

**R1. Columnas.** Configura `setCellValueFactory` en cada columna:
- `PropertyValueFactory` para nombre, comuna, PG, PE, PP, GF y GC;
- un lambda para PJ, DIF y PTS, que no son propiedades sino *bindings* (`jugadosBinding().asObject()`).

**R2. Orden inicial.** Al abrir, la tabla está ordenada por PTS, DIF y GF, todos de mayor a menor (`getSortOrder()`, `setSortType`).

**R3. Filtros.** Encadena `ObservableList → FilteredList → SortedList → TableView`:
- `txtBuscar` filtra por nombre (contiene, sin distinguir mayúsculas);
- `cmbComuna` ofrece `Todas` más las comunas existentes, sin repetir y ordenadas, y se recalcula al agregar o eliminar equipos;
- el clic en las columnas debe seguir ordenando (enlaza el `comparatorProperty`);
- `lblTotal` muestra `Mostrando X de Y equipos`.

**R4. Celdas personalizadas** (`setCellFactory`):
- la columna **#** muestra la posición de la fila (1, 2, 3...);
- **DIF** muestra el signo (`+2`, `0`, `-6`) en verde si es positiva y en rojo si es negativa (clases `dif-positiva` y `dif-negativa` de `estilos.css`).

**R5. Registrar resultado.** Lee los goles y delega en `liga.registrarResultado(...)`. Los errores (goles no numéricos, equipo contra sí mismo, equipos sin elegir) se informan con un `Alert`. **No llames a `tblEquipos.refresh()`.**

**R6. Agregar y eliminar.** *Agregar* crea el `Equipo` (el modelo rechaza nombres repetidos o vacíos). *Eliminar equipo* pide confirmación. Ambos operan sobre la lista de la liga, nunca sobre la tabla.

**R7. Desafío: reordenar al cambiar los puntos.** Registra `Juventud Las Vizcachas 5 - 0 Club Los Quillayes`. Los números de la fila cambian, pero el equipo **no sube** de posición hasta que haces clic en una columna. Investiga el **extractor** de `FXCollections.observableArrayList(...)` y corrígelo en `Liga`.

## Resultado esperado

Al abrir: Unión Bellavista (4 pts, +2), Real Cordillera (4, +1), Deportivo Nonato (3, +2), Estrella del Maipo (2), Club Los Quillayes (1), Juventud Las Vizcachas (1, -4).

Después del 5-0 (con R7 resuelto): Juventud Las Vizcachas sube al **2.º lugar** (4 pts, +1, 7 GF) y supera a Real Cordillera por goles a favor.

## Cómo ejecutar

```bash
mvn javafx:run
```

## Preguntas para pensar

1. ¿Qué busca exactamente `new PropertyValueFactory<>("golesFavor")`? ¿Qué pasa si escribes `"golesfavor"`?
2. ¿Por qué al registrar un resultado las celdas cambian solas, sin `refresh()`?
3. ¿Por qué `tblEquipos.getItems().add(...)` lanza `UnsupportedOperationException` después de R3?
4. ¿Qué ventaja tiene que la posición se calcule en la celda y no se guarde en `Equipo`?
