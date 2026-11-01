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
| Commits | e0d355eeb, e7dccfba1, implementation, archive | Refs #1985; final Closes #1985 |
| Pull Request | pending | pending |
| CI run | PR checks | pending |
| Merge commit | Owner merge | pending |
| Release / tag | next cut | pending |
| Smoke test | `/notaire/demo/` sign in and open Managements at 375 and 1280 px after the Pages deploy | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Visitor opens the demo | `e2e/demo.spec.ts` | green locally |
| Visitor tries to save a change | `tests/demo-shim.test.ts`; `e2e/demo.spec.ts` | green locally |
| The app asks for data that was not recorded | `tests/demo-shim.test.ts` | green locally |
| Fixtures are checked | `test_pages_demo_outcomes_about.py` | green locally |
| Reader opens the results page | `tests/outcomes-about.test.tsx`; `test_pages_demo_outcomes_about.py` | green locally |
| A metric is not derivable | `tests/outcomes-about.test.tsx` | green locally |
| Reader opens the About page | `tests/outcomes-about.test.tsx`; `e2e/site.spec.ts` | green locally |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `github-page/docs/demo.md` | yes | implementation commit |
| `github-page/README.md` | yes | implementation commit |
| `CHANGELOG.md` | yes | implementation commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1985, this change |
| 2 | Failing tests written, test cases designed | yes | e7dccfba1 (vitest, pytest and site build failed before the implementation) |
| 3 | Suite green, docs updated | yes | typecheck, lint, Vitest 43, export + demo, check:links 1789, Playwright 57 + axe (375/1280), test_pages_*.py, quick.sh all OK |
| 4 | CI green, review approved, no conflicts | pending | PR |
| 5 | Deployed, smoke test passed, Issue closed | pending | Pages deploy |

## Exceptions

- The token cannot set labels, milestones or board status (HTTP 403); the PR body notes it for the Owner.

## Commits

- e0d355eeb docs(openspec): plan
- e7dccfba1 test(pages): failing first
- implementation and archive commits: see the PR

## Related

- #1986: hosting options for a writable demo (nothing provisioned)
