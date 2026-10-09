-- Paso 3: datos de prueba.
--   mysql -u root -p club_halcones < sql/datos.sql
-- En SQL, una comilla dentro de un texto se escribe doble: 'O''Higgins'.

-- Los textos de este archivo tienen tildes: se le indica al cliente mysql que vienen en UTF-8.
SET NAMES utf8mb4;

INSERT INTO socio (rut, nombre, categoria, cuota_mensual, fecha_ingreso, activo) VALUES
    ('12.345.678-5', 'Bernardo O''Higgins Riquelme', 'SENIOR',   12000, '2019-03-01', TRUE),
    ('15.678.901-2', 'Camila Vergara',               'ADULTO',   18000, '2021-07-15', TRUE),
    ('16.789.012-3', 'Diego Araya',                  'ADULTO',   18000, '2022-01-10', TRUE),
    ('22.123.456-7', 'Florencia Tapia',              'INFANTIL',  9000, '2023-03-05', TRUE),
    ('22.234.567-8', 'Matías Tapia',                 'INFANTIL',  9000, '2023-03-05', FALSE),
    ('9.876.543-2',  'Rosa Henríquez',               'SENIOR',   12000, '2018-05-20', TRUE);
