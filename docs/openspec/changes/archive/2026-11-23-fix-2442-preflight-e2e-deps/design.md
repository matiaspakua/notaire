> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The install lived only in the testing group, which a backend/frontend branch skips.

## Goals / Non-Goals

**Goals:** No environment error reported as a test failure.
**Non-Goals:** Caching dependencies.

## Decisions

A shell function shared by both call sites; the npm script captures the diff status, removes the file, then exits with it.

## Riesgos / Trade-offs

Low: gate and dev script only.

## Testing Strategy

workspace/tests/test_preflight_e2e_deps.py (structure and the npm script's cleanup).

## Regression Strategy

Existing preflight tests stay green.

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
