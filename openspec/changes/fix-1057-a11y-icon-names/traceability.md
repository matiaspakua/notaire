# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
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
| Issue | #1057 | open (implement in progress) |
| Use Case | CU76 – QA / accessibility validation; issue RNF WCAG AA (`.claude/rules/ui-ux-design.md`). **CU gap:** no dedicated CU-XX for icon naming — document under CU76 | exists (CU76); gap noted |
| Related | #940 (dialog close a11y — closed); #608 / TS-0042 (search labels); audit-2026-09 | referenced |
| Specification | `openspec/changes/fix-1057-a11y-icon-names/` | Gate 1 validated; implementing |
| Branch | `cursor/fix-1057-a11y-icon-names-69d3` | created |
| Tasks | `tasks.md` | implement largely complete; await CI/merge |
| Commits | `14dbd9ab`, `1f62fa5d` | pushed |
| Pull Request | #1150 | draft open |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Edit action is named | E2E TS-0096 + unit inventory | implemented |
| Delete action is named | E2E TS-0096 + unit inventory | implemented |
| Presupuesto resumen action is named | E2E TS-0096 + unit inventory | implemented |
| Admin NotaireIcon row actions are named | E2E TS-0096 | pending |
| Unnamed icon button fails lint | unit `icon-button-aria-labels.test.ts` (static inventory) | implemented |
| Named icon button passes lint | unit inventory snippets present | implemented |
| Role-based selector finds edit on usuarios | E2E TS-0096 + TS-0016 | implemented |
| CU21 edit flow is no longer skipped for naming | unskip TS-0016 CU21-GW01/GW02 | implemented |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | yes | — |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | yes | — |
| `.claude/rules/ui-ux-design.md` | yes (optional) | — |
| `CHANGELOG.md` | yes | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes (draft) | artifacts in this folder; validate via temp copy into `openspec/changes/` |
| 2 | Failing tests written, test cases designed | yes | unit inventory + TS-0096 + CU21 unskip |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None. Implement deliberately deferred (no branch/PR/push) until #1147 and
#1148 ship — authorized by coordinator prep-only scope for this worker.
