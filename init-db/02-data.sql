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
INSERT INTO tipos_de_tramite (version, id_tipo_tramite, nombre, observaciones, habilitado) VALUES
(0, 1, 'Compraventa', 'Tramite de compraventa de inmuebles', true),
(0, 2, 'Donacion', 'Tramite de donacion', true),
(0, 3, 'Hipoteca', 'Tramite de hipoteca', true),
(0, 4, 'Sucesion', 'Tramite de sucesion', true),
(0, 5, 'Poder', 'Otorgamiento de poderes', true);

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
INSERT INTO personas (version, id_persona, apellido, nombre, domicilio, localidad, provincia, telefono, celular, email, nacionalidad, profesion, estado_civil, observaciones, es_escribano, fk_id_tipo_identificacion, numero_identificacion) VALUES
(0, 1, 'Garcia', 'Juan Carlos', 'Av. Principal 123', 'Buenos Aires', 'Buenos Aires', '011-4555-1234', '011-15-5555-1234', 'jcgarcia@notaria.com', 'Argentina', 'Escribano', 'Casado', NULL, true, 1, '20123456');

-- Reset sequence
SELECT setval('personas_id_persona_seq', (SELECT MAX(id_persona) FROM personas));

-- Sample usuario (nombre/contrasenia for login; tipo: Escribano/Empleado; estado: habilitado)
INSERT INTO usuarios (version, id_usuario, nombre, contrasenia, tipo, estado, fk_id_persona) VALUES
(0, 1, 'admin', '21232f297a57a5a743894a0e4a801fc3', 'Escribano', true, 1);

-- Reset sequence  
SELECT setval('usuarios_id_usuario_seq', (SELECT MAX(id_usuario) FROM usuarios));
