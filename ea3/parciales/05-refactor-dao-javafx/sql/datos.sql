-- Paso 3: datos de prueba.
--   mysql -u root -p lavanderia_burbuja < sql/datos.sql

-- Los textos de este archivo tienen tildes: se le indica al cliente mysql que vienen en UTF-8.
SET NAMES utf8mb4;

INSERT INTO cliente (nombre, telefono, correo, comuna) VALUES
    ('Andrea Valdés',   '912345678', 'andrea.valdes@correo.cl', 'Ñuñoa'),
    ('Bastián Godoy',   '923456789', 'bgodoy@correo.cl',        'Providencia'),
    ('Carolina Leiva',  '934567890', 'cleiva@correo.cl',        'Ñuñoa'),
    ('Daniel Fuentes',  '945678901', 'dfuentes@correo.cl',      'La Reina'),
    ('Elena O''Ryan',   '956789012', 'eoryan@correo.cl',        'Macul');
