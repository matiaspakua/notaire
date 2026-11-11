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
| Issue | #2071 | open (Gate 1.6 SKIPPED — labels 403) |
| Use Case | RNF-05 — Aspecto visual | exists |
| Specification | `docs/openspec/changes/feat-2071-table-empty-next-action/` | drafted |
| Branch | `fix/2071_table-empty-next-action` | created |
| Tasks | `tasks.md` | 1–2 complete |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| DataTable empty shows description and action | `frontend/src/tests/unit/data-table.test.tsx` | pending |
| People list empty shows description | `frontend/src/tests/unit/people-empty-next-action.test.tsx` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/203-design/FRONTEND-DESIGN-SYSTEM.md` | pending | pending |
| `CHANGELOG.md` | pending | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | OpenSpec change + validate scripts |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

Gate 1.6 honest SKIP: integration cannot add `status:in-progress` (addLabelsToLabelable 403).
