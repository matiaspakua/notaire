# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1525 | open → in progress |
| Use Case | RNF-07 – Usability; CU76 – UI/UX | exists |
| Specification | `docs/openspec/changes/fix-1525-row-action-icons/` | Gate 1 draft |
| Branch | `fix/1525_notaire_icon_shrink` | created from updated `main` |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | pushed |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Desktop table | `TS-0140` | passing |
| Mobile cards | `TS-0140` | passing |
| Guard | `notaire-icon.test.tsx` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | held (serialize) | — |
| `docs/300-development/303-testing/INVENTORY.md` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-1525-row-action-icons` |
| 2 | Failing tests written, test cases designed | yes | 1 Vitest failure and 3 Playwright failures on the pre-change build |
| 3 | Suite green, coverage held, docs updated | yes | `bash frontend/verify.sh` (see PR) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and the backend from `main` on :8080.
