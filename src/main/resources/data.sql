-- =====================================================================
-- Datos de prueba de Siete Raíces (12.3). Se cargan solos al arrancar,
-- después de que Hibernate crea las tablas (defer-datasource-initialization).
-- Las fechas de las reservas son relativas a CURRENT_DATE para que la
-- colección de pruebas funcione cualquier día de la sustentación.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Usuarios (7.2). Contraseñas cifradas con BCrypt; las contraseñas de
-- prueba están en el README (Admin123*, Recepcion123*, Servicio123*,
-- Huesped123*). Hay un usuario por rol (12.3).
-- ---------------------------------------------------------------------
INSERT INTO usuarios (identificacion, correo, documento, fecha_nacimiento, nombre_completo, telefono, rol, contrasena, activo) VALUES
('8d2f6a3e-1a1b-4c2d-9e01-000000000001', 'admin@sieteraices.co', '1000000001', '1980-03-15', 'Administración Siete Raíces', '3104445566', 'ADMINISTRADOR', '$2a$10$Z5uXGP3nGFwocmhV31hnT..mRDgUiRAptL9Jo0sdOg9iN40DzA71C', TRUE),
('8d2f6a3e-1a1b-4c2d-9e01-000000000002', 'recepcion@sieteraices.co', '1000000002', '1992-07-01', 'Recepción Siete Raíces', '3104445567', 'RECEPCIONISTA', '$2a$10$98Iokb67E2Soxbs0wpcld.wJ0httIpiTD79i1.GU3BtgK80AbgsAG', TRUE),
('8d2f6a3e-1a1b-4c2d-9e01-000000000005', 'servicio@sieteraices.co', '1000000003', '1994-10-20', 'Personal de Servicio', '3104445568', 'PERSONAL_SERVICIO', '$2a$10$nU.g.JsetSm09nLLHX80Y./c6maEd5M5Eu9LICMb5dVaZ2.hQkWt.', TRUE),
('8d2f6a3e-1a1b-4c2d-9e01-000000000003', 'ana.ramirez@correo.com', '1094123456', '1990-05-12', 'Ana Ramírez', '3001112233', 'HUESPED', '$2a$10$a6MxhB/3m4Mzsr09SdlOLujIyz1qJJwSOR3c8/ERcpsPuGmRWDqHW', TRUE),
('8d2f6a3e-1a1b-4c2d-9e01-000000000004', 'carlos.gomez@correo.com', '1094555666', '1985-08-30', 'Carlos Gómez', '3002223344', 'HUESPED', '$2a$10$a6MxhB/3m4Mzsr09SdlOLujIyz1qJJwSOR3c8/ERcpsPuGmRWDqHW', TRUE);

-- ---------------------------------------------------------------------
-- Alojamiento (7.3): identidad de la Ficha (A.1) y servicios adicionales con
-- el cargo de la Ficha (A.5, L-17) y sus cinco normas de convivencia (A.4).
-- Solo la dirección exacta y las coordenadas siguen siendo datos de ejemplo
-- pendientes de confirmar por el equipo; no aparecen en la Ficha.
-- El parqueadero adicional no está en este catálogo porque lo cobra RP-02
-- al registrar el segundo vehículo.
-- ---------------------------------------------------------------------
INSERT INTO alojamiento (identificacion, nombre, descripcion, ciudad, direccion, latitud, longitud) VALUES
('3c4d5e6f-7a8b-4c9d-8e0f-000000000001', 'Siete Raíces – Apartamentos Turísticos',
 'Siete Raíces es un alojamiento turístico conformado por siete apartamentos independientes y autosuficientes, diseñado para recibir parejas, familias y grupos pequeños. Cada apartamento cuenta con características y capacidades diferentes, permitiendo ofrecer alternativas de alojamiento de acuerdo con el número de ocupantes y sus necesidades durante la estancia.',
 'Armenia, Quindío', 'Armenia, Quindío (dirección de ejemplo)', 4.533900, -75.681100);

INSERT INTO alojamiento_normas (alojamiento_id, posicion, norma) VALUES
('3c4d5e6f-7a8b-4c9d-8e0f-000000000001', 0, 'No se permite superar la capacidad máxima establecida para cada apartamento.'),
('3c4d5e6f-7a8b-4c9d-8e0f-000000000001', 1, 'Se debe mantener un nivel de ruido moderado, especialmente entre las 10:00 p. m. y las 7:00 a. m., con el fin de preservar el descanso de los demás huéspedes.'),
('3c4d5e6f-7a8b-4c9d-8e0f-000000000001', 2, 'No está permitido fumar dentro de los apartamentos ni en espacios interiores compartidos.'),
('3c4d5e6f-7a8b-4c9d-8e0f-000000000001', 3, 'Los huéspedes deben hacer uso adecuado del mobiliario, equipos y demás elementos del apartamento y reportar cualquier daño o novedad presentada durante la estancia.'),
('3c4d5e6f-7a8b-4c9d-8e0f-000000000001', 4, 'Se permite máximo una mascota por reserva, bajo la responsabilidad del titular y respetando las condiciones de convivencia del alojamiento.');

INSERT INTO alojamiento_servicios (alojamiento_id, posicion, codigo, nombre, genera_cargo, valor, unidad) VALUES
('3c4d5e6f-7a8b-4c9d-8e0f-000000000001', 0, 'LAVANDERIA', 'Servicio de lavandería', TRUE, 25000, 'POR_SOLICITUD'),
('3c4d5e6f-7a8b-4c9d-8e0f-000000000001', 1, 'DESAYUNO', 'Desayuno', TRUE, 25000, 'POR_PERSONA');

-- ---------------------------------------------------------------------
-- Apartamentos (Ficha A.2) y tarifas por temporada (Ficha A.3)
-- ---------------------------------------------------------------------
-- APT-202 está FUERA_DE_SERVICIO y APT-303 OCUPADO por la reserva EN_CURSO (12.3).
INSERT INTO apartamentos (identificacion, nombre, descripcion, dormitorios, capacidad, estado_operativo, activo) VALUES
-- Descripción y características diferenciadoras tomadas de la Ficha (A.2).
('APT-101', 'Café', 'Cama doble, cocina equipada, baño privado y balcón.', 1, 2, 'PREPARADO', TRUE),
('APT-102', 'Guadua', 'Cama doble, sofá cama, cocina equipada y espacio de trabajo.', 1, 3, 'PREPARADO', TRUE),
('APT-201', 'Palma', 'Dos dormitorios, sala, cocina equipada y balcón.', 2, 4, 'PREPARADO', TRUE),
('APT-202', 'Yarumo', 'Dos dormitorios, sala amplia, cocina equipada y espacio de trabajo.', 2, 5, 'FUERA_DE_SERVICIO', TRUE),
('APT-301', 'Orquídea', 'Dos dormitorios, cocina equipada, balcón y zona de descanso.', 2, 4, 'PREPARADO', TRUE),
('APT-302', 'Barranquero', 'Tres dormitorios, sala amplia, cocina equipada y dos baños.', 3, 6, 'PREPARADO', TRUE),
('APT-303', 'Cocora', 'Tres dormitorios, sala amplia, cocina equipada, dos baños y balcón panorámico.', 3, 6, 'OCUPADO', TRUE);

INSERT INTO apartamento_caracteristicas (apartamento_id, posicion, caracteristica)
SELECT a.id, c.posicion, c.caracteristica
FROM apartamentos a
JOIN (VALUES
    ('APT-101', 0, 'Cama doble'), ('APT-101', 1, 'Cocina equipada'), ('APT-101', 2, 'Baño privado'), ('APT-101', 3, 'Balcón'),
    ('APT-102', 0, 'Cama doble'), ('APT-102', 1, 'Sofá cama'), ('APT-102', 2, 'Cocina equipada'), ('APT-102', 3, 'Espacio de trabajo'),
    ('APT-201', 0, 'Sala'), ('APT-201', 1, 'Cocina equipada'), ('APT-201', 2, 'Balcón'),
    ('APT-202', 0, 'Sala amplia'), ('APT-202', 1, 'Cocina equipada'), ('APT-202', 2, 'Espacio de trabajo'),
    ('APT-301', 0, 'Cocina equipada'), ('APT-301', 1, 'Balcón'), ('APT-301', 2, 'Zona de descanso'),
    ('APT-302', 0, 'Sala amplia'), ('APT-302', 1, 'Cocina equipada'), ('APT-302', 2, 'Dos baños'),
    ('APT-303', 0, 'Sala amplia'), ('APT-303', 1, 'Cocina equipada'), ('APT-303', 2, 'Dos baños'), ('APT-303', 3, 'Balcón panorámico')
) AS c(apartamento, posicion, caracteristica) ON c.apartamento = a.identificacion;

-- Histórico de tarifas (7.4): la vigente de cada temporada es la más reciente y
-- coincide con la Ficha (A.3). Para mostrar el histórico, APT-301 tuvo antes una
-- tarifa BASE de 74.000 y hace 30 días pasó a la de la Ficha (78.000).
INSERT INTO apartamento_tarifas (apartamento_id, posicion, temporada, valor_por_ocupante_noche, vigente_desde)
SELECT a.id, t.posicion, t.temporada, t.valor, DATEADD('DAY', -90, LOCALTIMESTAMP)
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

UPDATE apartamento_tarifas SET valor_por_ocupante_noche = 74000
WHERE temporada = 'BASE' AND apartamento_id = (SELECT id FROM apartamentos WHERE identificacion = 'APT-301');

INSERT INTO apartamento_tarifas (apartamento_id, posicion, temporada, valor_por_ocupante_noche, vigente_desde)
SELECT id, 3, 'BASE', 78000, DATEADD('DAY', -30, LOCALTIMESTAMP) FROM apartamentos WHERE identificacion = 'APT-301';

-- Novedades (7.6) del apartamento fuera de servicio
INSERT INTO apartamento_novedades (apartamento_id, posicion, fecha, autor, descripcion, gravedad)
SELECT id, 0, DATEADD('DAY', -2, LOCALTIMESTAMP), 'servicio@sieteraices.co',
       'Filtración de agua en el baño principal', 'GRAVE'
FROM apartamentos WHERE identificacion = 'APT-202';

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
--  RES-2025-00006  Carlos APT-303  EN_CURSO   (llegó anteayer, sale en dos días)
--  RES-2025-00007  Ana    APT-202  NO_SHOW    (canal EXTERNO, identificador BK-778899)
-- Quedan representados todos los estados y los tres canales (12.3).
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
 DATEADD('DAY', -20, LOCALTIMESTAMP), '1094123456', 0, 1, 'FINALIZADA', '15:30:00', NULL, 0),
('RES-2025-00006', 'APT-303', DATEADD('DAY', -2, CURRENT_DATE), DATEADD('DAY', 2, CURRENT_DATE), 'DIRECTO', NULL,
 DATEADD('DAY', -15, LOCALTIMESTAMP), '1094555666', 0, 1, 'EN_CURSO', '14:00:00', NULL, 0),
('RES-2025-00007', 'APT-202', DATEADD('DAY', -4, CURRENT_DATE), DATEADD('DAY', -2, CURRENT_DATE), 'EXTERNO', 'BK-778899',
 DATEADD('DAY', -25, LOCALTIMESTAMP), '1094123456', 0, 1, 'NO_SHOW', '17:00:00', NULL, 0);

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
    ('RES-2025-00005', 1, '1094123457', 'Pedro Ramírez', DATE '1988-11-20'),
    ('RES-2025-00006', 0, '1094555666', 'Carlos Gómez', DATE '1985-08-30'),
    ('RES-2025-00006', 1, '1094555667', 'Laura Gómez', DATE '1987-02-14'),
    ('RES-2025-00006', 2, 'TI1030555111', 'Martín Gómez', DATE '2020-06-01'),
    ('RES-2025-00007', 0, '1094123456', 'Ana Ramírez', DATE '1990-05-12')
) AS o(codigo, posicion, documento, nombre, nacimiento) ON o.codigo = r.codigo;

-- Valor congelado noche por noche (RN-05): temporada de la noche según el calendario
-- (BASE si ninguna la incluye) y tarifa del apartamento para esa temporada.
INSERT INTO reserva_cargos_noche (reserva_id, posicion, noche, temporada, tarifa_por_ocupante, ocupantes_facturables)
SELECT n.reserva_id, n.posicion, n.noche, n.temporada,
       (SELECT at.valor_por_ocupante_noche FROM apartamento_tarifas at
        JOIN apartamentos a ON a.id = at.apartamento_id
        WHERE a.identificacion = n.apartamento_id AND at.temporada = n.temporada
        ORDER BY at.vigente_desde DESC LIMIT 1),
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
    ('RES-2025-00005', 4, 25920, 'SALIDA', 'recepcion@sieteraices.co', 'EN_CURSO', 'FINALIZADA', 'El grupo salió del apartamento'),
    ('RES-2025-00006', 0, 0, 'CREACION', 'recepcion@sieteraices.co', NULL, 'PENDIENTE', 'Reserva creada por el canal DIRECTO'),
    ('RES-2025-00006', 1, 0, 'HORA_LLEGADA', 'recepcion@sieteraices.co', 'PENDIENTE', 'PENDIENTE', 'Hora estimada de llegada: 14:00'),
    ('RES-2025-00006', 2, 60, 'CONFIRMACION', 'recepcion@sieteraices.co', 'PENDIENTE', 'CONFIRMADA', 'Reserva confirmada'),
    ('RES-2025-00006', 3, 18720, 'REGISTRO', 'recepcion@sieteraices.co', 'CONFIRMADA', 'EN_CURSO', 'El grupo tomó el apartamento'),
    ('RES-2025-00007', 0, 0, 'CREACION', 'canal-externo', NULL, 'PENDIENTE', 'Reserva creada por el canal EXTERNO'),
    ('RES-2025-00007', 1, 0, 'HORA_LLEGADA', 'canal-externo', 'PENDIENTE', 'PENDIENTE', 'Hora estimada de llegada: 17:00'),
    ('RES-2025-00007', 2, 60, 'CONFIRMACION', 'recepcion@sieteraices.co', 'PENDIENTE', 'CONFIRMADA', 'Reserva confirmada'),
    ('RES-2025-00007', 3, 30240, 'NO_SHOW', 'recepcion@sieteraices.co', 'CONFIRMADA', 'NO_SHOW', 'El titular no se presentó | política de la reserva: versión 1')
) AS h(codigo, posicion, minutos, accion, autor, anterior, nuevo, observacion) ON h.codigo = r.codigo;

-- ---------------------------------------------------------------------
-- Folios (7.7): cargo de alojamiento igual al valor congelado de la reserva
-- ---------------------------------------------------------------------
INSERT INTO folios (codigo_reserva, cerrado, alojamiento_liquidado)
SELECT codigo, estado = 'FINALIZADA', estado IN ('CANCELADA', 'NO_SHOW') FROM reservas;

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

-- No-show: reverso del alojamiento y penalidad del 50 % de la política versión 1
INSERT INTO folio_cargos (folio_id, posicion, tipo, concepto, valor, momento, autor)
SELECT f.id, t.posicion, t.tipo, t.concepto,
       CASE WHEN t.tipo = 'REVERSO_ALOJAMIENTO' THEN -c.valor ELSE ROUND(c.valor * 0.5, 0) END,
       DATEADD('MINUTE', 30240, r.fecha_creacion), 'recepcion@sieteraices.co'
FROM folios f
JOIN reservas r ON r.codigo = f.codigo_reserva
JOIN folio_cargos c ON c.folio_id = f.id AND c.tipo = 'ALOJAMIENTO'
JOIN (VALUES (1, 'REVERSO_ALOJAMIENTO', 'Reverso: No-show | política 1'),
             (2, 'PENALIDAD', 'No-show | política 1')) AS t(posicion, tipo, concepto) ON TRUE
WHERE r.codigo = 'RES-2025-00007';

-- Servicio adicional consumido por el grupo en curso (L-17): desayuno para 2 personas ($25.000 c/u, Ficha A.5)
INSERT INTO folio_cargos (folio_id, posicion, tipo, concepto, valor, momento, autor)
SELECT f.id, 1, 'SERVICIO_ADICIONAL', 'Desayuno x2 (POR_PERSONA)', 50000,
       DATEADD('MINUTE', 20000, r.fecha_creacion), 'recepcion@sieteraices.co'
FROM folios f JOIN reservas r ON r.codigo = f.codigo_reserva
WHERE r.codigo = 'RES-2025-00006';

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
    ('RES-2025-00005', UUID '7a1e0c3b-2d4f-4e6a-9b8c-000000000005', 'EFECTIVO', 'Pago total de la estancia', TRUE),
    ('RES-2025-00006', UUID '7a1e0c3b-2d4f-4e6a-9b8c-000000000006', 'TRANSFERENCIA_BANCARIA', 'Anticipo 30 % para confirmar', FALSE),
    ('RES-2025-00007', UUID '7a1e0c3b-2d4f-4e6a-9b8c-000000000007', 'TARJETA_CREDITO', 'Anticipo 30 % para confirmar', FALSE)
) AS p(codigo, identificacion, medio, concepto, total) ON p.codigo = r.codigo;

-- Secuencia de códigos de 2025 alineada con las reservas de ejemplo
INSERT INTO secuencias_reserva (anio, ultimo) VALUES (2025, 7);
