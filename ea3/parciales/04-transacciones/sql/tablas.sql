-- Paso 2: crea (o reinicia) las tablas.
--   mysql -u root -p cooperativa_andes < sql/tablas.sql
-- Las pruebas ejecutan este mismo archivo sobre H2.
-- En MySQL, las transacciones requieren tablas InnoDB (el motor por defecto desde la versión 5.5).

DROP TABLE IF EXISTS movimiento;
DROP TABLE IF EXISTS cuenta;

CREATE TABLE cuenta (
    numero  VARCHAR(10) PRIMARY KEY,
    titular VARCHAR(60) NOT NULL,
    saldo   BIGINT      NOT NULL CHECK (saldo >= 0)
);

CREATE TABLE movimiento (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    numero_cuenta VARCHAR(10) NOT NULL,
    fecha_hora    DATETIME    NOT NULL,
    tipo          VARCHAR(5)  NOT NULL,
    monto         BIGINT      NOT NULL CHECK (monto > 0),
    glosa         VARCHAR(80) NOT NULL,
    FOREIGN KEY (numero_cuenta) REFERENCES cuenta (numero)
);
