-- ======================================================
-- SISTEMA DE RESERVA DE RECURSOS - Proyecto #2 EIF206
-- Script de creación de base de datos MySQL
-- Traducción directa del modelo que ya tenían en XML
-- (Usuario/Administrador/Funcionario, Categoria, Recurso, Reserva)
-- ======================================================

-- OJO: borra la base completa y la crea de nuevo (úsese solo para empezar de cero).
DROP DATABASE IF EXISTS reservarecursos;
CREATE DATABASE reservarecursos;

USE reservarecursos;

-- Usuarios: se usa una sola tabla para Administrador y Funcionario
-- (igual que el objeto Java, que es una jerarquía simple), con
-- una columna "rol" como discriminador y "telefono" que solo
-- aplica (y se deja NULL) para los administradores.
CREATE TABLE usuarios (
    id              INT          NOT NULL,   -- lo digita el administrador (no es autoincremental)
    identificacion  VARCHAR(20)  NOT NULL,
    nombre          VARCHAR(80)  NOT NULL,
    clave           VARCHAR(50)  NOT NULL,
    rol             VARCHAR(20)  NOT NULL,   -- 'ADMINISTRADOR' o 'FUNCIONARIO'
    telefono        VARCHAR(20),
    PRIMARY KEY (id),
    UNIQUE (identificacion)
);

CREATE TABLE categorias (
    id          VARCHAR(15)  NOT NULL,       -- ej. CAT-000001
    descripcion VARCHAR(80)  NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE recursos (
    id          VARCHAR(20)  NOT NULL,       -- id/numero de activo, ej. 238715
    categoria   VARCHAR(15)  NOT NULL,
    descripcion VARCHAR(120) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (categoria) REFERENCES categorias(id)
);

CREATE TABLE reservas (
    id          VARCHAR(15)  NOT NULL,       -- ej. RES-000001
    actividad   VARCHAR(120) NOT NULL,
    fecha       DATE         NOT NULL,
    horaInicio  TIME         NOT NULL,
    horaFin     TIME         NOT NULL,
    funcionario INT          NOT NULL,
    estado      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVA',  -- 'ACTIVA' o 'CANCELADA'
    PRIMARY KEY (id),
    FOREIGN KEY (funcionario) REFERENCES usuarios(id)
);

-- Una reserva puede tener varios recursos asignados (uno por cada
-- categoría que se pidió), y un recurso puede aparecer en varias
-- reservas (en fechas/horas distintas) => tabla de enlace N:M.
CREATE TABLE reserva_recursos (
    reserva VARCHAR(15) NOT NULL,
    recurso VARCHAR(20) NOT NULL,
    PRIMARY KEY (reserva, recurso),
    FOREIGN KEY (reserva) REFERENCES reservas(id),
    FOREIGN KEY (recurso) REFERENCES recursos(id)
);

-- ======================================================
-- Datos semilla: los mismos usuarios de prueba que ya
-- tenían sembrados en Data.sembrarUsuariosDePrueba()
-- ======================================================

INSERT INTO usuarios (id, identificacion, nombre, clave, rol, telefono) VALUES
    (1, 'admin', 'Administrador', 'admin', 'ADMINISTRADOR', NULL),
    (2, '111',   'Juan Perez',    '111',   'FUNCIONARIO',   '3323'),
    (3, '222',   'Maria Perez',   '222',   'FUNCIONARIO',   '222222'),
    (444, '444', 'Keren Rojas',   '444',   'FUNCIONARIO',   '555555');

INSERT INTO categorias (id, descripcion) VALUES
    ('CAT-000001', 'Sala'),
    ('CAT-000002', 'Computadora'),
    ('CAT-000003', 'Proyector');

INSERT INTO recursos (id, categoria, descripcion) VALUES
    ('1001', 'CAT-000001', 'Sala de 10 personas'),
    ('1002', 'CAT-000001', 'Sala de 30 personas'),
    ('2004', 'CAT-000001', 'Sala de 30 personas'),
    ('1111', 'CAT-000002', 'Lenovo'),
    ('2222', 'CAT-000002', 'MAC Pro'),
    ('1122', 'CAT-000003', 'HP'),
    ('1234', 'CAT-000003', 'HP');
