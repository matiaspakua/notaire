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
| Tasks | `tasks.md` | in progress |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
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
| `CHANGELOG.md` | held (tip conflict risk) | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `openspec validate --strict` + `bash workspace/sdlc/validate-sdlc-plan.sh fix-595-budget-payment-list-lazy` |
| 2 | Failing tests written, test cases designed | yes | red: 2 failures while EAGER (`/opt/cursor/artifacts/595-tdd-red.log`); green after LAZY |
| 3 | Suite green, coverage held, docs updated | partial | slice regression green; CHANGELOG held; full `mvn verify` / preflight deferred to CI |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | slice — issue stays open |

## Exceptions

`CHANGELOG.md` update held on this PR to avoid tip conflict with open English PRs; Fixed entry follows when the lock is claimed. Issue #595 stays open after merge (`Refs #595`); remaining EAGER `@OneToMany` slices track on the same issue. Bot cannot add `in-progress` label (`addLabelsToLabelable` denied).
