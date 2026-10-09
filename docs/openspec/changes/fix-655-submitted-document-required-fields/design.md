# Design

## Context

Issue #655 lists endpoints that accept empty or incomplete bodies. Run 7: the Owner made `typeId` required and asked for a user-centred analysis of `procedureId`. V6 (2026-06-05) made `fk_id_tramite` nullable so that the Documentos screen could create documents before it had any gestión/trámite selector; #773 later added those selectors as optional. Readers that depend on the link: ManagementCaseSummaryService (case summary), CU04 documentation-complete flow, ReportDocumentFactory CU09 debt report (findByFkIdProcedureFkIdManagementIdManagement), CU42 UpcomingExpirationService (gestión number and header), ExternalEntityDocumentService CU10 (validateBelongsToManagement), ReingresoDocumentacionService CU43.

## Goals / Non-Goals

Goal: no new orphan or typeless documents; legacy rows untouched. Non-goals: backfilling or deleting documents already stored without a trámite; making the DB column NOT NULL (needs a data decision); the other #655 endpoints.

## Decisions

Require on create and keep the column nullable plus PUT partial: it stops the problem for every new document without a migration or a data-loss decision, and legacy rows stay editable (the dialog offers to link them). Bean validation on a dedicated create record, as for payments, so the contract shows `required` exactly where it is enforced. The breaking change is accepted rather than avoided because the Owner asked for these fields to be required and the shipped UI is updated in the same PR.

## Riesgos / Trade-offs

An external client that created documents without a procedure now gets 400; none ships in this repository. A gestión with no trámite cannot receive documents until one is added; the dialog says so.

## Testing Strategy

Validation tests, contract test, frontend unit test and Bruno 12/13 written first and observed failing (4 backend failures; 2 Bruno failures; frontend module missing).

## Regression Strategy

Full backend suite (2129 tests); vitest 386; full Bruno 328/556 against this backend on PostgreSQL 17; Playwright chromium TS-0033, TS-0097, TS-0099, TS-0090 and TS-0012, TS-0043, TS-0060, TS-0070, TS-0071 (110 passed) against this frontend and backend.

## Playwright Strategy

TS-0033, TS-0097, TS-0099, TS-0090, TS-0012, TS-0043, TS-0060, TS-0070, TS-0071: 110 passed.

## Deployment Strategy

Backend and frontend together (the old UI could still send a create without trámite).

## Rollback Strategy

Revert the merge commit.
