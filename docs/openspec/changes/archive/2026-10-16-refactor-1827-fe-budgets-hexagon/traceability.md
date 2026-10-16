# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1827 | in-progress |
| Use Case | Architectural refactor under epic #1693 (ADR-030); budgets — no behaviour change | exists |
| Specification | `docs/openspec/changes/2026-10-16-refactor-1827-fe-budgets-hexagon/` | passed |
| Branch | `cursor/1827-fe-budgets-a603` | created |
| Tasks | `tasks.md` | in progress |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario | Test | Status |
|----------|------|--------|
| useBudgets and useBudgetSearch leave the eslint baseline | architecture-boundaries | pending |
| Budgets dashboard leaves the eslint baseline | architecture-boundaries | pending |
| REST behaviour unchanged | useCreateBudget / gateway / use-cases tests | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/202-ADR/ADR-030-hexagonal-architecture-full-adoption.md` | pending | pending |
| `CHANGELOG.md` | n/a (#1766) | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1827, this folder |
| 2 | Failing tests | pending | pending |
| 3 | Suite green | pending | pending |
| 4 | CI green | pending | pending |
| 5 | Deployed | pending | pending |

## Exceptions

None.
