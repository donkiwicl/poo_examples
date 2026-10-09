# Guía de resolución · EA3 05 Refactorización del DAO e integración con JavaFX

> Para comparar tu trabajo con la solución: `git diff main solucion -- ea3/parciales/05-refactor-dao-javafx`

## Idea central

```
AppLavanderia (arma las capas)
   │  new ConexionBD(db.properties) → new ClienteDaoJdbc(conexion) → controlador.inicializar(dao)
   ▼
ClienteController ──usa──► ClienteDao (interfaz) ◄──implementa── ClienteDaoJdbc ──► ConexionBD ──► MySQL
   │  vista, validación,        lanza DaoException                 SQL, mapeo,
   │  mensajes                  (nunca SQLException)               traducción de errores
   ▼
ObservableList<Cliente> ◄── se recarga desde el DAO después de cada escritura
```

Cada clase tiene **una razón para cambiar**: la vista cambia si cambia el diseño, el controlador si cambia la interacción y el DAO si cambia la base de datos.

> Las guías 3.2.3 y 3.2.4 llaman a estas clases `DatabaseConnection` y `ClienteDaoMySql`. Aquí se usan `ConexionBD` (igual que en los casos 01 a 04) y `ClienteDaoJdbc`, porque la misma clase funciona con MySQL y con H2 en las pruebas.

## Paso 1 — Diagnóstico (R1)

| Problema en el código inicial | Consecuencia (reporte del usuario) |
|---|---|
| SQL concatenado (`"... LIKE '%" + txtBuscar.getText() + "%'"`) | La comilla de *O'Ryan* rompe la búsqueda. Además permite **inyección SQL**. |
| `Connection`, `Statement` y `ResultSet` nunca se cierran | Cada operación pierde una conexión: *Too many connections*. |
| `catch (Exception e) { e.printStackTrace(); }` | Los errores solo se ven en la consola: sin servidor, la ventana abre vacía y en silencio. Un correo repetido «no hace nada». |
| `onActualizar` no vuelve a leer la tabla | **Desincronización**: la pantalla muestra datos viejos. |
| `onEliminar` quita la fila de la tabla sin revisar el resultado | Si el `DELETE` falla, la pantalla y la base de datos quedan distintas. |
| URL, usuario y clave repetidos en 5 métodos | Cambiar la contraseña obliga a editar 5 lugares (y es fácil olvidar uno). |
| `rs.getString(2)` (columnas por posición) y `SELECT *` | Agregar una columna a la tabla desordena los datos sin dar error. |
| Sin validación | Clientes con el teléfono vacío o mal escrito. |
| `seleccionado.getId()` sin revisar `null` | `NullPointerException` al pulsar **Actualizar** sin una fila seleccionada. |
| Todo en una clase | No se puede probar el acceso a datos sin abrir la ventana, ni cambiar de motor sin tocar la interfaz. |

## Paso 2 — Conexión y configuración (R2)

Es la `ConexionBD` del caso 01, en el paquete `database`. Lo importante es que **solo existe un lugar** con la URL, el usuario y la clave (`db.properties`), y un solo lugar que llama a `DriverManager`.

## Paso 3 — El contrato (R3)

```java
public interface ClienteDao {
    List<Cliente> listarTodos() throws DaoException;
    List<Cliente> buscarPorNombre(String texto) throws DaoException;
    void insertar(Cliente cliente) throws DaoException;
    boolean actualizar(Cliente cliente) throws DaoException;
    boolean eliminar(int id) throws DaoException;
}
```

- **Pregunta 5:** `SQLException` es un detalle de JDBC. Si la interfaz la declarara, cualquier implementación (archivo, servicio web, memoria) tendría que inventar `SQLException` falsas, y el controlador tendría que importar `java.sql`. `DaoException` pertenece al **contrato**: dice «falló el acceso a datos» sin decir cómo se accede.
- `DaoException` es *checked*, igual que `PersistenciaException` en EA2: el compilador obliga al controlador a decidir qué mostrar.

## Paso 4 — La implementación JDBC (R4)

```java
@Override
public void insertar(Cliente cliente) throws DaoException {
    try (Connection con = conexion.abrir();
         PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
        asignarDatos(ps, cliente);
        ps.executeUpdate();
        try (ResultSet claves = ps.getGeneratedKeys()) {
            if (claves.next()) cliente.setId(claves.getInt(1));
        }
    } catch (SQLIntegrityConstraintViolationException e) {
        throw correoRepetido(cliente, e);
    } catch (SQLException e) {
        throw traducir("guardar el cliente", e);
    }
}

private DaoException traducir(String accion, SQLException e) {
    String estado = e.getSQLState() == null ? "" : e.getSQLState();
    if (estado.startsWith("08")) {
        return new DaoException("No hay conexión con la base de datos. Revisa que MySQL esté iniciado"
                + " y los datos de db.properties.", e);
    }
    return new DaoException("No se pudo " + accion + ": " + e.getMessage(), e);
}
```

- **El orden de los `catch` importa:** `SQLIntegrityConstraintViolationException` es una subclase de `SQLException`, así que va primero. Escrito después, el compilador lo rechaza porque nunca se alcanzaría.
- El SQL está en **constantes**: se lee completo de un vistazo y no se reconstruye en cada llamada.
- `asignarDatos` evita repetir los cuatro `setString` en `insertar` y `actualizar` (los cuatro primeros `?` son iguales).
- **Pregunta 2:** el mapeo `ResultSet` → `Cliente` ocurre **aquí** (`mapearTodos`). El controlador recibe objetos y nunca ve una fila. La regla «el correo no se repite» la garantiza la base de datos (`UNIQUE`). El DAO la traduce a un mensaje, y el controlador solo lo muestra.

## Paso 5 — El controlador (R5)

```java
private final ObservableList<Cliente> clientes = FXCollections.observableArrayList();
private ClienteDao dao;

@FXML
public void initialize() {
    ... columnas ...
    tblClientes.setItems(clientes);
}

public void inicializar(ClienteDao dao) {
    this.dao = dao;
    recargar();
}

private void recargar() {
    String texto = txtBuscar.getText().trim();
    try {
        clientes.setAll(texto.isEmpty() ? dao.listarTodos() : dao.buscarPorNombre(texto));
        ...
    } catch (DaoException e) {
        clientes.clear();
        mostrarEstado("Sin conexión con la base de datos. Pulsa Recargar para reintentar.", true);
        Alertas.error("No se pudieron cargar los clientes", e.getMessage());
    }
}
```

- **`initialize()` y `inicializar(dao)`:** FXML llama a `initialize()` al cargar la vista, **antes** de que exista el DAO. Por eso ahí solo se configura la tabla, y los datos se cargan en `inicializar`, que llama `AppLavanderia` después de `loader.load()`. Es el mismo patrón de paso de parámetros de EA2.
- **Pregunta 3 (inyección de dependencias):** si el controlador hiciera `new ClienteDaoJdbc(...)`, estaría amarrado a JDBC y a la configuración. Recibiendo la **interfaz**, la misma clase funciona con cualquier implementación (por ejemplo, una en memoria para mostrar la interfaz sin servidor). Solo `AppLavanderia` conoce las clases concretas.
- **Pregunta 4 (recargar después de escribir):** la base de datos es la **fuente de verdad**. Volver a leer trae el `id` generado, el orden correcto (`ORDER BY nombre`), el correo tal como quedó guardado y los cambios de **otros usuarios**. Agregar el objeto a mano a la lista sirve con un solo usuario, pero desincroniza en cuanto hay dos. El costo (un `SELECT` de pocas filas) es despreciable.
- `clientes.setAll(...)` reemplaza el contenido de **la misma** lista: la tabla está enlazada a ella y se actualiza sola. Con `tblClientes.setItems(new ...)` en cada carga, se pierde ese enlace.
- **Pregunta 1:** el controlador solo usa `ClienteDao`. Cambiar a PostgreSQL implica cambiar la URL de `db.properties` y, si hace falta, el SQL de `ClienteDaoJdbc`. El controlador y la vista no se tocan.

## Paso 6 — Interacción (R6)

- **Selección → formulario:** un *listener* sobre `selectedItemProperty()` copia los datos a los `TextField`.
- **Botones según el estado:** `btnActualizar.disableProperty().bind(...selectedItemProperty().isNull())` elimina el `NullPointerException` del código inicial: sin selección, el botón no se puede pulsar.
- **Actualizar no modifica el objeto de la tabla:** `leerFormulario()` crea un `Cliente` nuevo con el `id` del seleccionado. Si el `UPDATE` falla, la fila de la tabla sigue mostrando los datos guardados, no los que se intentaron guardar.
- **Validación en dos niveles:** el controlador valida el formato (vacíos, 9 dígitos, patrón de correo) antes de ir a la base de datos. La base de datos garantiza lo que solo ella puede verificar (`UNIQUE`). El correo se guarda en minúsculas para que `FRivas@Correo.cl` y `frivas@correo.cl` cuenten como el mismo.
- `actualizar` retorna `false` si otro usuario eliminó el cliente mientras se editaba. Se avisa y se recarga.

## Paso 7 — Sin servidor (R7)

`AppLavanderia` muestra la ventana (`stage.show()`) **antes** de cargar los datos. Si `listarTodos()` falla, el `Alert` aparece sobre la aplicación ya abierta, la barra queda en rojo y **Recargar** vuelve a intentar. Nunca se llama a `Platform.exit()` por un error de conexión: el servidor puede volver en un minuto. Solo se cierra si `db.properties` es inválido, porque eso no se arregla reintentando.

## Verificación

| Prueba | Resultado en la solución |
|---|---|
| `mvn test` | 8 pruebas del DAO en verde (H2 y MySQL detenido simulado con el puerto 1). |
| Prueba 1, buscar `o'ryan` | `1 cliente con "o'ryan" en el nombre` |
| Prueba 2, formulario vacío | Un `Alert` con los 4 errores. |
| Prueba 3, correo repetido | `Ya existe un cliente con el correo bgodoy@correo.cl.` |
| Prueba 4, actualizar | `Cliente Daniel Fuentes actualizado.` y la tabla muestra `Peñalolén`. |
| Prueba 5, eliminar | Confirmación y `Cliente Bastián Godoy eliminado.` |
| Prueba 6, sin servidor (puerto 3399) | `Alert` «No hay conexión con la base de datos...» y barra roja. |

## Errores frecuentes

| Síntoma | Causa |
|---|---|
| `NullPointerException` en `recargar()` al abrir | Se cargan los datos en `initialize()`, cuando el DAO todavía es `null`. |
| La tabla no se actualiza después de guardar | Se creó una lista nueva en vez de `clientes.setAll(...)`, o falta llamar a `recargar()`. |
| `No suitable driver` solo al ejecutar con `javafx:run` | Falta el driver en el `pom.xml` o tiene `scope test`. Con `module-info`, no hace falta declararlo (se carga como servicio), pero sí debe estar `requires java.sql`. |
| `package java.sql is not visible` al compilar | Falta `requires java.sql;` en `module-info.java`. |
| El controlador sigue con `import java.sql.SQLException` | Falta traducir en el DAO: la interfaz debe lanzar `DaoException`. |
| El correo repetido muestra «No se pudo guardar el cliente: Duplicate entry...» | Falta el `catch (SQLIntegrityConstraintViolationException)`. Debe ir antes del de `SQLException`: escrito después, no compila. |
