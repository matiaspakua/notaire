-- Sample data for Notaire PostgreSQL
-- Extracted from MySQL dump

-- conceptos (4 records)
INSERT INTO conceptos (version, id_concepto, nombre, valor, porcentaje, habilitado, concepto_fijo) VALUES
(1, 1, 'IVA', 0, 20, true, false),
(1, 2, 'Honorarios', 0, 5, true, false),
(2, 3, 'Documentacion', 500, 0, true, true),
(1, 4, 'Protocolo', 150, 0, true, true);

-- Reset sequence
SELECT setval('conceptos_id_concepto_seq', (SELECT MAX(id_concepto) FROM conceptos));

-- estados_de_gestion (10 records)
INSERT INTO estados_de_gestion (version, id_estado_gestion, nombre, observaciones) VALUES
(0, 1, 'Iniciada', 'Inicio Gestion'),
(0, 2, 'En Tramite', ''),
(0, 3, 'Documentacion Completa', ''),
(0, 4, 'Archivada', ''),
(0, 5, 'Gestion Modificada', ''),
(0, 6, 'Gestion con Escritura Firmada', ''),
(0, 7, 'Gestion con Escritura Sin Firmar', ''),
(0, 8, 'Gestion con Escritura Anulada', ''),
(0, 9, 'Gestion con Escritura No Paso', ''),
(0, 10, 'Gestion con Escritura Inscripta', '');

-- Reset sequence
SELECT setval('estados_de_gestion_id_estado_gestion_seq', (SELECT MAX(id_estado_gestion) FROM estados_de_gestion));

-- tipos_identificacion (5 records)
INSERT INTO tipos_identificacion (version, id_tipo_identificacion, nombre, caracteres) VALUES
(0, 1, 'DNI', '8'),
(0, 2, 'LE', '8'),
(0, 3, 'LC', '8'),
(0, 4, 'Pasaporte', '12'),
(0, 5, 'CUIT', '11');

-- Reset sequence
SELECT setval('tipos_identificacion_id_tipo_identificacion_seq', (SELECT MAX(id_tipo_identificacion) FROM tipos_identificacion));

-- tipos_de_folio (3 records)
INSERT INTO tipos_de_folio (version, id_tipo_folio, nombre) VALUES
(0, 1, 'De documento'),
(0, 2, 'De actuacion'),
(0, 3, 'De certificacion');

-- Reset sequence
SELECT setval('tipos_de_folio_id_tipo_folio_seq', (SELECT MAX(id_tipo_folio) FROM tipos_de_folio));

-- tipos_de_tramite (sample records)
INSERT INTO tipos_de_tramite (version, id_tipo_tramite, nombre, observaciones, habilitado, se_archiva, se_inscribe, asocia_inmuebles) VALUES
(0, 1, 'Compraventa', 'Tramite de compraventa de inmuebles', true, true, true, true),
(0, 2, 'Donacion', 'Tramite de donacion', true, true, true, true),
(0, 3, 'Hipoteca', 'Tramite de hipoteca', true, true, true, true),
(0, 4, 'Sucesion', 'Tramite de sucesion', true, false, false, true),
(0, 5, 'Poder', 'Otorgamiento de poderes', true, true, false, false);

-- Reset sequence
SELECT setval('tipos_de_tramite_id_tipo_tramite_seq', (SELECT MAX(id_tipo_tramite) FROM tipos_de_tramite));

-- tipos_de_documento (sample records)
INSERT INTO tipos_de_documento (version, id_tipo_documento, nombre, devuelto, vence, dias_vencimiento, importe_a_pagar, habilitado, quien_entrega) VALUES
(0, 1, 'Libre Deuda Municipal', true, true, 30, 0, true, 'Entidad'),
(0, 2, 'Informe de Dominio', true, true, 60, 150, true, 'Entidad'),
(0, 3, 'Certificado Catastral', true, true, 90, 100, true, 'Entidad'),
(0, 4, 'Fotocopia DNI', false, false, NULL, 0, true, 'Cliente');

-- Reset sequence
SELECT setval('tipos_de_documento_id_tipo_documento_seq', (SELECT MAX(id_tipo_documento) FROM tipos_de_documento));

-- Sample persona (escribano)
INSERT INTO personas (
    version, id_persona, apellido, nombre, numero_identificacion, cuit, sexo, fecha_nacimiento,
    estado_civil, numero_nupcias, ocupacion, domicilio, e_mail, registro_escribano, es_cliente,
    localidad, provincia, telefono, celular, email, nacionalidad, profesion, observaciones,
    fk_id_tipo_identificacion, es_escribano
) VALUES (
    0, 1, 'Garcia', 'Juan Carlos', '20123456', '20-20123456-3', 'M', '1980-01-15',
    'Casado', 1, 'Escribano', 'Av. Principal 123', 'jcgarcia@notaria.com', 1001, false,
    'Buenos Aires', 'Buenos Aires', '011-4555-1234', '011-15-5555-1234', 'jcgarcia@notaria.com',
    'Argentina', 'Escribano', NULL, 1, true
);

-- Reset sequence
SELECT setval('personas_id_persona_seq', (SELECT MAX(id_persona) FROM personas));

-- Sample usuario (nombre/contrasenia for login; tipo: Escribano/Empleado; estado: habilitado)
-- Password: admin (almacenado como hash MD5)
-- MD5("admin") = 21232f297a57a5a743894a0e4a801fc3
INSERT INTO usuarios (version, id_usuario, nombre, contrasenia, tipo, estado, fk_id_persona) VALUES
(0, 1, 'admin', '21232f297a57a5a743894a0e4a801fc3', 'Escribano', true, 1);

-- Reset sequence
SELECT setval('usuarios_id_usuario_seq', (SELECT MAX(id_usuario) FROM usuarios));

-- =============================================================================
-- Workflow demo data (CU70/CU71 — issue #453): standard gestión workflow with a
-- fork, assigned to the main tipos de trámite, plus sample gestiones with
-- trámites and historial so the dashboard workflow tracker renders end-to-end.
-- =============================================================================

INSERT INTO workflow_definition (version, id_workflow_definition, nombre, descripcion, activo) VALUES
(0, 1, 'Workflow de Gestión Estándar', 'Ciclo de vida estándar de una gestión de escritura', true);

SELECT setval('workflow_definition_id_workflow_definition_seq', (SELECT MAX(id_workflow_definition) FROM workflow_definition));

INSERT INTO workflow_node (version, id_workflow_node, fk_workflow_definition_id, fk_estado_gestion_id, tipo) VALUES
(0, 1, 1, 1, 'INITIAL'),       -- Iniciada
(0, 2, 1, 2, 'INTERMEDIATE'),  -- En Tramite
(0, 3, 1, 3, 'INTERMEDIATE'),  -- Documentacion Completa
(0, 4, 1, 7, 'INTERMEDIATE'),  -- Escritura Sin Firmar
(0, 5, 1, 6, 'INTERMEDIATE'),  -- Escritura Firmada
(0, 6, 1, 10, 'FINAL'),        -- Escritura Inscripta
(0, 7, 1, 4, 'FINAL');         -- Archivada

SELECT setval('workflow_node_id_workflow_node_seq', (SELECT MAX(id_workflow_node) FROM workflow_node));

INSERT INTO workflow_transition (version, id_workflow_transition, fk_workflow_definition_id, fk_nodo_origen_id, fk_nodo_destino_id, condicion, descripcion) VALUES
(0, 1, 1, 1, 2, NULL, 'Se asignan trámites y comienza la gestión'),
(0, 2, 1, 2, 3, NULL, 'Toda la documentación requerida fue presentada'),
(0, 3, 1, 3, 4, NULL, 'Se redacta la escritura'),
(0, 4, 1, 4, 5, NULL, 'Las partes firman la escritura'),
(0, 5, 1, 5, 6, NULL, 'La escritura se inscribe en el registro'),
(0, 6, 1, 3, 7, NULL, 'La gestión se archiva sin escritura');

SELECT setval('workflow_transition_id_workflow_transition_seq', (SELECT MAX(id_workflow_transition) FROM workflow_transition));

UPDATE tipos_de_tramite SET fk_workflow_definition_id = 1 WHERE id_tipo_tramite IN (1, 2, 3);

INSERT INTO gestiones_de_escrituras (version, id_gestion, numero, fecha_inicio, encabezado, observaciones, fk_id_persona_escribano, fk_id_estado_de_gestion) VALUES
(0, 1, 1001, '2026-05-02', 'Compraventa Lote 12 — Familia Pérez', NULL, 1, 3),
(0, 2, 1002, '2026-05-20', 'Donación — Sucesión Gómez', NULL, 1, 2);

SELECT setval('gestiones_de_escrituras_id_gestion_seq', (SELECT MAX(id_gestion) FROM gestiones_de_escrituras));

INSERT INTO tramites (version, id_tramite, numero, nombre, observaciones, fk_id_tipo_tramite, fk_id_gestion) VALUES
(0, 1, 1, 'Compraventa inmueble Lote 12', NULL, 1, 1),
(0, 2, 2, 'Donación a herederos', NULL, 2, 2);

SELECT setval('tramites_id_tramite_seq', (SELECT MAX(id_tramite) FROM tramites));

INSERT INTO historial (version, id_historial, fecha, observaciones, fk_id_gestion, fk_id_estado_gestion) VALUES
(0, 1, '2026-05-02', 'Apertura de la gestión', 1, 1),
(0, 2, '2026-05-09', 'Trámite de compraventa en curso', 1, 2),
(0, 3, '2026-05-28', 'Documentación completa recibida', 1, 3),
(0, 4, '2026-05-20', 'Apertura de la gestión', 2, 1),
(0, 5, '2026-06-01', 'Trámite de donación en curso', 2, 2);

SELECT setval('historial_id_historial_seq', (SELECT MAX(id_historial) FROM historial));
