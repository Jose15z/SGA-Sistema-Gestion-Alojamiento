-- =====================================================================
-- Datos de prueba de Siete Raíces (12.3). Se cargan solos al arrancar,
-- después de que Hibernate crea las tablas (defer-datasource-initialization).
-- Las fechas de las reservas son relativas a CURRENT_DATE para que la
-- colección de pruebas funcione cualquier día de la sustentación.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Usuarios (7.2). Contraseñas cifradas con BCrypt; las contraseñas de
-- prueba están en el README (Admin123*, Recepcion123*, Huesped123*).
-- ---------------------------------------------------------------------
INSERT INTO usuarios (identificacion, correo, documento, nombre_completo, rol, contrasena, activo) VALUES
('8d2f6a3e-1a1b-4c2d-9e01-000000000001', 'admin@sieteraices.co', '1000000001', 'Administración Siete Raíces', 'ADMINISTRADOR', '$2a$10$Z5uXGP3nGFwocmhV31hnT..mRDgUiRAptL9Jo0sdOg9iN40DzA71C', TRUE),
('8d2f6a3e-1a1b-4c2d-9e01-000000000002', 'recepcion@sieteraices.co', '1000000002', 'Recepción Siete Raíces', 'RECEPCIONISTA', '$2a$10$98Iokb67E2Soxbs0wpcld.wJ0httIpiTD79i1.GU3BtgK80AbgsAG', TRUE),
('8d2f6a3e-1a1b-4c2d-9e01-000000000003', 'ana.ramirez@correo.com', '1094123456', 'Ana Ramírez', 'HUESPED', '$2a$10$a6MxhB/3m4Mzsr09SdlOLujIyz1qJJwSOR3c8/ERcpsPuGmRWDqHW', TRUE),
('8d2f6a3e-1a1b-4c2d-9e01-000000000004', 'carlos.gomez@correo.com', '1094555666', 'Carlos Gómez', 'HUESPED', '$2a$10$a6MxhB/3m4Mzsr09SdlOLujIyz1qJJwSOR3c8/ERcpsPuGmRWDqHW', TRUE);

-- ---------------------------------------------------------------------
-- Apartamentos (Ficha A.2) y tarifas por temporada (Ficha A.3)
-- ---------------------------------------------------------------------
INSERT INTO apartamentos (identificacion, nombre, dormitorios, capacidad, estado_operativo, activo) VALUES
('APT-101', 'Café', 1, 2, 'PREPARADO', TRUE),
('APT-102', 'Guadua', 1, 3, 'PREPARADO', TRUE),
('APT-201', 'Palma', 2, 4, 'PREPARADO', TRUE),
('APT-202', 'Yarumo', 2, 5, 'PREPARADO', TRUE),
('APT-301', 'Orquídea', 2, 4, 'PREPARADO', TRUE),
('APT-302', 'Barranquero', 3, 6, 'PREPARADO', TRUE),
('APT-303', 'Cocora', 3, 6, 'PREPARADO', TRUE);

INSERT INTO apartamento_tarifas (apartamento_id, posicion, temporada, valor_por_ocupante_noche)
SELECT a.id, t.posicion, t.temporada, t.valor
FROM apartamentos a
JOIN (VALUES
    ('APT-101', 0, 'BASE', 70000), ('APT-101', 1, 'ALTA', 80000), ('APT-101', 2, 'ESPECIAL', 90000),
    ('APT-102', 0, 'BASE', 72000), ('APT-102', 1, 'ALTA', 82000), ('APT-102', 2, 'ESPECIAL', 92000),
    ('APT-201', 0, 'BASE', 75000), ('APT-201', 1, 'ALTA', 85000), ('APT-201', 2, 'ESPECIAL', 95000),
    ('APT-202', 0, 'BASE', 80000), ('APT-202', 1, 'ALTA', 90000), ('APT-202', 2, 'ESPECIAL', 100000),
    ('APT-301', 0, 'BASE', 78000), ('APT-301', 1, 'ALTA', 88000), ('APT-301', 2, 'ESPECIAL', 98000),
    ('APT-302', 0, 'BASE', 82000), ('APT-302', 1, 'ALTA', 92000), ('APT-302', 2, 'ESPECIAL', 102000),
    ('APT-303', 0, 'BASE', 85000), ('APT-303', 1, 'ALTA', 95000), ('APT-303', 2, 'ESPECIAL', 105000)
) AS t(apartamento, posicion, temporada, valor) ON t.apartamento = a.identificacion;

-- ---------------------------------------------------------------------
-- Temporadas (Ficha A.3). La BASE cubre toda fecha no asignada.
-- ---------------------------------------------------------------------
INSERT INTO temporadas (nombre, tipo, fecha_inicio, fecha_fin) VALUES
('Vacaciones de mitad de año 2026', 'ALTA', '2026-06-15', '2026-07-15'),
('Vacaciones de fin de año 2026', 'ALTA', '2026-12-01', '2027-01-15'),
('Semana Santa 2027', 'ESPECIAL', '2027-03-21', '2027-03-28'),
('Día de la Madre 2027', 'ESPECIAL', '2027-05-08', '2027-05-09'),
('Vacaciones de mitad de año 2027', 'ALTA', '2027-06-15', '2027-07-15'),
('Vacaciones de fin de año 2027', 'ALTA', '2027-12-01', '2028-01-15');

-- ---------------------------------------------------------------------
-- Política de cancelación vigente, versión 1 (Ficha A.4, L-10)
-- ---------------------------------------------------------------------
INSERT INTO politicas_cancelacion (version, porcentaje_no_show, hora_entrada, vigente)
VALUES (1, 50, '15:00:00', TRUE);

INSERT INTO politica_tramos (politica_id, posicion, antelacion_minima_horas, porcentaje)
SELECT p.id, t.posicion, t.horas, t.porcentaje
FROM politicas_cancelacion p
JOIN (VALUES (0, 168, 0), (1, 48, 30), (2, 0, 50)) AS t(posicion, horas, porcentaje) ON TRUE
WHERE p.version = 1;

-- ---------------------------------------------------------------------
-- Bloqueo vigente por mantenimiento (RN-07)
-- ---------------------------------------------------------------------
INSERT INTO bloqueos (identificacion, apartamento_id, fecha_entrada, fecha_salida, motivo, vigente, motivo_levantamiento)
VALUES ('5b0c1d2e-3f40-4a5b-8c6d-000000000001', 'APT-303', DATEADD('DAY', 10, CURRENT_DATE),
        DATEADD('DAY', 15, CURRENT_DATE), 'Mantenimiento de la cubierta', TRUE, NULL);

-- ---------------------------------------------------------------------
-- Reservas de ejemplo (códigos de 2025: el generador de 2026 empieza en 00001)
--  RES-2025-00001  Ana    APT-301  PENDIENTE  (creada hace 1 hora, con hora de llegada)
--  RES-2025-00002  Carlos APT-302  CONFIRMADA (entrada ayer: lista para registrar llegada)
--  RES-2025-00003  Ana    APT-201  CONFIRMADA (entrada ayer: lista para declarar no-show)
--  RES-2025-00004  Carlos APT-202  CANCELADA
--  RES-2025-00005  Ana    APT-101  FINALIZADA
-- ---------------------------------------------------------------------
INSERT INTO reservas (codigo, apartamento_id, fecha_entrada, fecha_salida, canal_origen, identificador_externo,
                      fecha_creacion, titular_documento, porcentaje_descuento, politica_version, estado,
                      hora_estimada_llegada, novedad_llegada, mascotas) VALUES
('RES-2025-00001', 'APT-301', DATEADD('DAY', 20, CURRENT_DATE), DATEADD('DAY', 23, CURRENT_DATE), 'PORTAL', NULL,
 DATEADD('HOUR', -1, LOCALTIMESTAMP), '1094123456', 0, 1, 'PENDIENTE', '16:30:00', NULL, 0),
('RES-2025-00002', 'APT-302', DATEADD('DAY', -1, CURRENT_DATE), DATEADD('DAY', 2, CURRENT_DATE), 'DIRECTO', NULL,
 DATEADD('DAY', -10, LOCALTIMESTAMP), '1094555666', 0, 1, 'CONFIRMADA', '15:00:00', NULL, 1),
('RES-2025-00003', 'APT-201', DATEADD('DAY', -1, CURRENT_DATE), DATEADD('DAY', 1, CURRENT_DATE), 'PORTAL', NULL,
 DATEADD('DAY', -12, LOCALTIMESTAMP), '1094123456', 0, 1, 'CONFIRMADA', '18:00:00', NULL, 0),
('RES-2025-00004', 'APT-202', DATEADD('DAY', 40, CURRENT_DATE), DATEADD('DAY', 42, CURRENT_DATE), 'DIRECTO', NULL,
 DATEADD('DAY', -5, LOCALTIMESTAMP), '1094555666', 0, 1, 'CANCELADA', NULL, NULL, 0),
('RES-2025-00005', 'APT-101', DATEADD('DAY', -5, CURRENT_DATE), DATEADD('DAY', -2, CURRENT_DATE), 'PORTAL', NULL,
 DATEADD('DAY', -20, LOCALTIMESTAMP), '1094123456', 0, 1, 'FINALIZADA', '15:30:00', NULL, 0);

INSERT INTO reserva_ocupantes (reserva_id, posicion, documento, nombre_completo, fecha_nacimiento)
SELECT r.id, o.posicion, o.documento, o.nombre, o.nacimiento
FROM reservas r
JOIN (VALUES
    ('RES-2025-00001', 0, '1094123456', 'Ana Ramírez', DATE '1990-05-12'),
    ('RES-2025-00001', 1, 'TI1030998877', 'Sofía Ramírez', DATE '2019-03-02'),
    ('RES-2025-00002', 0, '1094555666', 'Carlos Gómez', DATE '1985-08-30'),
    ('RES-2025-00002', 1, '1094555667', 'Laura Gómez', DATE '1987-02-14'),
    ('RES-2025-00003', 0, '1094123456', 'Ana Ramírez', DATE '1990-05-12'),
    ('RES-2025-00004', 0, '1094555666', 'Carlos Gómez', DATE '1985-08-30'),
    ('RES-2025-00005', 0, '1094123456', 'Ana Ramírez', DATE '1990-05-12'),
    ('RES-2025-00005', 1, '1094123457', 'Pedro Ramírez', DATE '1988-11-20')
) AS o(codigo, posicion, documento, nombre, nacimiento) ON o.codigo = r.codigo;

-- Valor congelado noche por noche (RN-05): temporada de la noche según el calendario
-- (BASE si ninguna la incluye) y tarifa del apartamento para esa temporada.
INSERT INTO reserva_cargos_noche (reserva_id, posicion, noche, temporada, tarifa_por_ocupante, ocupantes_facturables)
SELECT n.reserva_id, n.posicion, n.noche, n.temporada,
       (SELECT at.valor_por_ocupante_noche FROM apartamento_tarifas at
        JOIN apartamentos a ON a.id = at.apartamento_id
        WHERE a.identificacion = n.apartamento_id AND at.temporada = n.temporada),
       (SELECT COUNT(*) FROM reserva_ocupantes o
        WHERE o.reserva_id = n.reserva_id AND DATEADD('YEAR', 10, o.fecha_nacimiento) <= n.fecha_entrada)
FROM (
    SELECT r.id AS reserva_id, r.apartamento_id, r.fecha_entrada, CAST(x.X AS INT) AS posicion,
           DATEADD('DAY', x.X, r.fecha_entrada) AS noche,
           COALESCE((SELECT t.tipo FROM temporadas t
                     WHERE DATEADD('DAY', x.X, r.fecha_entrada) BETWEEN t.fecha_inicio AND t.fecha_fin), 'BASE') AS temporada
    FROM reservas r
    JOIN SYSTEM_RANGE(0, 60) x ON x.X < DATEDIFF('DAY', r.fecha_entrada, r.fecha_salida)
) n;

-- Historial cronológico de cada reserva
INSERT INTO reserva_historial (reserva_id, posicion, momento, accion, autor, estado_anterior, estado_nuevo, observacion)
SELECT r.id, h.posicion, DATEADD('MINUTE', h.minutos, r.fecha_creacion), h.accion, h.autor, h.anterior, h.nuevo, h.observacion
FROM reservas r
JOIN (VALUES
    ('RES-2025-00001', 0, 0, 'CREACION', 'ana.ramirez@correo.com', NULL, 'PENDIENTE', 'Reserva creada por el canal PORTAL'),
    ('RES-2025-00001', 1, 0, 'HORA_LLEGADA', 'ana.ramirez@correo.com', 'PENDIENTE', 'PENDIENTE', 'Hora estimada de llegada: 16:30'),
    ('RES-2025-00002', 0, 0, 'CREACION', 'recepcion@sieteraices.co', NULL, 'PENDIENTE', 'Reserva creada por el canal DIRECTO'),
    ('RES-2025-00002', 1, 0, 'HORA_LLEGADA', 'recepcion@sieteraices.co', 'PENDIENTE', 'PENDIENTE', 'Hora estimada de llegada: 15:00'),
    ('RES-2025-00002', 2, 0, 'MASCOTAS', 'recepcion@sieteraices.co', 'PENDIENTE', 'PENDIENTE', 'Mascotas: 1'),
    ('RES-2025-00002', 3, 60, 'CONFIRMACION', 'recepcion@sieteraices.co', 'PENDIENTE', 'CONFIRMADA', 'Reserva confirmada'),
    ('RES-2025-00003', 0, 0, 'CREACION', 'ana.ramirez@correo.com', NULL, 'PENDIENTE', 'Reserva creada por el canal PORTAL'),
    ('RES-2025-00003', 1, 0, 'HORA_LLEGADA', 'ana.ramirez@correo.com', 'PENDIENTE', 'PENDIENTE', 'Hora estimada de llegada: 18:00'),
    ('RES-2025-00003', 2, 120, 'CONFIRMACION', 'recepcion@sieteraices.co', 'PENDIENTE', 'CONFIRMADA', 'Reserva confirmada'),
    ('RES-2025-00004', 0, 0, 'CREACION', 'recepcion@sieteraices.co', NULL, 'PENDIENTE', 'Reserva creada por el canal DIRECTO'),
    ('RES-2025-00004', 1, 90, 'CANCELACION', 'recepcion@sieteraices.co', 'PENDIENTE', 'CANCELADA', 'Cambio de planes del huésped | política de la reserva: versión 1'),
    ('RES-2025-00005', 0, 0, 'CREACION', 'ana.ramirez@correo.com', NULL, 'PENDIENTE', 'Reserva creada por el canal PORTAL'),
    ('RES-2025-00005', 1, 0, 'HORA_LLEGADA', 'ana.ramirez@correo.com', 'PENDIENTE', 'PENDIENTE', 'Hora estimada de llegada: 15:30'),
    ('RES-2025-00005', 2, 60, 'CONFIRMACION', 'recepcion@sieteraices.co', 'PENDIENTE', 'CONFIRMADA', 'Reserva confirmada'),
    ('RES-2025-00005', 3, 21600, 'REGISTRO', 'recepcion@sieteraices.co', 'CONFIRMADA', 'EN_CURSO', 'El grupo tomó el apartamento'),
    ('RES-2025-00005', 4, 25920, 'SALIDA', 'recepcion@sieteraices.co', 'EN_CURSO', 'FINALIZADA', 'El grupo salió del apartamento')
) AS h(codigo, posicion, minutos, accion, autor, anterior, nuevo, observacion) ON h.codigo = r.codigo;

-- ---------------------------------------------------------------------
-- Folios (7.7): cargo de alojamiento igual al valor congelado de la reserva
-- ---------------------------------------------------------------------
INSERT INTO folios (codigo_reserva, cerrado, alojamiento_liquidado, autorizacion_cierre)
SELECT codigo, estado = 'FINALIZADA', estado = 'CANCELADA', NULL FROM reservas;

INSERT INTO folio_cargos (folio_id, posicion, tipo, concepto, valor, momento, autor)
SELECT f.id, 0, 'ALOJAMIENTO', 'Alojamiento',
       (SELECT SUM(c.tarifa_por_ocupante * c.ocupantes_facturables) FROM reserva_cargos_noche c WHERE c.reserva_id = r.id),
       r.fecha_creacion, 'recepcion@sieteraices.co'
FROM folios f JOIN reservas r ON r.codigo = f.codigo_reserva;

-- Reserva cancelada con más de 7 días de antelación: reverso total y penalidad 0 % (RN-13)
INSERT INTO folio_cargos (folio_id, posicion, tipo, concepto, valor, momento, autor)
SELECT f.id, t.posicion, t.tipo, t.concepto,
       CASE WHEN t.tipo = 'REVERSO_ALOJAMIENTO' THEN -c.valor ELSE 0 END,
       DATEADD('MINUTE', 90, r.fecha_creacion), 'recepcion@sieteraices.co'
FROM folios f
JOIN reservas r ON r.codigo = f.codigo_reserva
JOIN folio_cargos c ON c.folio_id = f.id AND c.tipo = 'ALOJAMIENTO'
JOIN (VALUES (1, 'REVERSO_ALOJAMIENTO', 'Reverso: Cancelación | política 1 | Cambio de planes del huésped'),
             (2, 'PENALIDAD', 'Cancelación | política 1 | Cambio de planes del huésped')) AS t(posicion, tipo, concepto) ON TRUE
WHERE r.codigo = 'RES-2025-00004';

-- Anticipo del 30 % (L-11) en las confirmadas y pago total en la finalizada
INSERT INTO folio_pagos (folio_id, posicion, identificacion, valor, medio, momento, concepto, autor)
SELECT f.id, 0, p.identificacion,
       CASE WHEN p.total THEN c.valor ELSE ROUND(c.valor * 0.3, 0) END,
       p.medio, DATEADD('MINUTE', 30, r.fecha_creacion), p.concepto, 'recepcion@sieteraices.co'
FROM folios f
JOIN reservas r ON r.codigo = f.codigo_reserva
JOIN folio_cargos c ON c.folio_id = f.id AND c.tipo = 'ALOJAMIENTO'
JOIN (VALUES
    ('RES-2025-00002', UUID '7a1e0c3b-2d4f-4e6a-9b8c-000000000002', 'TRANSFERENCIA_BANCARIA', 'Anticipo 30 % para confirmar', FALSE),
    ('RES-2025-00003', UUID '7a1e0c3b-2d4f-4e6a-9b8c-000000000003', 'TARJETA_CREDITO', 'Anticipo 30 % para confirmar', FALSE),
    ('RES-2025-00005', UUID '7a1e0c3b-2d4f-4e6a-9b8c-000000000005', 'EFECTIVO', 'Pago total de la estancia', TRUE)
) AS p(codigo, identificacion, medio, concepto, total) ON p.codigo = r.codigo;

-- Secuencia de códigos de 2025 alineada con las reservas de ejemplo
INSERT INTO secuencias_reserva (anio, ultimo) VALUES (2025, 5);
