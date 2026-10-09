# Ejemplos de POO y JavaFX · DSY1102

**DSY1102 · Programación Orientada a Objetos · Material de práctica para EA1, EA2 y EA3**

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
| 03 | [Clínica Veterinaria Patitas del Sur — pacientes e historial clínico](completos/03-veterinaria) | **Enums con atributos**, **composición** (historial anidado en el JSON), **fechas** (`LocalDate`, `DatePicker`, `jackson-datatype-jsr310`), vista **maestro-detalle**. |

Arquitectura común (MVC + Repository + DAO):

```
Vista (FXML) → Controlador → Repository<T> → XxxRepository → XxxDao → JsonXxxDao → data/xxx.json
                    └────────→ Modelo (reglas del negocio) ←────────┘
```

## Grupo 3 · EA3: aplicaciones con base de datos

Casos de la Experiencia de Aprendizaje 3 (JDBC con MySQL). Las **pruebas usan H2 en memoria en modo MySQL**, así que `mvn test` no necesita un servidor. Para ejecutar los programas sí se necesita MySQL 8: cada caso trae en `sql/` los scripts para crear su base de datos.

### Casos parciales

| # | Caso | Tema | Tipo |
|---|---|---|---|
| 01 | [Conexión JDBC — Camping Lago Ranco](ea3/parciales/01-conexion-jdbc) | Driver y `scope`, URL JDBC, `db.properties`, `ConexionBD`, diagnóstico de `SQLException`, `try-with-resources` | Consola + JUnit |
| 02 | [Consultas y ResultSet — Liceo Bicentenario de Talca](ea3/parciales/02-consultas-resultset) | `JOIN`/`LEFT JOIN`, `GROUP BY`/`HAVING`, mapeo de filas a objetos, `NULL` y `wasNull`, fechas | Consola + JUnit |
| 03 | [Sentencias parametrizadas — Club Deportivo Los Halcones](ea3/parciales/03-sentencias-parametrizadas) | `PreparedStatement`, inyección SQL, `LIKE` con parámetros, claves generadas, `UNIQUE` | Consola + JUnit |
| 04 | [Transacciones — Cooperativa de Ahorro Los Andes](ea3/parciales/04-transacciones) | `commit`/`rollback`, una transacción = una conexión, `UPDATE` condicional, lotes | Consola + JUnit |
| 05 | [Refactorización del DAO — Lavandería La Burbuja](ea3/parciales/05-refactor-dao-javafx) | Sacar el JDBC del controlador: interfaz DAO, `DaoException`, inyección de dependencias, `TableView` sincronizada, sin servidor | JavaFX + JUnit |

### Caso general (tipo EP3)

| Caso | Qué integra |
|---|---|
| [Almacén Don Kiwi — de JSON a MySQL](ea3/general/almacen-don-kiwi) | Migrar una aplicación de EA2 a MySQL **sin tocar sus controladores**, diseño de tablas (herencia en una tabla, FK), caja de ventas en **transacción**, **reportes** con `JOIN` y `GROUP BY`, migración de datos JSON → MySQL. |

```
Vista (FXML) → Controlador → Repository → XxxDao (interfaz) → JdbcXxxDao → ConexionBD → MySQL
                                                                   ▲
                                                            db.properties
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
| MySQL | 8.0 o superior (solo para ejecutar los casos de EA3; las pruebas no lo necesitan) |

Si tu equipo solo tiene JDK 21, cambia en el `pom.xml` del caso `maven.compiler.release` a `21` y, en los casos JavaFX, `javafx.version` a `21.0.6`. La versión mayor de JavaFX debe coincidir con la del JDK.

### Comandos (dentro de la carpeta del caso)

```bash
mvn compile              # compila
mvn test                 # pruebas de autoevaluación (casos de consola)
mvn compile exec:java    # ejecuta un programa de consola
mvn javafx:run           # ejecuta una aplicación JavaFX
```

> Las evaluaciones parciales se rinden **sin internet**. Ejecuta cada caso al menos una vez con conexión para que Maven descargue las dependencias y los plugins a `~/.m2`. Después funcionan con `mvn -o`.

Cada push compila todos los casos en GitHub Actions. En `main` solo se compila y en `solucion` también se ejecutan las pruebas.

## Condiciones de uso

- Resuelve **sin herramientas de inteligencia artificial** que generen el código: el valor de estos ejercicios está en detectar tus propios vacíos antes de las evaluaciones.
- Respeta las convenciones de Java: `PascalCase` para clases y `camelCase` para métodos, atributos y `fx:id`. Los `fx:id` llevan un prefijo según el control (`txtNombre`, `btnGuardar`, `cmbTipo`, `tblDatos`, `colNombre`, `lblMensaje`).

## Licencia

Material docente publicado bajo [CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/deed.es). El código que escribas en tu fork es tuyo y no queda cubierto por esta licencia.
