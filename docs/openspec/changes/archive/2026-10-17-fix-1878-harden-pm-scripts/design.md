> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`apply_milestone_policy.py`, `check-board-status-label-coherence.py`, and Owner
shell helpers already exist. Gaps from #1847/#1856/#1874 left apply fragile and
`--check` incomplete. Snapshots and Owner scripts are already on this branch after
the live Owner apply; this design hardens behavior without re-running `--apply`.

## Goals / Non-Goals

**Goals:**

- Survive a whole apply against a fake GitHub that rejects `due_on: null`.
- Make `--check` and coherence `--scope all` complete, fast, and actionable.
- Version Owner scripts; guard premature `Closes` on multi-step PM work.

**Non-Goals:**

- Re-apply live milestones (Owner done; mismatches already 0).
- Label phase 2 (#1860).
- Teaching GraphQL to set group-by/sort the API cannot write.

## Decisions

- Fake GitHub is an in-process double of `GitHub.request`/`graphql` used only in
  unit tests; rejected alternative: recorded HTTP cassettes (too brittle for
  milestone create shapes).
- Snapshot freshness: `--check` compares committed JSON open/closed counts and
  milestone titles to live state (or fails with a clear "commit snapshot PR"
  message). Rejected: always rewrite on check (would dirty agent checkouts).
- Board `--scope all`: one paginated GraphQL project-items query that includes
  issue state, labels, Status field, and linked PR open count — no per-issue `gh`
  calls. Rejected: keep `gh project item-list` + N×`gh issue view` (300s+).
- `open_prs` on `BoardItem`: integer from linked PR nodes with `state == OPEN`;
  R6 only when not `None`.
- Status Done sync: `sync_closed_status_done.py` (or apply step) mutates Project
  Status via GraphQL; unit-tested with a fake.
- VIEW group/sort: extend `VIEW_CONTRACTS` (or sibling `VIEW_LAYOUT`) with
  expected groupBy / verticalGroupBy / sort when GraphQL returns them; document
  API-unreachable settings in `docs/github/README.md`.
- Current train: helper `current_train()` returns earliest open scheduled `vX.Y`
  title from policy constants; filter uses it. Document train-cut retarget if
  derivation would change filters unexpectedly mid-apply.
- Closes guard: new Process Checks script scans PR body + commit messages for
  `Closes`/`Fixes` when the touched change has `multi_pr: true` or tasks mention
  Owner-pending; allow `Refs`.
- Owner scripts: `workspace/sdlc/owner/*.sh`, preview default; CI `bash -n` +
  shellcheck when available.

## Riesgos / Trade-offs

- [Stricter `--check` fails until snapshot PR lands] → this PR includes refreshed
  snapshots from the Owner apply.
- [GraphQL field availability varies] → treat missing sort fields as UI-only docs,
  not hard failures.
- [Closes guard false positives on unrelated PRs] → only when the diff touches an
  active OpenSpec change with `multi_pr` / Owner-pending markers.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Whole-run rejects due_on null, ends clean | unit | `workspace/sdlc/test_milestone_policy.py` |
| Richer check reports drift classes | unit | `workspace/sdlc/test_milestone_policy.py` |
| Scopes preflight / step errors | unit | `workspace/sdlc/test_milestone_policy.py` |
| open_prs + R6 live-shaped | unit | `workspace/tests/test_board_status_label_coherence.py` |
| GraphQL all-scope pagination shape | unit | `workspace/tests/test_board_status_label_coherence.py` |
| Status Done sync | unit | `workspace/tests/test_sync_closed_status_done.py` |
| Closes/Refs guard | unit | `workspace/tests/test_check_closes_refs_guard.py` |
| Owner scripts bash -n | unit/CI | `workspace/tests/test_owner_scripts.py` |

- New unit tests under `workspace/sdlc/` and `workspace/tests/`.
- No Java / JaCoCo impact.
- Playwright: n/a — no UI surface.

## Regression Strategy

- Existing: `CreateBodyTests`, `ConsolidationTests`, coherence R6 in
  `test_label_taxonomy.py` / `test_board_status_label_coherence.py`.
- Do not weaken `create_body` assertions.

## Playwright Strategy

n/a — no UI surface; Process Checks and workspace unit tests are Gate 3 evidence.

## Deployment Strategy

- Flyway: no. Feature flag: no.
- Smoke: `python3 workspace/sdlc/apply_milestone_policy.py --check` → mismatches: 0
  (Owner already applied). Do not re-run `--apply`.

## Rollback Strategy

- Revert safe: yes (scripts + docs + CI steps).
- Blast radius if delayed: stricter check may fail PRs until snapshots match.

## Migration Plan

1. Gate 1 artifacts → failing tests → implement → mark tasks.
2. Commit snapshots already written by Owner apply (do not discard).
3. Single PR with `Closes #1878` (this change is the final PR; set
   `multi_pr: false` / omit unless intermediate PRs are needed).

## Open Questions

None. Current train derivation ships with a documented fallback in
`docs/github/README.md` if the earliest open train differs from `RELEASE`.
