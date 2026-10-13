# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1557 | open → in progress |
| Use Case | CU47 – Consultar Pagos | exists |
| Specification | `docs/openspec/changes/feat-1557-payments-date-filter/` | Gate 1 draft |
| Branch | `feat/1557_payments_date_filter` | created from updated `main` |
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
| Inclusive | TS-0014 GW01, Vitest | passing |
| With budget | Vitest | passing |
| Inverted UI | Vitest, TS-0014 GW01 | passing |
| Inverted API | PaymentControllerTest, Bruno 06a | passing |
| Mobile | screenshots | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | yes | branch commit |
| `docs/openspec/specs/e2e-feature-gap-skip-tracking/spec.md` | yes | branch commit |
| `backend-api/api-test/COVERAGE.md` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh feat-1557-payments-date-filter` |
| 2 | Failing tests written, test cases designed | yes | the controller tests and the Vitest file failed before the change |
| 3 | Suite green, coverage held, docs updated | yes | quick.sh (see PR) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and the backend from `main` on :8080.
