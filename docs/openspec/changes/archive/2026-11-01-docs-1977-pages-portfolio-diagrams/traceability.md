# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> Rows below Tasks stay `pending` until the corresponding step actually happens.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1977 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/docs-1977-pages-portfolio-diagrams/` | Gate 1 |
| Branch | `docs/1977_pages_portfolio_diagrams` | from the #1976 branch |
| Tasks | `tasks.md` | groups 1–10 done |
| Commits | `69dba5a4e` plan, `0ab0e176a` tests, `f18b477f3` implementation and docs, then the archive commit (`Closes #1977`) | done |
| Pull Request | pending | pending |
| CI run | PR checks | pending |
| Merge commit | Owner merge | pending |
| Release / tag | next cut | pending |
| Smoke test | `/notaire/docs/hexagonal/` step through the request flow at 375 and 1280 px after the Pages deploy | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Reader opens a page with a diagram | `tests/diagrams.test.tsx` (never plays on its own); `test_pages_diagrams.py` (no autoplay) | green |
| Reader steps with the keyboard | `tests/diagrams.test.tsx` (buttons and arrow keys, live region); `e2e/site.spec.ts` diagram test | green |
| Reader on a 375 px phone | `e2e/site.spec.ts` axe sweep and diagram test on the mobile project | green |
| A workflow is added | `tests/diagrams.test.tsx` (trigger grouping); `test_pages_diagrams.py` (`workflowTriggers()` on the stack page) | green |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `github-page/docs/portfolio-review.md` | new | `f18b477f3` |
| `github-page/docs/ux-assessment.md` | follow-up section | `f18b477f3` |
| `github-page/README.md` | diagrams and screenshots | `f18b477f3` |
| `README.md` | workflow count 11 → 19 | `f18b477f3` |
| `CHANGELOG.md` | `[Unreleased]` Documentation | `f18b477f3` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1977, this change |
| 2 | Failing tests written, test cases designed | yes | `0ab0e176a` before `f18b477f3` |
| 3 | Suite green, docs updated | yes | vitest, tsc, eslint, build, check:links, Playwright + axe, workspace tests, quick.sh |
| 4 | CI green, review approved, no conflicts | pending | PR |
| 5 | Deployed, smoke test passed, Issue closed | pending | Pages deploy |

## Exceptions

- The token cannot set labels, milestones or board status (HTTP 403); the PR body notes it for the Owner.
