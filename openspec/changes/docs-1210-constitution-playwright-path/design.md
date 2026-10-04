> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates, §7 Testing Rules, §11 Release Rules.

## Context

Issue #1210, Use Case CU76. #1192 moved the suite; the Constitution (§4, §5 step 15, §7) still says
`frontend/tests/e2e`. #1192 exempted `CONSTITUTION.md` from its guard on purpose, citing this issue.

## Goals / Non-Goals

**Goals:** correct paths and command; the guard covers the Constitution from now on.
**Non-Goals:** any change to a process step, gate or rule; touching §13 beyond the path.

## Decisions

1. Replace only the path and command text. Rejected: restructuring §7, which would change the process.
2. Remove the exemption in the guard first (red), then edit the Constitution (green).

## Riesgos / Trade-offs

- [Stacked on #1212] → this PR targets `main` and includes #1212's commits until it merges; the diff for review is the last two commits.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| No stale Playwright path in the Constitution | static | `scripts/test_testing_standalone.py` `E2ELegacyReferenceTest` |
| E2E command points at testing/e2e | static | same (pattern `cd frontend && npx playwright`) |
| Agent rule files stay consistent | static | `bash scripts/check-agent-rules.sh` |

- New unit tests (`src/test/java/.../unit/`): n/a
- New integration tests: n/a
- Coverage impact: none

## Regression Strategy

- Existing tests affected: none; `python3 -m unittest discover -s scripts/tests`.
- Full suite command: `bash scripts/preflight.sh`.
- HTTP/Bruno API suite: unchanged.
- Legacy paths at risk: none.

## Playwright Strategy

No UI change; n/a.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: merge right after #1212
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): CI green on the merge commit

## Rollback Strategy

- Revert the PR; documentation only.
