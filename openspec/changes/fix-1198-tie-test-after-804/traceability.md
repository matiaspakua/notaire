# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```unknown
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1198 | open → in progress (hotfix for a regression of PR #1200) |
| Use Case | CU13 – gestion status and history | exists |
| Related | PR #1200 (the fix and the test), PR #1199 / #804 (the rule the test broke) | referenced |
| Specification | `openspec/changes/fix-1198-tie-test-after-804/` | Gate 1 draft |
| Branch | `fix/1198_tie_test_after_804` | created from updated `main` |
| Tasks | `tasks.md` | test rewritten and verified; pipeline, PR, Gates 4-5 pending |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| The tie test passes with the fix | `ManagementHistorialOrphanWriteIntegrationTest#shouldReturnLaterHistoryRowWhenDatesTie` (3 of 3) | covered |
| The tie test fails without the fix | same, comparator removed (3 of 3 failures), then restored | covered |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md`, CU13 | none needed | n/a |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash scripts/validate-sdlc-plan.sh fix-1198-tie-test-after-804` |
| 2 | Failing tests written, test cases designed | yes | `main` fails 1 of 11 (clean worktree); rewritten test fails 3 of 3 without the fix |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None.
