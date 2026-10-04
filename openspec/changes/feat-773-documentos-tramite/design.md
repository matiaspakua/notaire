> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates, §7 Testing Rules, §11 Release Rules.

## Context

Issue #773, Use Case CU04 – Registrar documentación cliente (#157); CU72 – Gestión de Documentos Presentados (#163); CU03 – Lista documentos y certificados necesarios (#156). The #1055/#977 renames changed the API field names; the Documentos screen was not updated and has no E2E, so it silently lost the type and date.

## Goals / Non-Goals

**Goals:** A working Documentos module with a trámite link, visible from the gestión.
**Non-Goals:** Automatic listing of required documents when a trámite is confirmed; file upload of document scans.

## Decisions

1. Keep the existing controller request shape and fix the client, instead of changing the API again: other clients and Bruno already use typeId, date and delivered.
2. The trámite selector reuses the CU43 endpoint instead of a new listing endpoint.

## Riesgos / Trade-offs

- The list endpoint returns every document; pagination is out of scope.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Create a document with a type and a date | integration | `SubmittedDocumentControllerTest` |
| Create a document for a trámite | e2e | `testing/e2e/tests/TS-0099-documento-tramite-link.spec.ts` |
| A gestión with a linked document | unit | `ManagementCaseSummaryServiceTest` |
| A gestión without documents | unit | `ManagementCaseSummaryServiceTest` |

- New unit tests (`src/test/java/.../unit/`): `ManagementCaseSummaryServiceTest`, `useDocumentosPresentados` hook test
- New integration tests: `SubmittedDocumentControllerTest` and `ManagementCaseSummaryIntegrationTest` (H2)
- Coverage impact: positive: new branches covered; the floor is unchanged

## Regression Strategy

- Existing tests affected: SubmittedDocumentControllerTest, ManagementCaseSummaryServiceTest
- Full suite command: `mvn verify -pl backend-api; npx vitest run; Playwright TS-0099`
- HTTP/Bruno API suite: unchanged
- Legacy paths at risk: none

## Playwright Strategy

No UI change; n/a.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: single PR
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): CI green on the merge commit; a document created against a trámite appears in the case summary

## Rollback Strategy

- Revert the PR.
