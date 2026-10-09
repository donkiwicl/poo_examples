-- Paso 2: crea (o reinicia) las tablas.
--   mysql -u root -p club_halcones < sql/tablas.sql
-- Las pruebas ejecutan este mismo archivo sobre H2.

DROP TABLE IF EXISTS socio;

CREATE TABLE socio (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    rut           VARCHAR(12) NOT NULL UNIQUE,
    nombre        VARCHAR(60) NOT NULL,
    categoria     VARCHAR(10) NOT NULL CHECK (categoria IN ('INFANTIL', 'ADULTO', 'SENIOR')),
    cuota_mensual INT         NOT NULL CHECK (cuota_mensual > 0),
    fecha_ingreso DATE        NOT NULL,
    activo        BOOLEAN     NOT NULL DEFAULT TRUE
);
