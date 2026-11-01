# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> Rows below Tasks stay `pending` until the corresponding step actually happens.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1985 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/feat-1985-pages-demo-metrics-about/` | Gate 1 |
| Branch | `feat/1985_pages_demo_metrics_about` | from `main` after #1984 |
| Tasks | `tasks.md` | in progress |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | PR checks | pending |
| Merge commit | Owner merge | pending |
| Release / tag | next cut | pending |
| Smoke test | `/notaire/demo/` sign in and open Managements at 375 and 1280 px after the Pages deploy | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Visitor opens the demo | `e2e/demo.spec.ts` | pending |
| Visitor tries to save a change | `tests/demo-shim.test.ts`; `e2e/demo.spec.ts` | pending |
| The app asks for data that was not recorded | `tests/demo-shim.test.ts` | pending |
| Fixtures are checked | `test_pages_demo_outcomes_about.py` | pending |
| Reader opens the results page | `tests/outcomes-about.test.tsx`; `test_pages_demo_outcomes_about.py` | pending |
| A metric is not derivable | `tests/outcomes-about.test.tsx` | pending |
| Reader opens the About page | `tests/outcomes-about.test.tsx`; `e2e/site.spec.ts` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `github-page/docs/demo.md` | pending | pending |
| `github-page/README.md` | pending | pending |
| `CHANGELOG.md` | pending | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1985, this change |
| 2 | Failing tests written, test cases designed | pending | test commit |
| 3 | Suite green, docs updated | pending | local runs |
| 4 | CI green, review approved, no conflicts | pending | PR |
| 5 | Deployed, smoke test passed, Issue closed | pending | Pages deploy |

## Exceptions

- The token cannot set labels, milestones or board status (HTTP 403); the PR body notes it for the Owner.
