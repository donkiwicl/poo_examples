-- Paso 2: crea (o reinicia) las tablas.
--   mysql -u root -p lavanderia_burbuja < sql/tablas.sql

DROP TABLE IF EXISTS cliente;

CREATE TABLE cliente (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    nombre   VARCHAR(60) NOT NULL,
    telefono VARCHAR(12) NOT NULL,
    correo   VARCHAR(80) NOT NULL UNIQUE,
    comuna   VARCHAR(40) NOT NULL
);
