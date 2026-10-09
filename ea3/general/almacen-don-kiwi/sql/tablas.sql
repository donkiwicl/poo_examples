-- Paso 2: crea (o reinicia) las tablas.
--   mysql -u root -p almacen_kiwi < sql/tablas.sql
-- Las pruebas ejecutan este mismo archivo sobre H2.

-- Primero las tablas que tienen claves foráneas hacia otras.
DROP TABLE IF EXISTS detalle_venta;
DROP TABLE IF EXISTS venta;
DROP TABLE IF EXISTS producto;

-- Herencia en una sola tabla: la columna tipo indica la subclase, y las
-- columnas propias de cada subclase admiten NULL.
CREATE TABLE producto (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    codigo            VARCHAR(6)  NOT NULL UNIQUE,
    nombre            VARCHAR(60) NOT NULL,
    tipo              VARCHAR(12) NOT NULL CHECK (tipo IN ('PERECIBLE', 'NO_PERECIBLE')),
    categoria         VARCHAR(12) NOT NULL,
    precio            INT         NOT NULL CHECK (precio > 0),
    stock             INT         NOT NULL CHECK (stock >= 0),
    fecha_vencimiento DATE        NULL,
    marca             VARCHAR(40) NULL
);

CREATE TABLE venta (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    fecha_hora DATETIME NOT NULL,
    total      INT      NOT NULL CHECK (total > 0)
);

CREATE TABLE detalle_venta (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    venta_id        INT NOT NULL,
    producto_id     INT NOT NULL,
    cantidad        INT NOT NULL CHECK (cantidad > 0),
    precio_unitario INT NOT NULL CHECK (precio_unitario > 0),
    FOREIGN KEY (venta_id) REFERENCES venta (id),
    FOREIGN KEY (producto_id) REFERENCES producto (id)
);
