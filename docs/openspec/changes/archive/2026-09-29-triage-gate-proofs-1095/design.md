# Design — triage gate protects derived values and rejects non-proofs

## Context

`seed_triage` pre-fills `triage.env` (`ISSUE`, `USE_CASE`, `TYPE` from the title,
`TEST_SURFACE`). `gate_triage` checks the worker's files with shell/regex. Rules
that parse criteria lines are hard to test in shell, so the new ones go to Python.

## Goals / Non-Goals

**Goals:** none of the three #1064 rerun failure modes costs an attempt or passes the gate.

**Non-Goals:** restoring `TEST_SURFACE` (the worker may legitimately change it to
`frontend`); judging test quality.

## Decisions

- **Repair, not reject, derived values.** Like `repair_tasks` (#1091): what the
  harness knows it restores, and logs for Gate 4. Only keys seeded with a real
  value (not `?`) are restored, so an underivable `TYPE` stays the worker's.
- **Deny-list of read-only programs.** `grep`, `find`, `cat`, `head`, `tail`,
  `ls`, `wc`, `echo`, `sed -n`, `git ls-tree|log|show|grep` succeed whether or not
  the criterion holds. An optional leading `bash`/`sh` is skipped. An allow-list
  would reject legitimate checks (`bash scripts/x.sh`, `gitleaks`).
- **KIND conflict is a rejection.** The gate cannot know whether the tests or
  the KIND is wrong; the message says tests need `KIND=code`.

## Riesgos / Trade-offs

- A disguised search (`bash -c "grep …"`) passes. Gate 4 still reads the triage.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Derived value restored; Underivable value kept | unit | `local-ai/sdlc/tests/test_triage_check.py` |
| Search command rejected; Script command accepted | unit | `local-ai/sdlc/tests/test_triage_check.py` |
| Docs kind with new test rejected; Code kind with new test accepted | unit | `local-ai/sdlc/tests/test_triage_check.py` |

## Regression Strategy

`python3 -m unittest discover -s local-ai/sdlc/tests`, `bash -n foreman.sh`, and
`RECHECK=1` of `gate_triage` on the stored #1064 retry files.

## Playwright Strategy

n/a — no UI change.

## Deployment Strategy

Merged through the PR. The next `foreman.sh` run uses it.

## Rollback Strategy

`git revert` of the merge commit.

## Migration Plan

None.

## Open Questions

None.
