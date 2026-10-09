-- Paso 2: crea (o reinicia) las tablas.
--   mysql -u root -p liceo_talca < sql/tablas.sql
-- Las pruebas ejecutan este mismo archivo sobre H2.

DROP TABLE IF EXISTS nota;
DROP TABLE IF EXISTS alumno;
DROP TABLE IF EXISTS curso;

CREATE TABLE curso (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    nombre        VARCHAR(10) NOT NULL UNIQUE,
    profesor_jefe VARCHAR(60) NOT NULL
);

CREATE TABLE alumno (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    rut              VARCHAR(12) NOT NULL UNIQUE,
    nombre           VARCHAR(60) NOT NULL,
    fecha_nacimiento DATE        NOT NULL,
    correo_apoderado VARCHAR(80) NULL,
    curso_id         INT         NOT NULL,
    FOREIGN KEY (curso_id) REFERENCES curso (id)
);

CREATE TABLE nota (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    alumno_id  INT          NOT NULL,
    asignatura VARCHAR(30)  NOT NULL,
    valor      DECIMAL(2,1) NOT NULL CHECK (valor BETWEEN 1.0 AND 7.0),
    FOREIGN KEY (alumno_id) REFERENCES alumno (id)
);
