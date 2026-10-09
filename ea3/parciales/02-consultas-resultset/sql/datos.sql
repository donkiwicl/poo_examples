-- Paso 3: datos de prueba.
--   mysql -u root -p liceo_talca < sql/datos.sql

-- Los textos de este archivo tienen tildes: se le indica al cliente mysql que vienen en UTF-8.
SET NAMES utf8mb4;

INSERT INTO curso (id, nombre, profesor_jefe) VALUES
    (1, '3°A', 'Patricia Muñoz'),
    (2, '3°B', 'Jorge Contreras');

INSERT INTO alumno (id, rut, nombre, fecha_nacimiento, correo_apoderado, curso_id) VALUES
    (1, '21.345.678-9', 'Valentina Rojas', '2009-04-12', 'mrojas@correo.cl',   1),
    (2, '21.456.789-K', 'Benjamín Soto',   '2009-08-30', NULL,                 1),
    (3, '21.567.890-1', 'Martina Pérez',   '2009-01-15', 'lperez@correo.cl',   1),
    (4, '21.678.901-2', 'Tomás Fuentes',   '2008-11-03', 'cfuentes@correo.cl', 2),
    (5, '21.789.012-3', 'Isidora Muñoz',   '2009-06-21', NULL,                 2),
    (6, '21.890.123-4', 'Agustín Díaz',    '2009-02-08', 'rdiaz@correo.cl',    2);

-- Agustín Díaz llegó transferido y todavía no tiene notas.
INSERT INTO nota (alumno_id, asignatura, valor) VALUES
    (1, 'Matemática', 6.5), (1, 'Matemática', 6.8), (1, 'Lenguaje', 6.0), (1, 'Historia', 5.9),
    (2, 'Matemática', 3.2), (2, 'Matemática', 4.0), (2, 'Lenguaje', 3.8), (2, 'Historia', 4.1),
    (3, 'Matemática', 5.5), (3, 'Lenguaje',   6.2), (3, 'Lenguaje', 5.8), (3, 'Historia', 6.0),
    (4, 'Matemática', 4.5), (4, 'Lenguaje',   3.0), (4, 'Historia', 3.5), (4, 'Historia', 3.9),
    (5, 'Matemática', 7.0), (5, 'Lenguaje',   6.6), (5, 'Historia', 6.9);
