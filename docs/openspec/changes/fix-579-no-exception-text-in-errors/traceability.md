# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #579 | open → in progress |
| Use Case | CU76 – Aseguramiento de Calidad e Infraestructura de Pruebas | exists |
| Specification | `docs/openspec/changes/fix-579-no-exception-text-in-errors/` | Gate 1 draft |
| Branch | `fix/579_no_exception_text_in_errors` | created from updated `main` |
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
| Database error on create | `backend-api/src/test/java/com/licensis/notaire/adapter/in/web/ControllerExceptionMessageLeakTest.java` | passing |
| Database error on update | `backend-api/src/test/java/com/licensis/notaire/adapter/in/web/ControllerExceptionMessageLeakTest.java` | passing |
| Application message | `backend-api/src/test/java/com/licensis/notaire/adapter/in/web/support/ErrorResponsesTest.java` | passing |
| No controller echoes exception text | `backend-api/src/test/java/com/licensis/notaire/adapter/in/web/ControllerExceptionMessageLeakTest.java` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-579-no-exception-text-in-errors` |
| 2 | Failing tests written, test cases designed | yes | `ControllerExceptionMessageLeakTest` observed failing (6 of 6) before the fix |
| 3 | Suite green, coverage held, docs updated | partial | backend verify, contracts, docs, workspace and security verify, full Bruno, oasdiff: no changes |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box.
