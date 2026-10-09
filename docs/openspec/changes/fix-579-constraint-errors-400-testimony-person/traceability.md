# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #579 | open → in progress |
| Use Case | CU07 – Testimonio; CU08 – Movimientos de testimonio; CU06 – Personas | exists |
| Specification | `docs/openspec/changes/fix-579-constraint-errors-400-testimony-person/` | Gate 1 draft |
| Branch | `fix/579_constraint_errors_400_testimony_person` | stacked on #1370 |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | pushed |
| Pull Request | see PR | open |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Empty testimony movement | `backend-api/src/test/java/com/licensis/notaire/integration/TestimonyPersonConstraintErrorsIntegrationTest.java, Bruno testimony-movements/09-create-empty-body` | passing |
| Constraint violation on create or update | `backend-api/src/test/java/com/licensis/notaire/adapter/in/web/TestimonyPersonConstraintErrorsTest.java` | passing |
| Other failures | `backend-api/src/test/java/com/licensis/notaire/adapter/in/web/TestimonyPersonConstraintErrorsTest.java` | passing |
| Contract | `backend-api/src/test/java/com/licensis/notaire/integration/TestimonyPersonConstraintErrorsIntegrationTest.java` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `backend-api/openapi/openapi.yaml` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-579-constraint-errors-400-testimony-person` |
| 2 | Failing tests written, test cases designed | yes | `TestimonyPersonConstraintErrorsTest` + integration observed failing 10/11 before the change |
| 3 | Suite green, coverage held, docs updated | partial | backend verify, Bruno and Playwright green; oasdiff reports no breaking change; stale-entry check OK; run_pipeline.sh not run (no Docker) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` and the Testcontainers `pg-integration` tests need Docker, unavailable on the agent box.
