# EA3 · 05 · Refactorización del DAO e integración con JavaFX — Lavandería La Burbuja

**Tema:** detectar el acoplamiento entre interfaz y base de datos, extraer la conexión (`ConexionBD` + `db.properties`), interfaz DAO + implementación JDBC, traducción de `SQLException` a una excepción propia, inyección de dependencias en el controlador, `TableView` + `ObservableList` sincronizada con la base de datos, selección de fila → formulario, validación en el controlador, aplicación que no se cae sin servidor.
**Tipo:** JavaFX + FXML + JUnit (H2) · **Tiempo estimado:** 2 bloques · **Sesión:** 3.2 (PPT 3.2.1 y 3.2.2, guías 3.2.3 y 3.2.4)

> La guía de resolución está en la rama `solucion`, en el archivo `GUIA.md` de esta carpeta.

## Problema

Un estudiante en práctica hizo la aplicación de clientes de la **Lavandería La Burbuja**. Funciona, pero `ClienteController` lo hace todo: abre conexiones, arma el SQL concatenando texto, recorre el `ResultSet` y llena la tabla. Los usuarios reportan:

- buscar a la clienta **Elena O'Ryan** no hace nada;
- después de **Actualizar**, la tabla sigue mostrando los datos antiguos hasta reiniciar;
- si MySQL está detenido, la ventana abre vacía **sin ningún aviso**;
- se pueden guardar clientes con el teléfono vacío o un correo repetido, y no pasa nada visible;
- al rato de uso, MySQL rechaza conexiones nuevas (*Too many connections*).

Hay que **refactorizar**: separar el acceso a datos en una capa DAO y dejar el controlador a cargo solo de la interfaz, sin cambiar la vista.

```
Antes:   ClienteController ──(SQL, DriverManager)──► MySQL

Después: ClienteController ──► ClienteDao (interfaz) ◄── ClienteDaoJdbc ──► ConexionBD ──► MySQL
                                                                              ▲
                                                                       db.properties
```

## Código inicial

| Archivo | Estado |
|---|---|
| `sql/*.sql` | Resueltos. Base `lavanderia_burbuja`, tabla `cliente` (con `correo UNIQUE`) y 5 clientes. |
| `model/Cliente.java`, `view/clientes-view.fxml`, `estilos.css`, `module-info.java` | Resueltos. **La vista no debería cambiar.** |
| `AppLavanderia.java` | Funciona. La modificarás en R5. |
| `controller/ClienteController.java` | **Funciona, pero mal diseñado.** Es lo que hay que refactorizar. |
| `src/test/.../ClienteDaoJdbcTest.java` | 8 pruebas de la capa DAO con H2. **No compilan** hasta que crees las clases de R2 a R4: ese es tu primer objetivo. |

Antes de empezar: ejecuta los 3 scripts de `sql/`, ajusta la contraseña en el controlador si la tuya no es vacía y prueba la aplicación con `mvn javafx:run` para ver los problemas.

## Requerimientos

**R1. Diagnóstico.** Antes de tocar el código, anota en tu primer commit los problemas de `ClienteController` que explican cada reporte de los usuarios. Busca al menos seis: piensa en responsabilidades, seguridad, recursos, errores y sincronización.

**R2. Configuración y conexión.** Crea `src/main/resources/db.properties` (`db.url`, `db.usuario`, `db.clave`) y la clase `database/ConexionBD`, como en el caso 01:
- constructor `ConexionBD(Properties)`, que valida `db.url` y `db.usuario`;
- `static ConexionBD desdeRecurso(String)`;
- `Connection abrir() throws SQLException`.

**R3. Contrato.** En el paquete `dao`, crea:
- `DaoException extends Exception`, con un constructor `(String mensaje, Throwable causa)`;
- la interfaz `ClienteDao`, que lanza `DaoException`:

| Método | Qué hace |
|---|---|
| `List<Cliente> listarTodos()` | Todos, por nombre. |
| `List<Cliente> buscarPorNombre(String texto)` | Nombre que contiene el texto, sin distinguir mayúsculas. |
| `void insertar(Cliente cliente)` | Guarda y **asigna el id generado**. |
| `boolean actualizar(Cliente cliente)` | Según el id. `false` si ya no existe. |
| `boolean eliminar(int id)` | `false` si ya no existía. |

**R4. `ClienteDaoJdbc implements ClienteDao`.** Recibe una `ConexionBD` en su constructor. Es la **única** clase con SQL:
- `PreparedStatement` siempre, `try-with-resources` siempre, columnas por nombre;
- toda `SQLException` se traduce a `DaoException`, con la original como causa:
  - correo repetido (`SQLIntegrityConstraintViolationException`) → `"Ya existe un cliente con el correo <correo>."`;
  - sin conexión (`SQLState` que empieza con `08`) → un mensaje que **empiece** con `"No hay conexión con la base de datos"`;
  - otro error → `"No se pudo <acción>: <mensaje>"`.

**R5. Controlador desacoplado.**
- `ClienteController` no importa nada de `java.sql` ni conoce `ClienteDaoJdbc`. Recibe un `ClienteDao` con un método `inicializar(ClienteDao dao)`.
- `AppLavanderia` arma las capas (`ConexionBD` → `ClienteDaoJdbc`) y le entrega el DAO al controlador con `loader.getController()`. Si falta `db.properties` o está incompleto, muestra un `Alert` y cierra la aplicación.
- La tabla se enlaza a **una** `ObservableList`. Después de cada escritura (guardar, actualizar o eliminar) se **vuelve a leer** desde el DAO, respetando la búsqueda escrita.

**R6. Interacción.**
- Al seleccionar una fila, sus datos pasan al formulario. **Actualizar** y **Eliminar** se deshabilitan sin selección.
- Antes de llamar al DAO se valida: nombre obligatorio, teléfono de 9 dígitos, correo con formato válido y comuna obligatoria. Muestra **todos** los errores en un solo `Alert` y marca los campos.
- **Eliminar** pide confirmación.
- Toda `DaoException` se muestra en un `Alert`. La barra inferior informa el resultado (`Cliente Felipe Rivas registrado con el id 6.`).

**R7. Sin servidor.** Con MySQL detenido, la aplicación **abre igual**: muestra un `Alert`, deja la tabla vacía y la barra inferior en rojo (`Sin conexión con la base de datos. Pulsa Recargar para reintentar.`). Al iniciar MySQL, **Recargar** carga los datos sin reiniciar la aplicación.

## Pruebas que usará el docente

1. Buscar `o'ryan` → aparece Elena O'Ryan.
2. Guardar con el formulario vacío → un `Alert` con 4 errores. Teléfono `12345` → rechazado.
3. Guardar con el correo `bgodoy@correo.cl` → `Ya existe un cliente con el correo bgodoy@correo.cl.`
4. Seleccionar a Daniel, cambiar la comuna a `Peñalolén` y **Actualizar** → la tabla muestra el cambio al instante.
5. Eliminar a Bastián → pide confirmación y desaparece de la tabla y de la base de datos.
6. Detener MySQL y abrir la aplicación → aviso y barra roja, sin excepciones en la consola. Iniciar MySQL → **Recargar** funciona.
7. Revisar el código: ningún `import java.sql` en el controlador, ningún `+` dentro de un SQL y ningún `printStackTrace`.

## Cómo ejecutar

```bash
mvn test          # objetivo: 8 pruebas de la capa DAO en verde (H2)
mvn javafx:run    # necesita MySQL con los 3 scripts de sql/ ejecutados
```

## Preguntas para pensar

1. ¿Por qué cambiar la capa de datos (por ejemplo, de MySQL a PostgreSQL o a un archivo) no debería obligar a modificar el controlador ni la vista?
2. ¿En qué capa debe ocurrir el mapeo de `ResultSet` a `Cliente`? ¿Y la validación del formato del correo? ¿Y la regla «el correo no se repite»?
3. ¿Por qué el controlador recibe el DAO desde afuera en vez de crearlo con `new ClienteDaoJdbc(...)`?
4. Después de guardar, ¿por qué volver a leer desde la base de datos en vez de solo agregar el objeto a la lista?
5. ¿Qué ventaja tiene que `ClienteDao` lance `DaoException` y no `SQLException`?
