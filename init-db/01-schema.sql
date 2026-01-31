-- PostgreSQL initialization script for Notaire
-- Converted from MySQL dump

-- Drop tables if they exist (for clean rebuild)
DROP TABLE IF EXISTS tramites_personas CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;
DROP TABLE IF EXISTS registro_auditoria CASCADE;
DROP TABLE IF EXISTS suplencias CASCADE;
DROP TABLE IF EXISTS pagos CASCADE;
DROP TABLE IF EXISTS items CASCADE;
DROP TABLE IF EXISTS movimientos_testimonio CASCADE;
DROP TABLE IF EXISTS copias CASCADE;
DROP TABLE IF EXISTS folios_copias CASCADE;
DROP TABLE IF EXISTS folios CASCADE;
DROP TABLE IF EXISTS testimonios CASCADE;
DROP TABLE IF EXISTS documentos_presentados CASCADE;
DROP TABLE IF EXISTS tramites CASCADE;
DROP TABLE IF EXISTS presupuestos CASCADE;
DROP TABLE IF EXISTS plantilla_presupuestos CASCADE;
DROP TABLE IF EXISTS plantilla_tramites CASCADE;
DROP TABLE IF EXISTS historial CASCADE;
DROP TABLE IF EXISTS gestiones_de_escrituras CASCADE;
DROP TABLE IF EXISTS escrituras CASCADE;
DROP TABLE IF EXISTS inmuebles CASCADE;
DROP TABLE IF EXISTS personas CASCADE;
DROP TABLE IF EXISTS tipos_identificacion CASCADE;
DROP TABLE IF EXISTS tipos_de_tramite CASCADE;
DROP TABLE IF EXISTS tipos_de_folio CASCADE;
DROP TABLE IF EXISTS tipos_de_documento CASCADE;
DROP TABLE IF EXISTS estados_de_gestion CASCADE;
DROP TABLE IF EXISTS conceptos CASCADE;

-- Table: conceptos
CREATE TABLE conceptos (
  version integer NOT NULL,
  id_concepto SERIAL PRIMARY KEY,
  nombre text NOT NULL,
  valor real NOT NULL DEFAULT 0,
  porcentaje integer NOT NULL DEFAULT 0,
  habilitado boolean NOT NULL,
  concepto_fijo boolean NOT NULL
);

-- Table: estados_de_gestion
CREATE TABLE estados_de_gestion (
  version integer NOT NULL,
  id_estado_gestion SERIAL PRIMARY KEY,
  nombre text NOT NULL,
  observaciones text
);

-- Table: tipos_de_documento
CREATE TABLE tipos_de_documento (
  version integer NOT NULL,
  id_tipo_documento SERIAL PRIMARY KEY,
  nombre text NOT NULL,
  devuelto boolean NOT NULL,
  vence boolean NOT NULL,
  dias_vencimiento integer,
  importe_a_pagar real,
  habilitado boolean NOT NULL,
  quien_entrega text NOT NULL
);

-- Table: tipos_de_folio
CREATE TABLE tipos_de_folio (
  version integer NOT NULL,
  id_tipo_folio SERIAL PRIMARY KEY,
  nombre text NOT NULL
);

-- Table: tipos_de_tramite
CREATE TABLE tipos_de_tramite (
  version integer NOT NULL,
  id_tipo_tramite SERIAL PRIMARY KEY,
  nombre text NOT NULL,
  observaciones text,
  habilitado boolean NOT NULL
);

-- Table: tipos_identificacion
CREATE TABLE tipos_identificacion (
  version integer NOT NULL,
  id_tipo_identificacion SERIAL PRIMARY KEY,
  nombre text NOT NULL,
  caracteres text NOT NULL
);

-- Table: personas
CREATE TABLE personas (
  version integer NOT NULL,
  id_persona SERIAL PRIMARY KEY,
  apellido text NOT NULL,
  nombre text NOT NULL,
  domicilio text,
  localidad text,
  provincia text,
  telefono text,
  celular text,
  email text,
  nacionalidad text,
  profesion text,
  estado_civil text,
  observaciones text,
  es_escribano boolean NOT NULL,
  fk_id_tipo_identificacion integer REFERENCES tipos_identificacion(id_tipo_identificacion),
  numero_identificacion text
);

-- Table: inmuebles
CREATE TABLE inmuebles (
  version integer NOT NULL,
  id_inmueble SERIAL PRIMARY KEY,
  nomenclatura text,
  matricula text,
  valuacion_anio integer,
  valuacion_fiscal real,
  partida integer,
  circunscripcion text,
  seccion text,
  zona text,
  manzana text,
  parcela text,
  poligono text,
  unidad_funcional text,
  domicilio text,
  localidad text
);

-- Table: escrituras
CREATE TABLE escrituras (
  version integer NOT NULL,
  id_escritura SERIAL PRIMARY KEY,
  numero integer NOT NULL,
  fecha_escrituracion date NOT NULL,
  cuerpo text NOT NULL,
  estado text NOT NULL,
  matricula_inscripcion text,
  fecha_inscripcion date,
  observaciones text
);

-- Table: gestiones_de_escrituras
CREATE TABLE gestiones_de_escrituras (
  version integer NOT NULL,
  id_gestion SERIAL PRIMARY KEY,
  numero integer NOT NULL,
  fecha_inicio date NOT NULL,
  encabezado text NOT NULL,
  observaciones text,
  fk_id_persona_escribano integer REFERENCES personas(id_persona),
  fk_id_estado_de_gestion integer REFERENCES estados_de_gestion(id_estado_gestion)
);

-- Table: historial
CREATE TABLE historial (
  version integer NOT NULL,
  id_historial SERIAL PRIMARY KEY,
  fecha date NOT NULL,
  observaciones text,
  fk_id_gestion integer REFERENCES gestiones_de_escrituras(id_gestion),
  fk_id_estado_gestion integer REFERENCES estados_de_gestion(id_estado_gestion)
);

-- Table: plantilla_tramites
CREATE TABLE plantilla_tramites (
  version integer NOT NULL,
  id_plantilla_tramite SERIAL PRIMARY KEY,
  orden integer NOT NULL,
  habilitado boolean NOT NULL,
  fk_id_tipo_tramite integer REFERENCES tipos_de_tramite(id_tipo_tramite),
  fk_id_tipo_documento integer REFERENCES tipos_de_documento(id_tipo_documento) ON DELETE CASCADE
);

-- Table: plantilla_presupuestos
CREATE TABLE plantilla_presupuestos (
  version integer NOT NULL,
  id_plantilla_presupuesto SERIAL PRIMARY KEY,
  orden integer NOT NULL,
  habilitado boolean NOT NULL,
  fk_id_tipo_tramite integer REFERENCES tipos_de_tramite(id_tipo_tramite),
  fk_id_concepto integer REFERENCES conceptos(id_concepto) ON DELETE CASCADE
);

-- Table: presupuestos
CREATE TABLE presupuestos (
  version integer NOT NULL,
  id_presupuesto SERIAL PRIMARY KEY,
  numero integer NOT NULL,
  fecha date NOT NULL,
  encabezado text NOT NULL,
  observaciones text,
  estado text NOT NULL,
  monto_inmueble real,
  fk_id_tramite integer,
  fk_id_persona integer REFERENCES personas(id_persona)
);

-- Table: tramites
CREATE TABLE tramites (
  version integer NOT NULL,
  id_tramite SERIAL PRIMARY KEY,
  numero integer NOT NULL,
  nombre text NOT NULL,
  observaciones text,
  fk_id_tipo_tramite integer REFERENCES tipos_de_tramite(id_tipo_tramite),
  fk_id_gestion integer REFERENCES gestiones_de_escrituras(id_gestion),
  fk_id_escritura integer REFERENCES escrituras(id_escritura),
  fk_id_presupuesto integer,
  fk_id_inmueble integer REFERENCES inmuebles(id_inmueble)
);

-- Update circular references
ALTER TABLE presupuestos ADD CONSTRAINT fk_presupuestos_tramite 
  FOREIGN KEY (fk_id_tramite) REFERENCES tramites(id_tramite);
ALTER TABLE tramites ADD CONSTRAINT fk_tramites_presupuesto 
  FOREIGN KEY (fk_id_presupuesto) REFERENCES presupuestos(id_presupuesto);

-- Table: documentos_presentados
CREATE TABLE documentos_presentados (
  version integer NOT NULL,
  id_documento_presentado SERIAL PRIMARY KEY,
  nombre text NOT NULL,
  numero_carton integer,
  fecha_ingreso date,
  fecha_salida date,
  preparado boolean NOT NULL,
  vence boolean NOT NULL,
  fecha_vencimiento date,
  dias_vencimiento integer,
  importe_a_pagar real,
  fecha_pago date,
  liberado boolean NOT NULL,
  fecha_liberado date,
  observado boolean NOT NULL,
  observaciones text,
  entregado boolean DEFAULT false,
  reingresado boolean NOT NULL,
  quien_entrega text NOT NULL,
  fk_id_tramite integer NOT NULL REFERENCES tramites(id_tramite),
  fk_id_tipo_documento integer REFERENCES tipos_de_documento(id_tipo_documento)
);

-- Table: testimonios
CREATE TABLE testimonios (
  version integer NOT NULL,
  id_testimonio SERIAL PRIMARY KEY,
  numero integer NOT NULL,
  observaciones text,
  fecha_inscripcion date,
  fecha_retiro date,
  fecha_ingreso_libro date,
  numero_carpeta integer,
  numero_expediente integer,
  reingresado boolean NOT NULL,
  fk_id_escritura integer REFERENCES escrituras(id_escritura)
);

-- Table: folios
CREATE TABLE folios (
  version integer NOT NULL,
  id_folio SERIAL PRIMARY KEY,
  numero integer NOT NULL,
  anio integer NOT NULL,
  estado text NOT NULL,
  observaciones text,
  fk_id_escritura integer REFERENCES escrituras(id_escritura),
  fk_id_tipo_folio integer NOT NULL REFERENCES tipos_de_folio(id_tipo_folio),
  fk_id_persona_escribano integer NOT NULL REFERENCES personas(id_persona)
);

-- Table: copias
CREATE TABLE copias (
  version integer NOT NULL,
  id_copia SERIAL PRIMARY KEY,
  numero integer NOT NULL,
  fecha_impresion date NOT NULL,
  fecha_retiro date,
  observaciones text,
  fk_id_testimonio integer REFERENCES testimonios(id_testimonio),
  fk_id_persona integer REFERENCES personas(id_persona)
);

-- Table: folios_copias
CREATE TABLE folios_copias (
  version integer NOT NULL,
  fk_id_folio integer NOT NULL REFERENCES folios(id_folio),
  fk_id_copia integer NOT NULL REFERENCES copias(id_copia),
  PRIMARY KEY (fk_id_folio, fk_id_copia)
);

-- Table: movimientos_testimonio
CREATE TABLE movimientos_testimonio (
  version integer NOT NULL,
  id_movimiento SERIAL PRIMARY KEY,
  fecha_movimiento date NOT NULL,
  observaciones text,
  fk_id_testimonio integer REFERENCES testimonios(id_testimonio)
);

-- Table: items
CREATE TABLE items (
  version integer NOT NULL,
  id_item SERIAL PRIMARY KEY,
  nombre text NOT NULL,
  valor real NOT NULL,
  porcentaje integer NOT NULL,
  concepto_fijo boolean NOT NULL,
  fk_id_presupuesto integer REFERENCES presupuestos(id_presupuesto)
);

-- Table: pagos
CREATE TABLE pagos (
  version integer NOT NULL,
  id_pago SERIAL PRIMARY KEY,
  fecha date NOT NULL,
  monto real NOT NULL,
  observaciones text,
  fk_id_presupuesto integer REFERENCES presupuestos(id_presupuesto)
);

-- Table: suplencias
CREATE TABLE suplencias (
  version integer NOT NULL,
  id_suplencia SERIAL PRIMARY KEY,
  fecha_inicio date NOT NULL,
  fecha_fin date,
  observaciones text,
  fk_id_suplantado integer REFERENCES personas(id_persona),
  fk_id_suplente integer REFERENCES personas(id_persona)
);

-- Table: usuarios (column names match backend entity Usuario)
CREATE TABLE usuarios (
  version integer NOT NULL,
  id_usuario SERIAL PRIMARY KEY,
  nombre text NOT NULL,
  contrasenia text NOT NULL,
  tipo text NOT NULL,
  estado boolean NOT NULL,
  fk_id_persona integer REFERENCES personas(id_persona)
);

-- Table: registro_auditoria
CREATE TABLE registro_auditoria (
  version integer NOT NULL,
  id_registro SERIAL PRIMARY KEY,
  fecha timestamp NOT NULL,
  accion text NOT NULL,
  fk_id_usuario integer REFERENCES usuarios(id_usuario)
);

-- Table: tramites_personas
CREATE TABLE tramites_personas (
  version integer NOT NULL,
  id_tramite_persona SERIAL PRIMARY KEY,
  rol text,
  fk_id_tramite integer REFERENCES tramites(id_tramite),
  fk_id_persona_cliente integer REFERENCES personas(id_persona)
);
