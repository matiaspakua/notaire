-- Cleanup script for UseCaseDomainsIntegrationTest
-- Removes ONLY data created by BusinessWorkflowIntegrationTest
-- (tables NOT seeded by data.sql).
--
-- Tables WITH seed data (from data.sql) are intentionally left alone:
--   tipos_identificacion, tipos_de_folio, estados_de_gestion,
--   tipos_de_tramite, conceptos, personas, usuarios, presupuestos
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
TRUNCATE TABLE gestiones_de_escrituras;
TRUNCATE TABLE escrituras;
TRUNCATE TABLE folios;
TRUNCATE TABLE folios_copias;
TRUNCATE TABLE items;
TRUNCATE TABLE pagos;
TRUNCATE TABLE historial;
TRUNCATE TABLE tramites;
TRUNCATE TABLE tramites_personas;
TRUNCATE TABLE documentos_presentados;
TRUNCATE TABLE inmuebles;
TRUNCATE TABLE testimonios;
TRUNCATE TABLE movimientos_testimonio;
TRUNCATE TABLE copias;
TRUNCATE TABLE registro_auditoria;
TRUNCATE TABLE suplencias;
TRUNCATE TABLE tipos_de_documento;
TRUNCATE TABLE plantilla_presupuestos;
TRUNCATE TABLE plantilla_tramites;
TRUNCATE TABLE identificaciones;

SET REFERENTIAL_INTEGRITY TRUE;
