-- Carga inicial del catálogo en una transacción. El bloqueo serializa arranques concurrentes.
DO $$
BEGIN
    PERFORM pg_advisory_xact_lock(742931502);
    IF EXISTS (SELECT 1 FROM alojamiento) THEN
        RETURN;
    END IF;

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
    INSERT INTO apartamentos (identificacion, nombre, descripcion, dormitorios, capacidad, estado_operativo, activo) VALUES
    -- Descripción y características diferenciadoras tomadas de la Ficha (A.2).
    ('APT-101', 'Café', 'Cama doble, cocina equipada, baño privado y balcón.', 1, 2, 'PREPARADO', TRUE),
    ('APT-102', 'Guadua', 'Cama doble, sofá cama, cocina equipada y espacio de trabajo.', 1, 3, 'PREPARADO', TRUE),
    ('APT-201', 'Palma', 'Dos dormitorios, sala, cocina equipada y balcón.', 2, 4, 'PREPARADO', TRUE),
    ('APT-202', 'Yarumo', 'Dos dormitorios, sala amplia, cocina equipada y espacio de trabajo.', 2, 5, 'PREPARADO', TRUE),
    ('APT-301', 'Orquídea', 'Dos dormitorios, cocina equipada, balcón y zona de descanso.', 2, 4, 'PREPARADO', TRUE),
    ('APT-302', 'Barranquero', 'Tres dormitorios, sala amplia, cocina equipada y dos baños.', 3, 6, 'PREPARADO', TRUE),
    ('APT-303', 'Cocora', 'Tres dormitorios, sala amplia, cocina equipada, dos baños y balcón panorámico.', 3, 6, 'PREPARADO', TRUE);

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
    SELECT a.id, t.posicion, t.temporada, t.valor, LOCALTIMESTAMP - INTERVAL '90 days'
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
    SELECT id, 3, 'BASE', 78000, LOCALTIMESTAMP - INTERVAL '30 days' FROM apartamentos WHERE identificacion = 'APT-301';

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
END;
$$;
