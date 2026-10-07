> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates, §7 Testing Rules, §11 Release Rules.

## Context

Issue #1307, Use Case CU76, follows ADR-026. `scripts/` mixes system start/stop, SDLC gates, CI report generators, module tools and guards.

## Goals / Non-Goals

**Goals:** each script in the module it serves; no stale reference; `scripts/` removed.
**Non-Goals:** changing what any script does (apart from anchoring the repo root).

## Decisions

1. Scripts and their references move together in one slice, so `main` is never half-moved.
2. One growing table in `test_scripts_layout.py` drives all slices: red first, then the move turns it green.
3. Scripts compute the repo root from their own location; moving adds the extra `..`. Rejected: leaving shims in `scripts/` (keeps the clutter the change removes).

## Riesgos / Trade-offs

- History text (CHANGELOG, archives) keeps old paths on purpose; the guard skips it.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Moved scripts exist at their new path | unit (stdlib) | `workspace/tests/test_scripts_layout.py` |
| Moved scripts are gone from the old path | unit (stdlib) | `workspace/tests/test_scripts_layout.py` |
| No file references an old path | unit (stdlib) | `workspace/tests/test_scripts_layout.py` |

- Coverage impact: none (no production code)

## Regression Strategy

- Full suite command: `bash scripts/preflight.sh`; `--full` after the last slice

## Playwright Strategy

No UI change; the existing suite runs unchanged in the final `--full`.

## Deployment Strategy

- Flyway migration required: no; configuration keys: none
- Smoke test after deploy (Gate 5): CD green on each slice

## Rollback Strategy

- Revert the slice PR.
