-- Paso 3: datos de prueba.
--   mysql -u root -p cooperativa_andes < sql/datos.sql

-- Los textos de este archivo tienen tildes: se le indica al cliente mysql que vienen en UTF-8.
SET NAMES utf8mb4;

INSERT INTO cuenta (numero, titular, saldo) VALUES
    ('1001', 'Panadería Los Andes SpA', 2500000),
    ('2001', 'Lorena Castillo',          150000),
    ('2002', 'Hernán Pizarro',            80000),
    ('2003', 'Javiera Molina',                0);
