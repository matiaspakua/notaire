> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`preflight.sh --full` starts a Docker stack for server-backed suites. Worktrees share the same Docker host, so concurrent runs collide. Worktrees also lack `.env` (git-ignored), causing backend crash-loops. The smoke test leaks containers when `docker compose up` fails because `down` never runs.

## Goals / Non-Goals

**Goals:**

- Exclusive lock around the server-backed section using atomic `mkdir` (macOS has no `flock`).
- Copy `.env` from the main checkout when the worktree has none.
- Ensure `docker compose down` always runs in the smoke test, even on failure.

**Non-Goals:**

- Changing `workspace/stack/start.sh` itself.
- Adding `flock` support — `mkdir` is the portable choice.

## Decisions

1. **`mkdir` for locking** — macOS lacks `flock`. Atomic `mkdir` is the standard POSIX alternative. Lock path: `<git-common-dir>/preflight-stack.lock`.
2. **Holder file with PID** — Store PID, branch, and start time so stale locks (dead PID) can be taken over.
3. **`trap` on EXIT for release** — Guarantees lock release even on failure.
4. **`stack_lock.sh` as separate script** — Makes the lock logic independently testable.
5. **`.env` from main checkout** — Use `git rev-parse --git-common-dir` to find the main checkout root.

## Riesgos / Trade-offs

- [Stale lock after crash] → Mitigated by PID liveness check; dead PID means take over.
- [Timeout too short] → Default 3600s (1 hour) is generous; configurable via `PREFLIGHT_STACK_WAIT`.
- [`.env` has secrets] → `.env` is git-ignored; copying within the same host is safe.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Acquire then release lock | unit | `test_stack_lock.py` |
| Second acquire waits and times out | unit | `test_stack_lock.py` |
| Stale lock (dead PID) is taken over | unit | `test_stack_lock.py` |
| Two concurrent acquirers, one wins | unit | `test_stack_lock.py` |
| preflight.sh calls stack_lock.sh acquire | text | `test_stack_lock.py` |
| Smoke test always reaches docker compose down | text | `test_stack_lock.py` |

- New unit tests: `workspace/tests/test_stack_lock.py`
- Coverage impact: N/A (shell script tests, not Java)

## Regression Strategy

- Existing tests affected: none — new script and new test file only.
- `preflight.sh` changes are additive (lock wrap, .env copy, trap).
- No Java code changes.

## Playwright Strategy

n/a — no UI surface. Pure SDLC tooling.

## Deployment Strategy

- Flyway migration required: no
- Configuration or `.env` keys to add: none
- Feature flag: no

## Rollback Strategy

- Revert safe: yes — pure tooling change.
- Database rollback: none needed.

## Migration Plan

N/A — no staged rollout needed.

## Open Questions

None.
