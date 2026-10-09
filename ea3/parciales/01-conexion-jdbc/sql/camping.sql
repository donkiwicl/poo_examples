-- Base de datos del Camping Lago Ranco.
-- Ejecuta este script una vez en tu servidor MySQL local, por ejemplo:
--   mysql -u root -p < sql/camping.sql
-- o ábrelo en MySQL Workbench y ejecútalo completo.

CREATE DATABASE IF NOT EXISTS camping_ranco;
USE camping_ranco;

DROP TABLE IF EXISTS sitio;

CREATE TABLE sitio (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    codigo    VARCHAR(5)  NOT NULL UNIQUE,
    tipo      VARCHAR(10) NOT NULL,
    capacidad INT         NOT NULL CHECK (capacidad > 0),
    valor     INT         NOT NULL CHECK (valor > 0)
);

INSERT INTO sitio (codigo, tipo, capacidad, valor) VALUES
    ('C-01', 'CARPA',    4, 18000),
    ('C-02', 'CARPA',    6, 22000),
    ('Q-01', 'QUINCHO', 12, 45000);
