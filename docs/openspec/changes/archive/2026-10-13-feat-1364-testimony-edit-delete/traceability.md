# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1364 | open → in progress |
| Use Case | CU07 – Generar testimonio; CU44 – Reingreso testimonio; RF-31 | exists |
| Specification | `docs/openspec/changes/feat-1364-testimony-edit-delete/` | Gate 1 draft |
| Branch | `feat/1364_testimony_edit_delete` | created from updated `main` |
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
| Edit | Vitest, TS-0143 | passing |
| Delete | JUnit, Vitest, TS-0143 | passing |
| In use | JUnit, Bruno 06b, Vitest, TS-0143 | passing |
| History | Vitest, TS-0143 | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `backend-api/api-test/COVERAGE.md` | yes | branch commit |
| `contracts/api-reachability-allowlist.yaml` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh feat-1364-testimony-edit-delete` |
| 2 | Failing tests written, test cases designed | yes | 1 JUnit failure (204 instead of 409) and 6 Vitest failures before the change |
| 3 | Suite green, coverage held, docs updated | yes | backend and frontend verify.sh (see PR) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and the backend from `main` on :8080.
