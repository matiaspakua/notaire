# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2119 | open |
| Use Case | CU47 – Payment consultation by budget and date range | exists |
| Related | #2114 / #2115 (page tests landed); do not reopen #2114 | referenced |
| Specification | `docs/openspec/changes/archive/2026-11-10-test-2119-payments-date-query-params/` | archived in PR |
| Branch | `test/2119_payments-date-query-params` | created |
| Tasks | `tasks.md` | implement in progress |
| Commits | pending on branch | pending |
| Pull Request | pending | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| typing both days keeps both keys in the address | `payments-page-actions.test.tsx` date range address case | implemented |
| clearing the start day leaves the end day | same | implemented |
| an exceeded balance is explained | existing unit | exists |
| a range that ends before it starts lists nothing | existing unit | exists |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/openspec/specs/payments-page-tests/spec.md` | yes (archive sync) | pending |
| `CHANGELOG.md` | yes | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | archived folder + `validate-sdlc-plan.sh` |
| 2 | Failing tests written, test cases designed | yes | corrected expectations failed; then mock fixed (30/30) |
| 3 | Suite green, coverage held, docs updated | pending | preflight `--full` |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

Label `status:in-progress` could not be applied via `gh` (GraphQL: Resource not
accessible by integration). Continue without the label; note as factory friction
only if new (do not reopen #2217 / #2218).
