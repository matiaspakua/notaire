# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #579 | open → in progress |
| Use Case | CU26 – Tipos de trámite; CU27 – Tipos de documento; CU29 – Conceptos; CU30 – Estados de gestión; CU36 – Tipos de folio | exists |
| Specification | `docs/openspec/changes/fix-579-constraint-errors-400-catalogs/` | Gate 1 draft |
| Branch | `fix/579_constraint_errors_400_catalogs` | created from updated `main` |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | local, not pushed |
| Pull Request | — | pending (Owner approval) |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Create violating a constraint | `backend-api/src/test/java/com/licensis/notaire/integration/CatalogConstraintErrorsIntegrationTest.java, Bruno */08-create-empty-body` | passing |
| Update violating a constraint | `backend-api/src/test/java/com/licensis/notaire/integration/CatalogConstraintErrorsIntegrationTest.java` | passing |
| Duplicate role name on update (409) | `backend-api/src/test/java/com/licensis/notaire/integration/UniqueConstraintConflictIntegrationTest.java`, `ErrorResponsesTest.uniqueViolationIsConflict`, Bruno roles duplicate-rename | passing |
| NOT NULL stays 400 | `UniqueConstraintConflictIntegrationTest.notNullStillBadRequest`, `ErrorResponsesTest.otherConstraintViolationsStayBadRequest` | passing |
| Contract documents 409 | `UniqueConstraintConflictIntegrationTest.contractDocumentsConflict` | passing |
| Other failures | `backend-api/src/test/java/com/licensis/notaire/adapter/in/web/support/ErrorResponsesTest.java` | passing |
| Contract | `backend-api/src/test/java/com/licensis/notaire/integration/CatalogConstraintErrorsIntegrationTest.java` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `backend-api/openapi/openapi.yaml` | yes | branch commit |
| `backend-api/openapi/accepted-breaking-changes.txt` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-579-constraint-errors-400-catalogs` |
| 2 | Failing tests written, test cases designed | yes | `CatalogConstraintErrorsIntegrationTest` observed failing 15/15 before the change |
| 3 | Suite green, coverage held, docs updated | partial | backend verify, Bruno and Playwright green; oasdiff reports no breaking change; stale-entry check OK; run_pipeline.sh not run (no Docker) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` and the Testcontainers `pg-integration` tests need Docker, unavailable on the agent box.
