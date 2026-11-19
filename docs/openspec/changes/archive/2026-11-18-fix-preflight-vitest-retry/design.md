> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

Four full-suite Vitest failures in this loop passed on the next run of the same file. Each one forced another 30-minute preflight.

## Goals / Non-Goals

**Goals:**

- A single frontend Vitest failure gets one more attempt.
- The second failure is still reported with the last 25 log lines.

**Non-Goals:**

- Hiding a failure that reproduces.

## Decisions

A separate `run_retry` keeps `run` single-attempt. Only the frontend Vitest line uses it, because that is the step that flaked.

## Riesgos / Trade-offs

- [A real failure costs one extra Vitest run] → about 90 seconds, then the gate still fails.
- [A flake that needs a third try still fails] → the recorded flakes passed on the first retry.

## Testing Strategy

| Scenario | Test level | Test |
|----------|------------|------|
| The script defines `run_retry` and uses it for frontend Vitest | unit | `test_preflight_vitest_retry.py` |

TDD: the test failed because `run_retry()` was absent.

## Regression Strategy

- `python3 -m unittest workspace.tests.test_preflight_vitest_retry`
- `bash -n workspace/sdlc/preflight.sh`
- `bash workspace/sdlc/preflight.sh --full`

## Playwright Strategy

n/a — no product UI.

- Specs under `testing/e2e/tests/`: none

## Deployment Strategy

- Flyway: no
- Smoke: the unit test and `bash -n`

## Rollback Strategy

- Revert safe: yes
- Database rollback: none
