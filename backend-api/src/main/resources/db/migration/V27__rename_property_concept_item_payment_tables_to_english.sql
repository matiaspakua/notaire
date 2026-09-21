-- =============================================================================
-- V27__rename_property_concept_item_payment_tables_to_english.sql
-- =============================================================================
-- Author: Claude (paired with Matias)
-- Date: 2026-09-21
-- Description: Slice 2 of the remaining domain-schema-to-English rename
-- (#973, openspec/changes/translate-domain-schema-to-english). Renames
-- inmuebles, conceptos, items and pagos and their own columns to English,
-- matching the field names already used by the corresponding (already
-- English) JPA entities: Property, Concept, Item, Payment.
--
-- "value", "date" and "type" are H2 reserved keywords (this project's H2
-- unit-test profile builds its schema from these same JPA annotations via
-- ddl-auto=create, so a DDL-illegal column name breaks unit tests, not just
-- style) -- renamed to amount/payment_date/item_type instead of the literal
-- English translation.
--
-- Columns not mapped by the JPA entity (pre-existing schema/entity drift,
-- unrelated to this rename) are translated too for table-wide consistency,
-- following the precedent set by V25__rename_personas_to_people.sql.
--
-- fk_id_presupuesto on items and pagos is intentionally left untouched:
-- it is this table's own column, but it points at "presupuestos", which
-- is not part of this slice (see tasks.md Slice 8) -- renaming it now
-- would create an English column name pointing at a still-Spanish table.
-- FK columns on OTHER tables that reference inmuebles/conceptos (e.g. any
-- fk_id_inmueble/fk_id_concepto elsewhere) are likewise deferred to their
-- owning table's own future slice.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

-- Table: inmuebles -> properties
ALTER TABLE inmuebles RENAME TO properties;
ALTER TABLE properties RENAME COLUMN id_inmueble TO id;
ALTER TABLE properties RENAME COLUMN nomenclatura TO nomenclature;
ALTER TABLE properties RENAME COLUMN matricula TO registration_number;
ALTER TABLE properties RENAME COLUMN valuacion_anio TO valuation_year;
ALTER TABLE properties RENAME COLUMN valuacion_fiscal TO fiscal_valuation;
ALTER TABLE properties RENAME COLUMN partida TO record_number;
ALTER TABLE properties RENAME COLUMN circunscripcion TO district;
ALTER TABLE properties RENAME COLUMN seccion TO section;
ALTER TABLE properties RENAME COLUMN zona TO zone;
ALTER TABLE properties RENAME COLUMN manzana TO block;
ALTER TABLE properties RENAME COLUMN parcela TO parcel;
ALTER TABLE properties RENAME COLUMN poligono TO polygon;
ALTER TABLE properties RENAME COLUMN unidad_funcional TO functional_unit;
ALTER TABLE properties RENAME COLUMN domicilio TO address;
ALTER TABLE properties RENAME COLUMN localidad TO locality;
ALTER TABLE properties RENAME COLUMN observaciones TO notes;
ALTER TABLE properties RENAME COLUMN tomo_folio_finca TO registry_volume_folio;
ALTER TABLE properties RENAME COLUMN linderos TO boundaries;
ALTER SEQUENCE IF EXISTS inmuebles_id_inmueble_seq RENAME TO properties_id_seq;

-- Table: conceptos -> concepts
ALTER TABLE conceptos RENAME TO concepts;
ALTER TABLE concepts RENAME COLUMN id_concepto TO id;
ALTER TABLE concepts RENAME COLUMN nombre TO name;
ALTER TABLE concepts RENAME COLUMN valor TO amount;
ALTER TABLE concepts RENAME COLUMN porcentaje TO percentage;
ALTER TABLE concepts RENAME COLUMN habilitado TO enabled;
ALTER TABLE concepts RENAME COLUMN concepto_fijo TO fixed_concept;
ALTER SEQUENCE IF EXISTS conceptos_id_concepto_seq RENAME TO concepts_id_seq;

-- Table: items (name already English; only its own columns are Spanish)
ALTER TABLE items RENAME COLUMN id_item TO id;
ALTER TABLE items RENAME COLUMN nombre TO name;
ALTER TABLE items RENAME COLUMN valor TO amount;
ALTER TABLE items RENAME COLUMN porcentaje TO percentage;
ALTER TABLE items RENAME COLUMN concepto_fijo TO fixed_concept;
ALTER TABLE items RENAME COLUMN observaciones TO notes;
ALTER TABLE items RENAME COLUMN tipo TO item_type;
ALTER TABLE items RENAME COLUMN motivo TO reason;
ALTER SEQUENCE IF EXISTS items_id_item_seq RENAME TO items_id_seq;

-- Table: pagos -> payments
ALTER TABLE pagos RENAME TO payments;
ALTER TABLE payments RENAME COLUMN id_pago TO id;
ALTER TABLE payments RENAME COLUMN fecha TO payment_date;
ALTER TABLE payments RENAME COLUMN monto TO amount;
ALTER TABLE payments RENAME COLUMN observaciones TO notes;
ALTER TABLE payments RENAME COLUMN metodo_pago TO payment_method;
ALTER SEQUENCE IF EXISTS pagos_id_pago_seq RENAME TO payments_id_seq;

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT COUNT(*) FROM properties;
-- SELECT COUNT(*) FROM concepts;
-- SELECT COUNT(*) FROM items;
-- SELECT COUNT(*) FROM payments;
