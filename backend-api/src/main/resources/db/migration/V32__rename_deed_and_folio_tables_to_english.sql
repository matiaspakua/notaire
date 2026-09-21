-- =============================================================================
-- V32__rename_deed_and_folio_tables_to_english.sql
-- =============================================================================
-- Author: Claude (paired with Matias)
-- Date: 2026-09-21
-- Description: Slice 7 of the remaining domain-schema-to-English rename
-- (#973, openspec/changes/translate-domain-schema-to-english). Renames
-- escrituras and gestiones_de_escrituras (highest-fan-out slice: nearly
-- every other table has an FK pointing here), plus folios' own remaining
-- Spanish columns (folios itself keeps its name -- "folio" is already a
-- valid, unambiguous English word in this domain). Matches the field
-- names already used by the corresponding (already English) JPA
-- entities: Deed, DeedManagement, Folio.
--
-- fk_id_gestion/fk_id_estado_de_gestion on gestiones_de_escrituras and
-- fk_id_presupuesto (elsewhere) are unaffected here; those FK columns
-- live on OTHER tables and stay untouched until presupuestos' own slice
-- (Slice 8) where applicable, per the epic's vertical-slice convention.
--
-- fk_id_estado_de_gestion and fk_id_persona_escribano on
-- gestiones_de_escrituras, and fk_id_escritura/fk_id_persona_escribano/
-- fk_id_tipo_folio/fk_id_cuaderno on folios, ARE renamed here: their
-- target tables (management_statuses, people, deeds, folio_types,
-- notebooks) are already English.
--
-- folios.anio is renamed to "year_number", not the literal "year": YEAR is
-- an H2 reserved keyword (same category of issue as "value"/"date"/"type"
-- in V27), and this project's H2 unit-test profile builds its schema from
-- these same JPA annotations (ddl-auto=create). See V33 for the matching
-- fix to notebooks.year, which V26 (Slice 1) renamed to the same
-- ill-fated literal "year" -- undetected there because Notebook.java's
-- own @Column annotation was never updated to match, so H2 silently kept
-- creating the old "anio" column and never exercised the reserved-word
-- collision.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

-- Table: escrituras -> deeds
ALTER TABLE escrituras RENAME TO deeds;
ALTER TABLE deeds RENAME COLUMN id_escritura TO id;
ALTER TABLE deeds RENAME COLUMN numero TO number;
ALTER TABLE deeds RENAME COLUMN fecha_escrituracion TO deed_date;
ALTER TABLE deeds RENAME COLUMN cuerpo TO body;
ALTER TABLE deeds RENAME COLUMN estado TO status;
ALTER TABLE deeds RENAME COLUMN matricula_inscripcion TO registration_number;
ALTER TABLE deeds RENAME COLUMN fecha_inscripcion TO registration_date;
ALTER TABLE deeds RENAME COLUMN observaciones TO notes;
ALTER SEQUENCE IF EXISTS escrituras_id_escritura_seq RENAME TO deeds_id_seq;

-- Table: gestiones_de_escrituras -> deed_managements
ALTER TABLE gestiones_de_escrituras RENAME TO deed_managements;
ALTER TABLE deed_managements RENAME COLUMN id_gestion TO id;
ALTER TABLE deed_managements RENAME COLUMN numero TO number;
ALTER TABLE deed_managements RENAME COLUMN fecha_inicio TO start_date;
ALTER TABLE deed_managements RENAME COLUMN encabezado TO heading;
ALTER TABLE deed_managements RENAME COLUMN observaciones TO notes;
ALTER TABLE deed_managements RENAME COLUMN fk_id_persona_escribano TO fk_id_notary_person;
ALTER TABLE deed_managements RENAME COLUMN fk_id_estado_de_gestion TO fk_id_management_status;
ALTER TABLE deed_managements RENAME COLUMN deuda_pendiente_al_archivar TO pending_debt_on_archive;
ALTER SEQUENCE IF EXISTS gestiones_de_escrituras_id_gestion_seq RENAME TO deed_managements_id_seq;

-- Table: folios (name unchanged; only its own columns are Spanish)
ALTER TABLE folios RENAME COLUMN id_folio TO id;
ALTER TABLE folios RENAME COLUMN numero TO number;
ALTER TABLE folios RENAME COLUMN anio TO year_number;
ALTER TABLE folios RENAME COLUMN estado TO status;
ALTER TABLE folios RENAME COLUMN observaciones TO notes;
ALTER TABLE folios RENAME COLUMN fk_id_escritura TO fk_id_deed;
ALTER TABLE folios RENAME COLUMN fk_id_persona_escribano TO fk_id_notary_person;
ALTER TABLE folios RENAME COLUMN fk_id_tipo_folio TO fk_id_folio_type;
ALTER TABLE folios RENAME COLUMN fk_id_cuaderno TO fk_id_notebook;
ALTER SEQUENCE IF EXISTS folios_id_folio_seq RENAME TO folios_id_seq;

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT COUNT(*) FROM deeds;
-- SELECT COUNT(*) FROM deed_managements;
-- SELECT COUNT(*) FROM folios;
