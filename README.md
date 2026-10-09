# Ejemplos de POO y JavaFX · DSY1102

**DSY1102 · Programación Orientada a Objetos · Material de práctica para EA1 y EA2**

Casos **autoconclusivos**: cada carpeta es un proyecto Maven independiente, con su problema, su código inicial y su guía de resolución. Puedes abrir y resolver cualquiera sin haber hecho los anteriores.

> Material de práctica. No corresponde a una evaluación sumativa.

| Rama | Contenido |
|---|---|
| `main` | **Problema** (`README.md` de cada caso) y **código inicial** con `TODO`. Es lo que debes resolver. |
| `solucion` | **Código resuelto** y **guía de resolución** (`GUIA.md` de cada caso): qué se pide, dónde se resolvió, por qué y cuáles son los errores frecuentes. |

---

## Grupo 1 · Casos puntuales

Cada caso aborda **un tema** en 1 o 2 bloques.

| # | Caso | Tema | Tipo |
|---|---|---|---|
| 01 | [Herencia — Remuneraciones de la Ferretería El Tornillo](puntuales/01-herencia) | `extends`, `super`, sobrescritura, herencia de varios niveles | Consola + JUnit |
| 02 | [Polimorfismo — Medios de pago del Almacén Don Kiwi](puntuales/02-polimorfismo) | Clases abstractas, interfaces, despacho dinámico, abierto/cerrado | Consola + JUnit |
| 03 | [Excepciones — Movimientos de la Cuenta Kiwi](puntuales/03-excepciones) | *Checked* y *unchecked*, excepciones propias, `finally`, `try-with-resources` | Consola + JUnit |
| 04 | [Maven — Validador de RUT](puntuales/04-maven) | `pom.xml`, GAV, dependencias y `scope`, plugins, ciclo de vida, JAR ejecutable | Consola + JUnit |
| 05 | [JavaFX: ciclo de vida, Stage y Scene — Aforo de la ramada](puntuales/05-javafx-escenas) | `Application`, hilos, `Stage`/`Scene`, layouts en código, eventos | JavaFX sin FXML |
| 06 | [FXML y eventos — Taller de cueca](puntuales/06-fxml-eventos) | FXML, `@FXML`, `initialize`, controles, *bindings*, CSS, validación | JavaFX + FXML |
| 07 | [JavaFX Images — Parques Nacionales](puntuales/07-javafx-imagenes) | `Image`/`ImageView`, recursos, `setCellFactory`, `FileChooser`, carga en segundo plano | JavaFX + FXML |
| 08 | [TableView — Liga vecinal de baby fútbol](puntuales/08-tableview) | Propiedades JavaFX, `FilteredList`/`SortedList`, celdas, *extractor* | JavaFX + FXML |
| 09 | [Navegación — Picada La Tía Rosa](puntuales/09-navegacion) | `Navegador`, paso de parámetros, estado compartido, ventana modal | JavaFX + FXML |
| 10 | [Persistencia JSON — Agenda de contactos](puntuales/10-persistencia-json) | Jackson, subtipos, DAO, traducción de excepciones, `@TempDir` | Consola + JUnit |

## Grupo 2 · Casos completos

Aplicaciones de escritorio completas, **del tamaño de la tarea de la Fonda San Belarmino**: integran todos los temas de EA2. Cada una incluye el modelo resuelto (como si viniera de EA1) y pide construir la interfaz y la persistencia.

| # | Caso | Lo que agrega respecto de la Fonda |
|---|---|---|
| 01 | [Arriendo Cerro Alegre — bicicletas y scooters](completos/01-arriendo-bicicletas) | Estado que cambia y se persiste (disponible, batería), *template method*, copia antes de guardar, filtro con `CheckBox`. |
| 02 | [Biblioteca Gabriela Mistral — préstamo de libros y revistas](completos/02-biblioteca) | Valores calculados que **no** deben ir al JSON, validación cruzada, `Spinner` con máximo polimórfico, `TextInputDialog`, filtro combinado. |

Arquitectura común (MVC + Repository + DAO):

```
Vista (FXML) → Controlador → Repository<T> → XxxRepository → XxxDao → JsonXxxDao → data/xxx.json
                    └────────→ Modelo (reglas del negocio) ←────────┘
```

---

## Cómo trabajar

1. Pulsa **Fork** y clona tu copia. También puedes descargar solo la carpeta del caso que te interesa.
2. Abre **la carpeta del caso** (no la raíz del repositorio) como proyecto Maven en IntelliJ IDEA o NetBeans.
3. Lee su `README.md` y resuelve los requerimientos en orden. Haz commits a medida que avanzas.
4. Los casos de consola traen **pruebas de autoevaluación** (`mvn test`). Algunas no compilan hasta que crees las clases pedidas: ese es tu primer objetivo.
5. Cuando termines, o si te quedas atascado, compara con la solución:

```bash
git fetch origin
git diff origin/main origin/solucion -- puntuales/02-polimorfismo     # qué cambió
git checkout origin/solucion -- puntuales/02-polimorfismo/GUIA.md     # trae solo la guía
```

### Requisitos

| Herramienta | Versión |
|---|---|
| JDK | 25 (LTS) |
| Maven | 3.8 o superior (IntelliJ IDEA y NetBeans traen uno incorporado) |
| Scene Builder | 25 o superior (opcional, recomendado para los casos con FXML) |

Si tu equipo solo tiene JDK 21, cambia en el `pom.xml` del caso `maven.compiler.release` a `21` y, en los casos JavaFX, `javafx.version` a `21.0.6`. La versión mayor de JavaFX debe coincidir con la del JDK.

### Comandos (dentro de la carpeta del caso)

```bash
mvn compile              # compila
mvn test                 # pruebas de autoevaluación (casos de consola)
mvn compile exec:java    # ejecuta un programa de consola
mvn javafx:run           # ejecuta una aplicación JavaFX
```

> La Evaluación Parcial 2 se rinde **sin internet**. Ejecuta cada caso al menos una vez con conexión para que Maven descargue las dependencias y los plugins a `~/.m2`. Después funcionan con `mvn -o`.

Cada push compila todos los casos en GitHub Actions. En `main` solo se compila y en `solucion` también se ejecutan las pruebas.

## Condiciones de uso

- Resuelve **sin herramientas de inteligencia artificial** que generen el código: el valor de estos ejercicios está en detectar tus propios vacíos antes de las evaluaciones.
- Respeta las convenciones de Java: `PascalCase` para clases y `camelCase` para métodos, atributos y `fx:id`. Los `fx:id` llevan un prefijo según el control (`txtNombre`, `btnGuardar`, `cmbTipo`, `tblDatos`, `colNombre`, `lblMensaje`).

## Licencia

Material docente publicado bajo [CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/deed.es). El código que escribas en tu fork es tuyo y no queda cubierto por esta licencia.
