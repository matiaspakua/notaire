> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #1198, hotfix. `main` at `31caed44`: CI failed, CD skipped. Cause: PR #1200 merged first and added
`shouldReturnLaterHistoryRowWhenDatesTie`, which changes status with `PUT /gestiones/{id}`; PR #1199 (#804)
then made that a 400. Verified on a clean worktree of `origin/main`: 11 tests, 1 failure.

## Goals / Non-Goals

**Goals:** green `main`; the tie test keeps proving the #1198 bug.
**Non-Goals:** production code; changing #804.

## Decisions

1. **Append the second History row directly**, as #804 did for `shouldReturnEstadoActualFromHistoryWhenRowsExist`.
   - Rejected: calling `POST /transition` — needs a workflow graph the test does not otherwise require.
2. **Prove the test still guards the bug**: with the production comparator removed the test fails 3 of 3;
   with it, it passes 3 of 3.

## Riesgos / Trade-offs

- [Merge-order conflicts between green PRs] → not solved here; stated in the PR.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| The tie test passes with the fix | integration (H2) | `ManagementHistorialOrphanWriteIntegrationTest#shouldReturnLaterHistoryRowWhenDatesTie` |
| The tie test fails without the fix | mutation check | same, run with the comparator removed |

- New unit tests (`src/test/java/.../unit/`): n/a
- New integration tests: one rewritten
- Coverage impact (JaCoCo): none

## Regression Strategy

- Existing tests affected: the same class; all 11 tests pass 3 consecutive runs.
- Full suite command: `mvn verify -pl backend-api`, then `bash scripts/run_pipeline.sh`.
- HTTP/Bruno API suite: unchanged.
- Legacy paths at risk: none.

## Playwright Strategy

n/a — no UI change.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: single PR; restores a green `main` so CD can publish.
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): CI and CD green on the merge commit.

## Rollback Strategy

- Revert the PR; the test returns to its failing state, so only revert together with #804.
