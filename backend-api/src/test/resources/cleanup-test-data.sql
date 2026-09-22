-- Cleanup script for UseCaseDomainsIntegrationTest
-- Removes ONLY data created by BusinessWorkflowIntegrationTest
-- (tables NOT seeded by data.sql).
--
-- Tables WITH seed data (from data.sql) are intentionally left alone:
--   identification_types, folio_types, management_statuses,
--   procedure_types, concepts, personas, usuarios, budgets
-- These contain the reference IDs (1, 1, etc.) that downstream tests
-- like BusinessWorkflowIntegrationTest depend on.
--
-- Tables WITHOUT seed data are truncated to prevent lazy-loading failures:
-- BusinessWorkflowIntegrationTest creates rows in gestiones, escrituras,
-- folios, etc., and the legacy JPA controllers (which close the EntityManager
-- before returning) cannot serialize those entities' lazy @OneToMany
-- collections — causing LazyInitializationException → 500 errors.
--
-- Truncating these non-seed tables gives UseCaseDomainsIntegrationTest an
-- empty database in the relevant tables, without destroying seed data.
--
-- Uses SET REFERENTIAL_INTEGRITY to handle FK constraints in H2.

SET REFERENTIAL_INTEGRITY FALSE;

-- Tables WITHOUT seed data (populated only by BusinessWorkflowIntegrationTest)
TRUNCATE TABLE deed_managements;
TRUNCATE TABLE deeds;
TRUNCATE TABLE folios;
TRUNCATE TABLE folio_copies;
TRUNCATE TABLE items;
TRUNCATE TABLE payments;
TRUNCATE TABLE historial;
TRUNCATE TABLE procedures;
TRUNCATE TABLE person_procedures;
TRUNCATE TABLE submitted_documents;
TRUNCATE TABLE properties;
TRUNCATE TABLE testimonies;
TRUNCATE TABLE testimony_movements;
TRUNCATE TABLE copies;
TRUNCATE TABLE audit_records;
TRUNCATE TABLE substitutions;
TRUNCATE TABLE document_types;
TRUNCATE TABLE budget_templates;
TRUNCATE TABLE procedure_templates;
TRUNCATE TABLE identificaciones;

SET REFERENTIAL_INTEGRITY TRUE;
