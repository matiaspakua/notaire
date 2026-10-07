# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #773 | open → in progress |
| Use Case | CU04 – Registrar documentación cliente (#157); CU72 – Gestión de Documentos Presentados (#163); CU03 – Lista documentos y certificados necesarios (#156) | exists |
| Related | #771 (parent), #772, #774 | referenced |
| Specification | `docs/openspec/changes/feat-773-documentos-tramite/` | Gate 1 draft |
| Branch | `feat/773_required_docs` | created from updated `main` |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | in progress |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Create a document with a type and a date | `SubmittedDocumentControllerTest` | pending |
| Create a document for a trámite | `testing/e2e/tests/TS-0099-documento-tramite-link.spec.ts` | pending |
| A gestión with a linked document | `ManagementCaseSummaryServiceTest` | pending |
| A gestión without documents | `ManagementCaseSummaryServiceTest` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU04 – Registrar documentación cliente.md` | pending | — |
| `backend-api/openapi/openapi.yaml` | pending | — |
| `CHANGELOG.md` | pending | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | `bash scripts/validate-sdlc-plan.sh` |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None.
