# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1526 | open → in progress |
| Use Case | CU17 – Registrar persona; RF-53 – Administrar tablas base | exists |
| Specification | `docs/openspec/changes/fix-1526-identification-type-characters/` | Gate 1 draft |
| Branch | `fix/1526_identification_type_characters` | created from updated `main` |
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
| Missing | JUnit, Bruno 12 | passing |
| Not a number | JUnit, Bruno 13 | passing |
| Valid | JUnit | passing |
| Form | `identification-types-admin.test.tsx` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `backend-api/api-test/COVERAGE.md` | yes | branch commit |
| `backend-api/openapi/accepted-breaking-changes.d/1526-identification-type-characters.txt` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-1526-identification-type-characters` |
| 2 | Failing tests written, test cases designed | yes | 10 JUnit failures, 2 Bruno failures and 1 Vitest failure before the change |
| 3 | Suite green, coverage held, docs updated | yes | backend and frontend verify.sh (see PR) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and the backend from `main` on :8080.
