# Guía de resolución · Caso completo 01 Arriendo Cerro Alegre

Esta guía acompaña la rama `solucion`. Recorre el enunciado en el orden de los requerimientos e indica **qué se pide**, **dónde está resuelto** y **por qué se resolvió así**.

> Comparar tu avance: `git diff main solucion -- completos/01-arriendo-bicicletas`

---

## 0. Mapa de requisitos

| Requisito | Dónde se cumple |
|---|---|
| R1 Proyecto y ciclo de vida | `pom.xml`, `module-info.java`, `AppFX` |
| R2 Modelo listo para JSON y tabla | `model/Vehiculo`, `Bicicleta`, `ScooterElectrico` |
| R3 Vista principal | `principal-view.fxml`, `PrincipalController` |
| R4 Formulario crear/editar | `formulario-view.fxml`, `FormularioController` |
| R5 Arriendo | `arriendo-view.fxml`, `ArriendoController`, `Vehiculo.arrendar` |
| R6 Persistencia | `dao/*`, `repository/*` |
| R7 Validación y errores | `FormularioController`, `ArriendoController`, `Alertas`, `JsonVehiculoDao` |
| R8 Navegación | `Navegador` y el método `inicializar(...)` de cada controlador |

```
src/main/java/cl/dsy1102/arriendo/
├── AppFX.java                 arma las capas y muestra la vista principal
├── Navegador.java             FXMLLoader + paso de parámetros
├── Main.java                  demostración por consola
├── model/                     Vehiculo (abstracta), Bicicleta, ScooterElectrico, Recargable, ArriendoRechazadoException
├── dao/                       VehiculoDao, JsonVehiculoDao, PersistenciaException
├── repository/                Repository<T>, VehiculoRepository
└── controller/                Alertas, PrincipalController, FormularioController, ArriendoController
```

Flujo de dependencias:

```
Vista (FXML) → Controlador → Repository (interfaz) → VehiculoRepository → VehiculoDao (interfaz) → JsonVehiculoDao → data/vehiculos.json
                    └──────────→ Modelo (Vehiculo.arrendar, copiar, Recargable) ←──────────┘
```

---

## Paso 1 — Ciclo de vida y armado de capas (R1)

```java
Repository<Vehiculo> repositorio = new VehiculoRepository(new JsonVehiculoDao(ARCHIVO_DATOS));
Navegador.setStage(stage);
PrincipalController principal = Navegador.navegar("principal-view.fxml", "Arriendo Cerro Alegre");
principal.inicializar(repositorio);
stage.show();
try { repositorio.cargar(); } catch (PersistenciaException e) { Alertas.error(...); }
```

- `AppFX` es el **único** lugar, junto al DAO, que conoce `data/vehiculos.json`. Los controladores reciben `Repository<Vehiculo>`, una interfaz.
- Primero se muestra la ventana y después se cargan los datos. Si la carga falla, el `Alert` aparece sobre una ventana visible y la aplicación continúa vacía.
- La tabla se enlaza a la `ObservableList` **antes** de cargar. Cuando `cargar()` hace `setAll`, la tabla se llena sola.

`module-info.java`:

| Línea | Por qué |
|---|---|
| `opens ...controller to javafx.fxml` | FXML inyecta los `@FXML` privados. |
| `opens ...model to com.fasterxml.jackson.databind` | Jackson usa los constructores protegidos y el campo privado `disponible`. |
| `opens ...model to javafx.base` | `PropertyValueFactory` invoca los *getters*. |
| `exports cl.dsy1102.arriendo` | JavaFX instancia `AppFX`. |

## Paso 2 — Modelo (R2)

**`obtenerTipo()` abstracto.** La tabla muestra el tipo por polimorfismo:

```java
colTipo.setCellValueFactory(celda -> new ReadOnlyStringWrapper(celda.getValue().obtenerTipo()));
```

Como no empieza con `get`, Jackson no lo guarda en el JSON.

**Jackson:**

```java
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "tipo")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Bicicleta.class, name = "BICICLETA"),
        @JsonSubTypes.Type(value = ScooterElectrico.class, name = "SCOOTER")
})
public abstract class Vehiculo {
    @JsonProperty("disponible")
    private boolean disponible = true;

    protected Vehiculo() { }
```

- `disponible` **no tiene *setter* público**: solo cambia con `arrendar()` y `devolver()`, que aplican las reglas. ¿Cómo lo restaura Jackson al leer? Como existe el *getter* `isDisponible()`, Jackson **infiere** que puede escribir directamente el campo privado (opción `INFER_PROPERTY_MUTATORS`, activa por defecto). `@JsonProperty("disponible")` sobre el campo deja esa decisión **explícita** y sigue funcionando aunque el *getter* cambie de nombre. Sin *getter* convencional (por ejemplo, `estaDisponible()`), la anotación es **obligatoria**. Es lo que ocurre en la Fonda con `tieneVentaRestringida()`.
- Con el constructor vacío, Jackson llama a los *setters*, que validan. Un JSON editado a mano con `"codigo": "X1"` se rechaza al cargar.
- `copiar()` ahora usa el constructor vacío y copia los campos uno a uno. No pasa por las validaciones porque el original ya es válido.

**Template method en `arrendar`.** La regla general vive en `Vehiculo` y es `final`. Cada subclase solo completa los puntos de extensión:

```java
public final String arrendar(int horas) throws ArriendoRechazadoException {
    if (!disponible) throw ...;
    if (horas < 1 || horas > MAXIMO_HORAS) throw ...;
    validarCondiciones(horas);      // ScooterElectrico: batería mínima
    int total = calcularCosto(horas);
    disponible = false;
    registrarUso(horas);            // ScooterElectrico: gasta batería
    return "Arriendo autorizado: ...";
}
```

## Paso 3 — Vista principal (R3)

- **Columnas calculadas** con lambdas (`obtenerTipo()`, el estado como texto y la batería). El resto usa `PropertyValueFactory`.
- **Batería por la interfaz:**
  ```java
  return vehiculo instanceof Recargable recargable ? recargable.getBateria() + "%" : "—";
  ```
  Se pregunta por una **capacidad** (`Recargable`), no por `ScooterElectrico`. Si mañana se agrega una bicicleta eléctrica que implemente `Recargable`, la tabla y el botón *Recargar* funcionan sin cambios.
- **Celdas con estilo:** `colEstado.setCellFactory(...)` quita las clases `estado-disponible` y `estado-arrendado` y luego agrega la que corresponde. Las celdas se reutilizan al ordenar o filtrar.
- **Filtro doble:** el predicado combina el texto (código **o** modelo) **y** el `CheckBox`. Ambos *listeners* llaman al mismo `actualizarFiltro()`.
- `FilteredList → SortedList` con `comparatorProperty().bind(...)`, igual que en la Fonda.

**Devolver y Recargar con copia:**

```java
Vehiculo copia = seleccionado.copiar();
copia.devolver();                              // la regla puede rechazar
repositorio.actualizar(seleccionado, copia);   // si falla el guardado, el original queda intacto
```

Si se modificara el objeto original y luego fallara el guardado, la tabla mostraría *Disponible* aunque el archivo dijera *Arrendado*. La prueba de "fallo de escritura" del README lo detecta.

## Paso 4 — Formulario (R4)

- Igual que en la Fonda: `visibleProperty` y `managedProperty` enlazadas al `ComboBox` de tipo.
- **Editar sin perder el estado:**
  ```java
  Vehiculo editado = original.copiar();     // conserva disponible y batería
  editado.setModelo(modelo);
  editado.setTarifaHora(tarifa);
  ...
  repositorio.actualizar(original, editado);
  ```
  Si se creara un objeto nuevo con el constructor, un vehículo arrendado volvería a *Disponible* al cambiar su tarifa (prueba 4).
- **Código repetido:** lo controla `VehiculoRepository.agregar`, que lanza `IllegalArgumentException`. El formulario la captura junto con las del modelo y muestra el mensaje.
- El código se deshabilita al editar porque identifica al vehículo. Si cambiara, los registros asociados quedarían huérfanos.

## Paso 5 — Arriendo (R5)

```java
Vehiculo copia = vehiculo.copiar();
String mensaje = copia.arrendar(horas);      // ArriendoRechazadoException → mensaje en rojo
repositorio.actualizar(vehiculo, copia);     // PersistenciaException → Alert
vehiculo = copia;
```

- El controlador **no** sabe nada de baterías ni de descuentos: llama a `arrendar` y muestra el texto.
- El costo estimado usa `vehiculo.calcularCosto(horas)` (polimorfismo) en un *listener* de `txtHoras`.
- Tras un arriendo exitoso se deshabilita *Arrendar* y se refresca la ficha.

## Paso 6 — Persistencia (R6)

`JsonVehiculoDao` sigue el mismo diseño de la Fonda (ver caso puntual 10):
- archivo inexistente o vacío → lista vacía;
- `Files.createDirectories` antes de escribir;
- `writerFor(TypeReference<List<Vehiculo>>)` con *pretty print*, para que cada objeto lleve su `tipo`;
- `IOException` o `IllegalArgumentException` → `PersistenciaException` con la causa.

`VehiculoRepository` **guarda una copia de la lista** antes de tocar la `ObservableList`:

```java
List<Vehiculo> copia = new ArrayList<>(vehiculos);
copia.set(indice, actualizado);
dao.guardar(copia);              // si lanza, la línea siguiente no se ejecuta
vehiculos.set(indice, actualizado);
```

Hay dos niveles de copia: la **del vehículo** (controlador) y la **de la lista** (repositorio). Juntas garantizan que la pantalla solo muestre lo que quedó en disco.

## Paso 7 — Validación (R7)

| Qué | Dónde | Cómo se informa |
|---|---|---|
| Campos vacíos, texto no numérico, tipo o categoría sin elegir | `FormularioController` | Un `Alert` con todos los errores y los campos en rojo. |
| Formato de código, rangos de tarifa, batería y autonomía | Setters del modelo | `IllegalArgumentException` → `Alert`. |
| Código repetido | `VehiculoRepository.agregar` | `IllegalArgumentException` → `Alert`. |
| Horas, disponibilidad, batería mínima | `Vehiculo.arrendar` | `ArriendoRechazadoException` → texto rojo. |
| Lectura o escritura del archivo | `JsonVehiculoDao` | `PersistenciaException` → `Alert` de error. |

## Paso 8 — Navegación (R8)

`Navegador.navegar(fxml, titulo)` reemplaza la raíz de la escena y retorna el controlador. Cada vista recibe lo que necesita con `inicializar(...)`:

| Vista | Recibe |
|---|---|
| Principal | `Repository<Vehiculo>` |
| Formulario | `Repository<Vehiculo>` y el vehículo, o `null` para crear uno nuevo |
| Arriendo | `Repository<Vehiculo>` y el vehículo |

Al volver, la vista principal se crea de nuevo y se le entrega **el mismo repositorio**, con la misma `ObservableList`, así que no se pierde nada.

---

## Verificación con los datos de prueba

| Prueba | Resultado en la solución |
|---|---|
| Formulario vacío | `Selecciona el tipo de vehículo.` + `El campo Código es obligatorio.` + ... |
| Código `X1` | `El código debe tener el formato AA-00 (ejemplo: BK-01).` |
| `BK-01` repetido | `Ya existe un vehículo con el código BK-01.` |
| BK-02 por 5 h | `Arriendo autorizado: BK-02 por 5 h \| Total: $14.000` |
| SC-02 con 15 % | `Arriendo rechazado: SC-02 tiene 15% de batería (mínimo 30%).` |
| Recargar SC-02 y arrendar 3 h | `Total: $9.400` y luego `70%` en la tabla |
| Recargar una bicicleta | `BK-01 no tiene batería.` |
| Eliminar un vehículo arrendado | `Registra la devolución de BK-01 antes de eliminarlo.` |
| Archivo de solo lectura y arrendar | `Alert` de error; el vehículo sigue *Disponible* |
| JSON dañado | `Alert` al iniciar y tabla vacía |

Formato del archivo:

```json
[ {
  "tipo" : "BICICLETA",
  "codigo" : "BK-01",
  "modelo" : "Oxford Urbana",
  "tarifaHora" : 2500,
  "categoria" : "Urbana",
  "conCanasto" : true,
  "disponible" : false
}, {
  "tipo" : "SCOOTER",
  "codigo" : "SC-02",
  "modelo" : "Segway E2",
  "tarifaHora" : 2800,
  "bateria" : 70,
  "autonomiaKm" : 25,
  "disponible" : false
} ]
```

## Errores frecuentes

| Síntoma | Causa |
|---|---|
| Al reiniciar, todos los vehículos aparecen *Disponible* | Jackson no ve el campo: el *getter* no sigue la convención (`estaDisponible()` en vez de `isDisponible()`) y falta `@JsonProperty` en el campo. Revisa que `"disponible"` aparezca en el JSON. |
| `Cannot construct instance of Vehiculo` | Faltan `@JsonTypeInfo`/`@JsonSubTypes` o el constructor vacío. |
| Editar la tarifa deja el vehículo disponible | Se creó un objeto nuevo en vez de editar `original.copiar()`. |
| La tabla muestra un arriendo que no se guardó | Se modificó el objeto original antes de guardar. |
| `module ... does not open cl.dsy1102.arriendo.model to javafx.base` | Falta el `opens` para `PropertyValueFactory`. |
| La columna Estado muestra colores mezclados al ordenar | La celda no quita las clases anteriores. |
