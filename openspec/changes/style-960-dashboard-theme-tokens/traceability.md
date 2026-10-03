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
| Issue | #960 | open (in-progress label blocked for worker gh) |
| Use Case | CU76 / RF #78 | exists |
| Related | frontend-design skill; ui-ux-design rules | referenced |
| Specification | `openspec/changes/style-960-dashboard-theme-tokens/` | Gate 1 in progress |
| Branch | `cursor/style-960-dashboard-theme-tokens-69d3` | created from `origin/main` |
| Tasks | `tasks.md` | pending implement |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Dashboard layout has no `#RRGGBB` literals | `hex-hygiene.test.ts` | pending |
| Dashboard home page has no `#RRGGBB` literals | `hex-hygiene.test.ts` | pending |
| Shared table UI has no `#RRGGBB` literals | `hex-hygiene.test.ts` | pending |
| Audited app/components globs exclude theme and stay hex-free | `hex-hygiene.test.ts` | pending |
| Visual parity at 320 / 768 / 1024 | Playwright smoke or computerUse evidence | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | pending | — |
| `FRONTEND-DESIGN-SYSTEM.md` (optional pointer) | pending | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | `validate-sdlc-plan.sh` |
| 2 | Failing tests written, test cases designed | pending | hex-hygiene red then green |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

Worker `gh` cannot add `in-progress` label (GraphQL resource not accessible).
Coordinator owns label/project board moves and merge after
`check-heavy-ci.sh` exit 0.
