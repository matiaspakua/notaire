# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1545 | open → in progress |
| Use Case | CU23 – Ver registro de actividades de usuario; CU73 | exists |
| Specification | `docs/openspec/changes/feat-1545-audit-user-date-filter/` | Gate 1 draft |
| Branch | `feat/1545_audit_user_date_filter` | created from updated `main` |
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
| User and range | JUnit, TS-0016 | passing |
| Combined | JUnit | passing |
| Open-ended | JUnit | passing |
| Invalid | JUnit, Bruno 06, Vitest, TS-0016 | passing |
| URL | Vitest | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `backend-api/api-test/COVERAGE.md` | yes | branch commit |
| `contracts/api-reachability-allowlist.yaml` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh feat-1545-audit-user-date-filter` |
| 2 | Failing tests written, test cases designed | yes | 5 JUnit and 4 Vitest failures before the change |
| 3 | Suite green, coverage held, docs updated | yes | verify.sh / quick.sh (see PR) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and the backend from `main` on :8080.
