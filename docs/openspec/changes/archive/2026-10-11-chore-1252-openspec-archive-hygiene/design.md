> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Assessment #1242 / issue #1252: agents leave OpenSpec changes active after merge
with unticked `tasks.md` boxes. `validate-sdlc-plan.sh` only checks plan
structure (twelve groups, filled sections), not completion. Constitution Gate 5
requires archive; it is not mechanical today. Related cloud docs already cover
heavy-CI false greens (`CI-MERGE-GATE.md`) but not OpenSpec lifecycle drift.
Baseline inventory (2026-10-11, post-#1515/#1519): ~34 active changes; at least
`feat-1467-infra-dashboards` (#1467 CLOSED) is shipped but unarchived;
`gestion-archive-deuda-check` is an orphan (incomplete schema / artifacts).
Many `fix-*` / `feat-773` / `feat-774` trees are still open-issue WIP or Gate 1
stockpile — reconcile, do not blindly archive.

## Goals / Non-Goals

**Goals:**

- Trustworthy active-change set: shipped work archived (specs synced when deltas
  exist); orphans fixed or archived; WIP left active with honest checkboxes.
- Mechanical process gate: PR touching an OpenSpec change fails when
  implementation task groups remain unchecked (or archive is proven post-merge).
- Cloud agents document the verification step next to heavy-CI merge rules.
- Traceability adapted from TPF / INGENIERIA.md (artifact IDs, RDUF) without
  copying SecTx or GitFlow.

**Non-Goals:**

- Closing product issues by inventing implementation.
- Changing GitHub org rulesets or the merge API's required-check behavior for
  the agent token (#1040 / operational note only).
- Touching `CHANGELOG.md` until #1518 merges.
- Expanding `local-ai/` enforcement.

## Decisions

1. **Extend process checks, do not only document.** A docs-only reminder does
   not restore the status signal. Prefer a `workspace/sdlc/` checker invoked
   from Process Checks / preflight (same family as `check-sdlc-exception.sh`)
   over hoping agents remember Gate 5. Alternative rejected: archive-only
   hygiene PRs without a lasting gate — the drift returns.

2. **Fail on unchecked implementation groups (3–8), not on Gate 1/9–12.**
   Groups 1–2 are planning/branch; 9–12 are commit/PR/deploy/close and stay
   open until after merge. Failing the whole checkbox list would block every
   honest in-flight PR. Alternative rejected: require 100% `[x]` before merge —
   that forces lying about Gate 5 rows.

3. **Archive reconciliation is an apply-phase inventory, not a frozen list.**
   The proposal names the method; apply re-runs issue state + archive presence
   before each move. Alternative rejected: hard-coding the assessment's "18
   changes" — the set already grew and partially self-healed.

4. **SURFACE = none.** No product controllers or UI. Tests live under
   `workspace/tests/` (Python), mirroring other SDLC checkers. Playwright n/a.

5. **CHANGELOG deferred.** Serialize with Agent 3 / #1518. Gate 1 and the
   process PR may land without Unreleased; a follow-up note after #1518 is
   allowed.

6. **PR linking.** Implementation / archive PRs use `Related: #1252` or
   `Refs: #1252` until the acceptance criteria are fully met; only the final
   closing PR uses `Closes #1252`.

## Riesgos / Trade-offs

- [False fail on WIP stockpile PRs] → Scope the check to changes the PR
  modifies; allow `sdlc-exception` with an Exceptions row in traceability.
- [Archive without sync drops delta requirements] → Apply runs
  `openspec-sync-specs` (or equivalent) before `openspec archive` when deltas
  exist; verify main specs afterward.
- [Agents tick boxes without shipping] → The gate pairs with archive hygiene
  and CI green on the issue; documentation states checkboxes are evidence, not
  decoration.
- [CHANGELOG serialization delay] → Explicit non-goal until #1518 merges.
- [Incomplete orphan changes fail validators] → Reconciliation may archive with
  warnings or delete only when Constitution / explore notes already classify
  them as invalid (e.g. `gestion-archive-deuda-check`).

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Shipped change still active fails inventory check | unit | `workspace/tests/test_openspec_archive_hygiene.py` |
| Unchecked implementation tasks on a touched change fail the gate | unit | `workspace/tests/test_openspec_archive_hygiene.py` |
| All implementation groups ticked allows the gate | unit | `workspace/tests/test_openspec_archive_hygiene.py` |
| `sdlc-exception` bypasses the task-completion gate | unit | `workspace/tests/test_openspec_archive_hygiene.py` |
| Cloud merge-gate doc names archive verification | unit | same module (string / path guard) |
| WIP open-issue change is not force-archived | unit | inventory fixture case |

- New unit tests (`workspace/tests/`): OpenSpec lifecycle hygiene
- New integration tests: n/a — no Java API
- Coverage impact (JaCoCo ratchet floor; 80% target): unchanged (no Java)

TDD: write the Python tests against a temp change fixture; observe fail before
the checker exists; then implement.

## Regression Strategy

- Existing tests affected: `workspace/tests/test_docs_business_pages_link.py`
  (archive naming patterns); `workspace/sdlc/validate-sdlc-plan.sh` behavior
  unchanged for structure-only checks.
- Full suite command: `python3 -m unittest workspace.tests.test_openspec_archive_hygiene` (exact module path as landed) plus `bash workspace/sdlc/validate-sdlc-plan.sh chore-1252-openspec-archive-hygiene`
- HTTP/Bruno API suite: n/a
- Legacy paths at risk: none

## Playwright Strategy

n/a — no UI surface. This change only moves OpenSpec trees and adds process
tooling under `workspace/` and cloud docs.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: land checker + workflow wiring with tests; then
  archive reconciliation commit(s) (docs-only / light CI friendly)
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): Process Checks green on a PR that touches
  a fully ticked change; intentional unticked fixture fails locally

## Rollback Strategy

- Revert safe: yes — revert the process-check commit; archived trees can be
  moved back from `archive/` if a false positive archive occurred
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: PRs with honest incomplete tasks stay
  blocked until exception label — low product risk

## Migration Plan

1. Gate 1 (this pack) validated.
2. Implement checker + tests (red → green).
3. Wire into Process Checks / preflight.
4. Update `CI-MERGE-GATE.md` / fleet docs / CU76 pointer.
5. Re-inventory active changes; archive shipped; document orphans.
6. Close #1252 only when AC are met; archive this change.

## Open Questions

- Whether to also fail `validate-sdlc-plan.sh --list` on active changes whose
  GitHub Issue is CLOSED (stronger inventory). Default in this design: separate
  inventory script or test, not Gate 1 structure validation, so stockpile of
  open issues remains usable.
