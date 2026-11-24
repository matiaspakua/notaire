> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

preflight.sh chains eight `python3 -m unittest discover` calls and one module run; unittest has no parallel mode.

## Goals / Non-Goals

**Goals:** Same tests and failures, a fraction of the wall time.
**Non-Goals:** Changing any test.

## Decisions

D1 One process per test file with `discover -s <dir> -p <file>`, so imports and top-level paths behave as before. D2 ThreadPoolExecutor over subprocesses, size os.cpu_count(). D3 Output only for failing files, plus a one-line summary.

## Riesgos / Trade-offs

Tests that share a file or port could collide when run concurrently; the full run is compared with the serial run before merge.

## Testing Strategy

`test_run_self_tests.py` plants a temp directory with a passing, a failing and two slow tests: exit code, named failure, wall time below the serial sum.

## Regression Strategy

The whole process self-test set passes under the runner with the same counts as the serial run.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
