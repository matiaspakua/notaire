# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1362 | open → in progress |
| Use Case | CU55 – Crear plantilla de trámite; CU03 – Lista documentos y certificados necesarios; RF-53 – Administrar tablas base | exists |
| Specification | `docs/openspec/changes/feat-1362-procedure-template-admin/` | Gate 1 draft |
| Branch | `feat/1362_procedure_template_admin` | created from updated `main` |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | pushed |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Add | `TS-0142`, `procedure-template-admin.test.tsx` | passing |
| Duplicate | `procedure-template-admin.test.tsx`, Bruno 05, JUnit | passing |
| Edit notes | `TS-0142`, Bruno 06 | passing |
| Remove | `TS-0142`, Bruno 07-08, JUnit | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `backend-api/api-test/COVERAGE.md` | yes | branch commit |
| `docs/300-development/303-testing/INVENTORY.md` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh feat-1362-procedure-template-admin` |
| 2 | Failing tests written, test cases designed | yes | 1 JUnit failure, 2 Bruno failures, Vitest failures and 2 Playwright failures before the change |
| 3 | Suite green, coverage held, docs updated | yes | backend and frontend verify.sh (see PR) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and the backend from `main` on :8080.
