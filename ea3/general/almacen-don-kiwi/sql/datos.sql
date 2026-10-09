-- Paso 3: datos de prueba (después de crear las tablas).
--   mysql -u root -p almacen_kiwi < sql/datos.sql
-- Las fechas son relativas al día en que se ejecuta el script, para que siempre
-- haya un producto por vencer, uno vencido y ventas recientes.

-- Los textos de este archivo tienen tildes: se le indica al cliente mysql que vienen en UTF-8.
SET NAMES utf8mb4;

INSERT INTO producto (codigo, nombre, tipo, categoria, precio, stock, fecha_vencimiento, marca) VALUES
    ('AB-001', 'Arroz grado 1 1 kg',        'NO_PERECIBLE', 'ABARROTES', 1490, 30, NULL, 'Tucapel'),
    ('AB-002', 'Fideos spaghetti 400 g',    'NO_PERECIBLE', 'ABARROTES',  990, 40, NULL, 'Carozzi'),
    ('AB-003', 'Aceite maravilla 1 L',      'NO_PERECIBLE', 'ABARROTES', 2790,  4, NULL, 'Belmont'),
    ('AB-004', 'Azúcar 1 kg',               'NO_PERECIBLE', 'ABARROTES', 1290, 25, NULL, 'Iansa'),
    ('BE-001', 'Bebida cola 1,5 L',         'NO_PERECIBLE', 'BEBIDAS',   1890, 36, NULL, 'Andina'),
    ('BE-002', 'Agua mineral 1,6 L',        'NO_PERECIBLE', 'BEBIDAS',    990,  3, NULL, 'Cachantun'),
    ('LA-001', 'Leche entera 1 L',          'PERECIBLE',    'LACTEOS',   1190, 24, CURRENT_DATE + INTERVAL '20' DAY, NULL),
    ('LA-002', 'Yogur frutilla 125 g',      'PERECIBLE',    'LACTEOS',    390, 18, CURRENT_DATE + INTERVAL '2' DAY,  NULL),
    ('LA-003', 'Queso gauda 250 g',         'PERECIBLE',    'LACTEOS',   3490,  6, CURRENT_DATE - INTERVAL '1' DAY,  NULL),
    ('LI-001', 'Detergente líquido 3 L',    'NO_PERECIBLE', 'LIMPIEZA',  7990, 10, NULL, 'Omo'),
    ('LI-002', 'Cloro gel 900 ml',          'NO_PERECIBLE', 'LIMPIEZA',  1590, 15, NULL, 'Clorinda'),
    ('PA-001', 'Marraqueta (1 kg)',         'PERECIBLE',    'PANADERIA', 2200, 20, CURRENT_DATE + INTERVAL '1' DAY,  NULL);

-- Tres ventas recientes (ids 1, 2 y 3).
INSERT INTO venta (fecha_hora, total) VALUES
    (LOCALTIMESTAMP - INTERVAL '3' DAY,  6550),
    (LOCALTIMESTAMP - INTERVAL '1' DAY, 15730),
    (LOCALTIMESTAMP - INTERVAL '1' DAY,  6070);

INSERT INTO detalle_venta (venta_id, producto_id, cantidad, precio_unitario) VALUES
    (1, (SELECT id FROM producto WHERE codigo = 'AB-001'), 2, 1490),
    (1, (SELECT id FROM producto WHERE codigo = 'LA-001'), 3, 1190),
    (2, (SELECT id FROM producto WHERE codigo = 'BE-001'), 2, 1890),
    (2, (SELECT id FROM producto WHERE codigo = 'AB-002'), 4,  990),
    (2, (SELECT id FROM producto WHERE codigo = 'LI-001'), 1, 7990),
    (3, (SELECT id FROM producto WHERE codigo = 'LA-001'), 2, 1190),
    (3, (SELECT id FROM producto WHERE codigo = 'PA-001'), 1, 2200),
    (3, (SELECT id FROM producto WHERE codigo = 'AB-001'), 1, 1490);
