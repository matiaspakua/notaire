# Design — implement scope and worker watchdog

## Context

`phase_implement` sets `SCOPE="^(\.localai/|openspec/changes/<c>/)|$SOURCE_ROOTS"`.
`scope_guard` reverts any changed path the regex does not match, and the
pre-commit hook refuses it. `run_worker` wraps `codex exec` in
`perl -e 'alarm N; exec …'`.

## Goals / Non-Goals

**Goals:** planned non-source files are editable in implement; a worker run
ends at `WORKER_TIMEOUT` whatever the worker does with signals.

**Non-Goals:** widening the scope to whole directories; changing other phases.

## Decisions

- **Planned files come from two places.** Triage `## Files to Edit` is the
  worker's own list. The foreman approves traceability `## Planned Files` at
  Gate 1. #1063's triage missed `CONSTITUTION.md`, but its traceability listed
  it. Only paths that exist in the worktree are taken. A table cell such as
  `JacocoCoverageConfigConsistencyTest.java` is not a path, so it is skipped.
- **Exact paths, not prefixes.** Each file becomes `^<escaped path>$`, so
  planning `CONSTITUTION.md` does not open the repo root.
- **Regex built in Python.** The ERE escaping and the markdown parsing are
  tested there. `foreman.sh` only calls `scope.py implement`.
- **A process-group watchdog, not an alarm.** The alarm sends SIGALRM to the
  exec'd process alone, and it did not end `codex exec`. The watchdog starts
  the worker with `start_new_session=True`, waits with a timeout, sends
  `killpg(TERM)`, and after a grace period (`WATCHDOG_GRACE`, default 10 s)
  sends `killpg(KILL)`. KILL cannot be ignored and reaches the children.
  Exit 124 matches coreutils `timeout`.

## Riesgos / Trade-offs

- A worker could plan an unrelated file in triage to widen its scope. The
  foreman reviews triage and the spec at Gate 1, and each path is exact.
- The worker gets its own session. Terminal Ctrl-C no longer reaches it
  directly; the watchdog forwards INT/TERM.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Planned file outside the source roots; Unplanned file; Non-path cell | unit | `local-ai/sdlc/tests/test_scope.py` |
| Worker finishes in time; Worker ignores TERM | unit | `local-ai/sdlc/tests/test_watchdog.py` |

## Regression Strategy

`python3 -m unittest discover -s local-ai/sdlc/tests`, `bash -n foreman.sh`,
`scope.py implement` on the stored #1063 state, then the #1063 rerun itself.

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
