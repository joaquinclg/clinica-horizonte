-- creacion de la base de datos

CREATE DATABASE IF NOT EXISTS clinica_horizonte
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE clinica_horizonte;

-- creacion de las tablas

DROP TABLE IF EXISTS usuarios;
CREATE TABLE usuarios (
  legajo      INT           NOT NULL,
  password    VARCHAR(255)  NOT NULL,
  nombre      VARCHAR(60)   NOT NULL,
  apellido    VARCHAR(60)   NOT NULL,
  rol         ENUM('ADMIN','AUXILIAR') NOT NULL,
  activo      TINYINT(1)    NOT NULL DEFAULT 1,
  creado_en   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (legajo)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS servicios;
CREATE TABLE servicios (
  id      INT          NOT NULL AUTO_INCREMENT,
  nombre  VARCHAR(60)  NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_servicio_nombre (nombre)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS insumos;
CREATE TABLE insumos (
  codigo            VARCHAR(30)   NOT NULL,
  nombre            VARCHAR(120)  NOT NULL,
  unidad            VARCHAR(20)   NOT NULL,
  stock             INT UNSIGNED  NOT NULL DEFAULT 0,
  stock_minimo      INT UNSIGNED  NOT NULL DEFAULT 0,
  estado            ENUM('ACTIVO','BLOQUEADO','BAJA') NOT NULL DEFAULT 'ACTIVO',
  fecha_vencimiento DATE NULL,
  PRIMARY KEY (codigo),
  CHECK (stock >= 0),
  CHECK (stock_minimo >= 0)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS movimientos;
CREATE TABLE movimientos (
  id             BIGINT        NOT NULL AUTO_INCREMENT,
  tipo           ENUM('INGRESO','EGRESO') NOT NULL,
  fecha          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  cantidad       INT UNSIGNED  NOT NULL,
  usuario_legajo INT           NOT NULL,
  insumo_codigo  VARCHAR(30)   NOT NULL,
  servicio_id    INT           NULL,
  PRIMARY KEY (id),
  KEY ix_mov_fecha    (fecha),
  KEY ix_mov_insumo   (insumo_codigo),
  KEY ix_mov_serv     (servicio_id),
  CONSTRAINT fk_mov_usuario  FOREIGN KEY (usuario_legajo)
      REFERENCES usuarios(legajo)
      ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_mov_insumo   FOREIGN KEY (insumo_codigo)
      REFERENCES insumos(codigo)
      ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_mov_servicio FOREIGN KEY (servicio_id)
      REFERENCES servicios(id)
      ON UPDATE CASCADE ON DELETE RESTRICT,
  CHECK (cantidad > 0),
  -- Regla: en INGRESO el servicio es NULL; en EGRESO es obligatorio
  CHECK ( (tipo='INGRESO' AND servicio_id IS NULL)
       OR (tipo='EGRESO'  AND servicio_id IS NOT NULL) )
) ENGINE=InnoDB;

-- inserciones

INSERT INTO usuarios (legajo, password, nombre, apellido, rol, activo)
VALUES
  (1000, 'admin123', 'Ana',  'García', 'ADMIN',   1),
  (2000, 'aux123',   'Luis', 'Pérez',  'AUXILIAR',1);

INSERT INTO servicios (nombre)
VALUES
  ('Guardia'),
  ('Internación'),
  ('Quirófano'),
  ('Consultorios');

INSERT INTO insumos (codigo, nombre, unidad, stock, stock_minimo, estado, fecha_vencimiento)
VALUES
  ('GUA-01', 'Guantes de látex',   'caja',    50, 10, 'ACTIVO',    NULL),
  ('GAS-01', 'Gasas estériles',    'pack',    12, 10, 'ACTIVO',    NULL),
  ('ALC-70', 'Alcohol 70%',        'botella',  5,  5, 'ACTIVO',    '2026-06-30'),
  ('JER-05', 'Jeringas 5 ml',      'unidad', 100, 40, 'ACTIVO',    NULL),
  ('BAR-01', 'Barbijo quirúrgico', 'caja',    25, 20, 'ACTIVO',    NULL);

INSERT INTO movimientos (tipo, cantidad, usuario_legajo, insumo_codigo, servicio_id)
VALUES ('INGRESO', 10, 1000, 'GAS-01', NULL);

INSERT INTO movimientos (tipo, cantidad, usuario_legajo, insumo_codigo, servicio_id)
VALUES ('EGRESO', 5, 2000, 'GUA-01',
        (SELECT id FROM servicios WHERE nombre='Guardia'));

INSERT INTO movimientos (tipo, cantidad, usuario_legajo, insumo_codigo, servicio_id)
VALUES ('EGRESO', 8, 2000, 'BAR-01',
        (SELECT id FROM servicios WHERE nombre='Consultorios'));

-- consultas

SELECT 
  m.id, m.fecha, m.tipo, m.cantidad,
  i.codigo  AS insumo_codigo,
  i.nombre  AS insumo_nombre,
  i.unidad,
  s.nombre  AS servicio,
  u.legajo,
  CONCAT(u.nombre, ' ', u.apellido) AS usuario,
  u.rol
FROM movimientos m
JOIN insumos   i ON i.codigo = m.insumo_codigo
LEFT JOIN servicios s ON s.id = m.servicio_id
JOIN usuarios  u ON u.legajo = m.usuario_legajo
ORDER BY m.fecha DESC, m.id DESC;

-- borrado de registro

DELETE FROM movimientos;
DELETE FROM insumos;
DELETE FROM servicios;
DELETE FROM usuarios;

SELECT 
  (SELECT COUNT(*) FROM usuarios)    AS usuarios,
  (SELECT COUNT(*) FROM servicios)   AS servicios,
  (SELECT COUNT(*) FROM insumos)     AS insumos,
  (SELECT COUNT(*) FROM movimientos) AS movimientos;