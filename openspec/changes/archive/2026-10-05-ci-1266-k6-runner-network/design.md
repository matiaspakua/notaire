> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates, §7 Testing Rules, §11 Release Rules.

## Context

Issue #1266, Use Case CU74. Runs 8 to 10 failed on a stale script path (fixed by #1179); run 11 shows the remaining defect, `dial tcp 127.0.0.1:8080: connect: connection refused` from inside the k6 container.

## Goals / Non-Goals

**Goals:** k6 reaches the backend; the weekly check is meaningful.
**Non-Goals:** Changing the load profile or thresholds.

## Decisions

1. Install k6 on the runner (`grafana/setup-k6-action`) instead of `docker run --network host`: no image pin to maintain and the same pattern as the other setup actions. Rejected: pointing `BASE_URL` at the runner's bridge IP (fragile).

## Riesgos / Trade-offs

- A real threshold failure can now appear once the test exercises the API; that is the intended signal.

- Measurements are a snapshot (`main` @ `610fa7b0`); the plan makes them reproducible through the metrics issue.

- The Spanish-text and unreachable-endpoint measurements are heuristics; the report and issues state the method and require confirmation before work.

- Description mapping from retired names is done once by position and type and reviewed by hand; a wrong mapping would be a documentation error, not a runtime one.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| k6 runs on the runner | static | `scripts/test_performance_test_assets.py` |

- New unit tests (`src/test/java/.../unit/`): n/a
- New integration tests: n/a
- Coverage impact: none

## Regression Strategy

- Existing tests affected: none
- Full suite command: `bash scripts/preflight.sh`
- HTTP/Bruno API suite: unchanged
- Legacy paths at risk: none

## Playwright Strategy

No UI change; n/a.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: single PR
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): CI green on the merge commit

## Rollback Strategy

- Revert the PR.
