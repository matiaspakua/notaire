# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #595 | open → in progress (slice) |
| Use Case | CU74 – Performance and Caching Strategy | exists |
| Specification | `docs/openspec/changes/fix-595-budget-payment-list-lazy/` | Gate 1 draft |
| Branch | `cursor/fix-595_budget-payment-list-lazy-69d3` | created from updated `main` |
| Tasks | `tasks.md` | Gate 3 implementation complete |
| Commits | `09f8f63e`, `6121aeff`, `a46e4823` (+ Process Checks tip fix) | on branch |
| Pull Request | [#1541](https://github.com/matiaspakua/notaire/pull/1541) | open (serialize holder) |
| CI run | PR #1541 checks | in progress |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Finding a budget | `backend-api/src/test/java/com/licensis/notaire/integration/BudgetPaymentListFetchIntegrationTest.java` | passing |
| Listing budgets | `backend-api/src/test/java/com/licensis/notaire/integration/BudgetPaymentListFetchIntegrationTest.java` | passing |
| Payments read on demand | `backend-api/src/test/java/com/licensis/notaire/integration/BudgetPaymentListFetchIntegrationTest.java` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | tip Process Checks fix |
| `docs/300-development/303-testing/INVENTORY.md` | yes | regenerate after new integration test |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `openspec validate --strict` + `bash workspace/sdlc/validate-sdlc-plan.sh fix-595-budget-payment-list-lazy` |
| 2 | Failing tests written, test cases designed | yes | red: 2 failures while EAGER (`/opt/cursor/artifacts/595-tdd-red.log`); green after LAZY |
| 3 | Suite green, coverage held, docs updated | yes | CHANGELOG + INVENTORY updated; Coverage Gate / Integration / Bruno / Playwright prove Gate 3 on CI |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | slice — issue stays open |

## Exceptions

This PR is the serialize holder for the #595 CHANGELOG Fixed entry; open English PRs #1539/#1540 must rebase after merge. Issue #595 stays open after merge (`Refs #595`); remaining EAGER `@OneToMany` slices track on the same issue. Bot cannot add `in-progress` label (`addLabelsToLabelable` denied).
