# EA3 · Caso general · Almacén Don Kiwi — de JSON a MySQL

**DSY1102 · Programación Orientada a Objetos · Caso de práctica tipo Evaluación Parcial 3**

> Material de práctica. La guía de resolución y la implementación de referencia están en la rama `solucion` (`GUIA.md` en esta carpeta).

| | |
|---|---|
| **Indicadores** | IL 3.1 al IL 3.5 |
| **Tiempo estimado** | 180 minutos (como la EP3) |
| **Tecnologías** | Java 25 · Maven · JavaFX 25 · FXML · MySQL 8 · JDBC (`mysql-connector-j`) · H2 para las pruebas |
| **Temas** | Migrar la capa DAO de archivo a base de datos sin tocar los controladores, diseño de tablas (PK, FK, `UNIQUE`, `CHECK`, `NULL`), herencia en una tabla, `PreparedStatement`, transacciones, reportes con `JOIN` y `GROUP BY`, manejo de errores de base de datos en la interfaz |

---

## 1. Contexto

El **Almacén Don Kiwi**, en Valparaíso, usa desde EA2 una aplicación JavaFX para su inventario. Guarda los productos en `data/productos.json`. Funciona, pero ahora hay **dos cajas**: si dos personas guardan al mismo tiempo, una pisa los cambios de la otra, y no hay forma de registrar ventas ni sacar reportes.

Se decidió migrar a **MySQL**. La aplicación ya está bien separada en capas (MVC + Repository + DAO), así que la migración debería tocar **solo la capa de datos**. Después, con la base de datos funcionando, se agregan la **caja** (ventas) y los **reportes**.

```
Hoy:     Controladores → ProductoRepository → ProductoDao ◄── JsonProductoDao ──► data/productos.json

Meta:    Controladores → ProductoRepository → ProductoDao ◄── JdbcProductoDao ──┐
         VentaController ─► VentaRepository ─► VentaDao  ◄── JdbcVentaDao ─────┴─► ConexionBD ─► MySQL
         ReportesController ┘
```

## 2. Lo que ya existe (código inicial)

| Paquete / archivo | Estado |
|---|---|
| `model/` | **Resuelto** (viene de EA1). `Producto` (abstracta) con `ProductoPerecible` (fecha de vencimiento: con 3 días o menos se vende con 30 % de descuento, vencido no se vende) y `ProductoNoPerecible` (marca). `Categoria` (enum). `Venta` y `LineaVenta` (carro de compras con sus reglas). `ResumenCategoria` y `ProductoVendido` (filas de reportes). |
| `dao/ProductoDao`, `JsonProductoDao`, `PersistenciaException` | Versión EA2, funcionando. |
| `dao/VentaDao` | **Interfaz** ya definida por el jefe de proyecto. No tiene implementación. |
| `repository/Repository`, `ProductoRepository` | Resueltos. |
| `controller/PrincipalController`, `FormularioController`, `Alertas` | Resueltos: catálogo con búsqueda, filtro, CRUD y validación. |
| `Navegador`, `AppFX`, `view/*.fxml`, `styles.css`, `module-info.java` | Resueltos. |
| `sql/crear-bd.sql`, `sql/datos.sql` | Resueltos. `datos.sql` carga 12 productos y 3 ventas recientes. |
| `sql/tablas.sql` | **Vacío** (R2). |
| `datos-ejemplo/productos.json` | Un archivo de la versión JSON para probar la migración (R7). |
| `src/test/...` | 17 pruebas de la capa de datos con H2. **No compilan** hasta que crees las clases de R1, R3 y R5. |

Antes de empezar, ejecuta `mvn javafx:run`. Registra un par de productos y revisa `data/productos.json`: así funciona la versión EA2.

## 3. Requerimientos

### R1. Proyecto y conexión
- Agrega al `pom.xml`: `mysql-connector-j` 9.7.0, `h2` 2.3.232 y `junit-jupiter` 5.12.2, cada una con el `scope` correcto.
- Agrega `requires java.sql;` al `module-info.java`.
- Crea `src/main/resources/db.properties` (`db.url`, `db.usuario`, `db.clave`) y `dao/ConexionBD`, con el constructor `ConexionBD(Properties)`, `static desdeRecurso(String)` y `Connection abrir()`, como en el caso parcial 01.

### R2. Diseño de las tablas (`sql/tablas.sql`)
Escribe el `DROP TABLE IF EXISTS` (en el orden correcto) y el `CREATE TABLE` de:

| Tabla | Columnas |
|---|---|
| `producto` | `id` (PK, autoincremental) · `codigo` (único, 6 caracteres) · `nombre` (60) · `tipo` (`PERECIBLE` o `NO_PERECIBLE`) · `categoria` (nombre del enum, 12) · `precio` (entero > 0) · `stock` (entero ≥ 0) · `fecha_vencimiento` (fecha, solo perecibles) · `marca` (40, solo no perecibles) |
| `venta` | `id` (PK, autoincremental) · `fecha_hora` · `total` (entero > 0) |
| `detalle_venta` | `id` (PK, autoincremental) · `venta_id` (FK → venta) · `producto_id` (FK → producto) · `cantidad` (> 0) · `precio_unitario` (> 0) |

Decide qué columnas aceptan `NULL`. Ejecuta en MySQL `crear-bd.sql`, tu `tablas.sql` y `datos.sql`: los tres deben correr sin errores. Las pruebas también ejecutan tu `tablas.sql`.

### R3. Migrar los productos a MySQL
- Crea `dao/JdbcProductoDao implements ProductoDao` (constructor `JdbcProductoDao(ConexionBD)`):
  - `listarTodos()` por código, creando la **subclase** que indica la columna `tipo`;
  - `insertar` asigna el `id` generado;
  - mensajes exactos: `Ya existe un producto con el código AB-001.`; al eliminar un producto con ventas, un mensaje que contenga `tiene ventas registradas`; sin servidor, uno que **empiece** con `No hay conexión con la base de datos`.
- En `AppFX`, cambia `JsonProductoDao` por `JdbcProductoDao`.
- **Comprueba con `git diff` que no modificaste `ProductoRepository`, `PrincipalController` ni `FormularioController`.** Si tuviste que hacerlo, algo de tu DAO no respeta el contrato.

### R4. La aplicación no se cae
- Con MySQL detenido, la aplicación abre, muestra un `Alert` y queda vacía. Al iniciar MySQL, **Recargar** carga los datos.
- Si `db.properties` falta o está incompleto, muestra un `Alert` y se cierra.
- Ninguna clase fuera de `dao/` importa `java.sql`. Ninguna `SQLException` llega a un controlador.

### R5. Caja: registrar ventas
- `dao/JdbcVentaDao implements VentaDao`, método `registrar(Venta)`, en **una transacción**:
  1. inserta la `venta` (con su total) y obtiene su `id`;
  2. por cada línea, descuenta el stock **solo si alcanza** (`UPDATE ... WHERE id = ? AND stock >= ?`). Si no alcanza, otra caja vendió antes: lanza `PersistenciaException("Stock insuficiente de <nombre>...")` y **no se guarda nada**;
  3. inserta las líneas en `detalle_venta`;
  4. confirma y, recién entonces, asigna el `id` a la venta.
- `repository/VentaRepository`: `registrar(venta)` usa el DAO y luego **vuelve a cargar los productos** (también si falla), para que la tabla muestre el stock real.
- Vista `venta-view.fxml` + `VentaController`, a la que se llega desde un botón **Nueva venta** de la vista principal:
  - un `ComboBox` con los productos **con stock**, un `Spinner` de cantidad y **Agregar**, que usa `venta.agregar(...)` y muestra su mensaje en verde o su rechazo en rojo;
  - el precio de hoy del producto elegido (y el descuento, si corresponde);
  - una tabla de líneas (código, producto, cantidad, precio, subtotal), **Quitar línea** y el total;
  - **Confirmar venta** (con confirmación) muestra `Boleta N° 4 por $12.525.` y vuelve a la vista principal. Si falla, muestra el error y la venta sigue en pantalla. **Cancelar** pide confirmación si hay líneas.

### R6. Reportes con SQL
- En `JdbcVentaDao`: `ventasPorCategoria`, `masVendidos` y `contarVentas`. Los calcula **la base de datos** (`JOIN`, `GROUP BY`, `SUM`, `ORDER BY`, `LIMIT`), no Java.
- El período incluye **completos** los días «desde» y «hasta».
- Vista `reportes-view.fxml` + `ReportesController`, desde un botón **Reportes**: dos `DatePicker` (por defecto, los últimos 7 días), **Consultar**, un resumen `3 ventas · $28.350`, la tabla por categoría y los 5 productos más vendidos.

### R7. Migración de datos
Crea un programa de consola `MigrarDatos` que copie los productos de un archivo JSON a MySQL **usando las dos implementaciones de `ProductoDao`**: lee con una y escribe con la otra. Si un código ya existe, lo informa y sigue con el siguiente. Ejecútalo con `mvn compile exec:java -Dexec.args="datos-ejemplo/productos.json"`.

---

## 4. Pruebas que usará el docente

Con la base recién cargada (`tablas.sql` + `datos.sql`):

1. La vista principal muestra 12 productos. *Aceite maravilla* y *Agua mineral* destacan por stock bajo.
2. Nuevo producto con el código `AB-001` → `Ya existe un producto con el código AB-001.`
3. Editar el precio del azúcar, cerrar y abrir la aplicación → el cambio sigue ahí (está en MySQL, no en el JSON).
4. Eliminar el *Arroz* (tiene ventas) → mensaje claro y el producto sigue. Eliminar el *Cloro gel* (sin ventas) → se elimina.
5. Nueva venta: *Yogur* (vence en 2 días) → precio $273 con descuento. *Queso gauda* (vencido) → rechazado. 5 *Aceites* → `Stock insuficiente... quedan 4`. Confirmar 5 yogures + 4 aceites → `Boleta N° 4 por $12.525.`. El stock del aceite queda en 0 y el producto desaparece del `ComboBox` de la caja.
6. Reportes de los últimos 7 días, antes de la venta del punto 5: `3 ventas · $28.350`. Abarrotes primero, con 7 unidades y $8.430.
7. Detener MySQL → la aplicación abre con un aviso y sin excepciones en la consola. Encenderlo → **Recargar**.
8. `MigrarDatos` con `datos-ejemplo/productos.json` → 3 migrados y 1 omitido (AB-001).
9. Revisión del código: `git diff` de los controladores de EA2, ningún SQL concatenado, `try-with-resources` en todo el DAO y commits por requerimiento.

## 5. Cómo ejecutar

```bash
mysql -u root -p < sql/crear-bd.sql
mysql -u root -p almacen_kiwi < sql/tablas.sql
mysql -u root -p almacen_kiwi < sql/datos.sql

mvn test                                                       # objetivo: 17 pruebas en verde (H2)
mvn javafx:run                                                 # aplicación
mvn compile exec:java -Dexec.args="datos-ejemplo/productos.json"   # R7
```

> La EP3 se rinde **sin internet**. Ejecuta este caso al menos una vez con conexión para que Maven descargue el driver, H2 y los plugins.

## 6. Autoevaluación

| Dimensión | Pregunta de control |
|---|---|
| Conectividad y persistencia real | ¿El CRUD y las ventas quedan en MySQL y sobreviven al reinicio? |
| Estabilidad SQL | ¿Un nombre con comilla (`Pan d'Or`) se guarda bien? ¿Hay algún SQL armado con `+`? |
| Cohesión por capas | ¿Migrar de JSON a MySQL cambió solo `AppFX` y el paquete `dao`? |
| Gestión de excepciones JDBC | ¿Con MySQL detenido la aplicación informa y sigue viva? ¿Alguna `SQLException` llega a la vista? |
| Recursos | ¿Toda `Connection`, `PreparedStatement` y `ResultSet` se cierra con `try-with-resources`? |
| Control de versiones | ¿Un commit por requerimiento, con mensajes descriptivos? |
