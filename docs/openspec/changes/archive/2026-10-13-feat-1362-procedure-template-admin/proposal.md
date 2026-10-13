# Manage the required-documents checklist per procedure type; DELETE really removes the row (#1362)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1362 |
| Use Case | CU55 – Crear plantilla de trámite; CU03 – Lista documentos y certificados necesarios; RF-53 – Administrar tablas base |
| Branch | `feat/1362_procedure_template_admin` |
| Gate 1 status | draft |

## Objetivo

Admins couldn't edit which documents each procedure type requires: POST/PUT/DELETE /api/v1/procedure-templates were #1250 bucket C with no UI. Building the screen exposed two real bugs. DELETE /procedure-templates/{type}/{document} answered 204 but kept the row: the document type eagerly loads its template list with CascadeType.ALL, so the flush re-persisted the row. And the Documentos necesarios page read nested procedureType/documentType objects that the API never sends (it answers flat fkIdProcedureType/fkIdDocumentType rows), so every required document showed a blank name and 'No' for expiry.

## What Changes

- Backend `ProcedureTemplateRepository.deleteByPk`: JPQL bulk delete (clears the persistence context), used by the persistence adapter; the controller's DELETE is `@Transactional`. No contract change.
- Frontend: `ProcedureTemplate` type matches the API (flat ids, notes, version); `useProcedureTemplate` joins each row with the document-type catalog (`ProcedureTemplateRow`); create/update/delete hooks invalidate the procedure type's checklist.
- Frontend: Administración → Trámites gets a "Documentos requeridos" row action (`btn-required-docs-procedure`) that opens `RequiredDocumentsDialog`: list by name with notes, add (select offers only document types not listed yet, 409 → duplicate message), edit notes inline, remove with confirmation. i18n es/en.
- Documentos necesarios: rows keyed by the flat ids, names and expiry come from the join.
- `contracts/api-reachability-allowlist.yaml`: POST, PUT and DELETE /api/v1/procedure-templates removed (now used by the UI). GET /procedure-templates (list of all rows) stays bucket C: no screen needs it.
- Bruno procedure-templates 03-09 (document type setup, create, duplicate 409, PUT notes, DELETE, GET 404 after delete, teardown) and COVERAGE row.
- Tests: JUnit `ProcedureTemplateAdminIntegrationTest`, Vitest `procedure-template-admin.test.tsx` and `useProcedureTemplate.test.tsx`, Playwright `TS-0142`. CHANGELOG; testing INVENTORY regenerated.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A procedure type's checklist has at most one row per document type | procedure_templates composite key | Made explicit (409 surfaced in the UI) |
| Removing a checklist row removes it | CU55 | Fixed (DELETE used to keep the row) |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `ui-procedure-template-admin`: Admins manage the required-documents checklist of each procedure type.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | repository bulk delete, Bruno |
| `frontend` | yes | dialog, hooks, types, required-documents page, i18n |
| `contracts` | yes | allowlist |
| `testing/e2e` | yes | TS-0142 |

### Surface area

- Endpoints: none new; DELETE /api/v1/procedure-templates/{idProcedureType}/{idDocumentType} now really deletes
- Entities / Flyway / configuration: none

### Architecture review

Hexagonal layering unchanged: the controller still talks to ProcedureTemplateRepositoryPort; only the adapter's delete implementation changes.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Added + Fixed entries |
| `backend-api/api-test/COVERAGE.md` | procedure-templates row |
| `docs/300-development/303-testing/INVENTORY.md` | TS-0142 listed |
