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
| Specification | `openspec/changes/style-960-dashboard-theme-tokens/` | Gate 1 validated |
| Branch | `cursor/style-960-dashboard-theme-tokens-69d3` | created from `origin/main` |
| Tasks | `tasks.md` | implement complete; merge pending |
| Commits | `ac3570e1`, `b88033f4` | pushed |
| Pull Request | https://github.com/matiaspakua/notaire/pull/1189 (draft) | open |
| CI run | — | pending heavy gate |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Dashboard layout has no `#RRGGBB` literals | `hex-hygiene.test.ts` | passing |
| Dashboard home page has no `#RRGGBB` literals | `hex-hygiene.test.ts` | passing |
| Shared table UI has no `#RRGGBB` literals | `hex-hygiene.test.ts` | passing |
| Audited app/components globs exclude theme and stay hex-free | `hex-hygiene.test.ts` | passing |
| Visual parity at 320 / 768 / 1024 | Playwright screenshots `/opt/cursor/artifacts/dashboard-960-*.png`, `table-960-1024.png` | evidence captured |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | `b88033f4` |
| `FRONTEND-DESIGN-SYSTEM.md` (pointer) | yes | `b88033f4` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `validate-sdlc-plan.sh style-960-dashboard-theme-tokens` PASS |
| 2 | Failing tests written, test cases designed | yes | hex-hygiene red then green logs |
| 3 | Suite green, coverage held, docs updated | yes (local frontend) | vitest/lint/typecheck/build green; full-repo preflight fails on unrelated stale OpenSpec changes with CLOSED issues |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

Worker `gh` cannot add `in-progress` label (GraphQL resource not accessible).
Coordinator owns label/project board moves and merge after
`check-heavy-ci.sh` exit 0.
