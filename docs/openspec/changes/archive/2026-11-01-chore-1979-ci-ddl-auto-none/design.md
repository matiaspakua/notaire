> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Production default is `ddl-auto=none` (`application.properties`). Four CI backend
starts still pass `--spring.jpa.hibernate.ddl-auto=update`.

## Goals / Non-Goals

**Goals:** Flyway-only schema in E2E/DAST/k6; ratchet test against regressions.
**Non-Goals:** Broader profile redesign (#583).

## Decisions

1. Use `none` (same as production default), not `validate`, for a one-line swap.
2. Guard via `workspace/tests/test_ci_ddl_auto_none.py` in Process Checks discovery.

## Riesgos / Trade-offs

- [Risk] CI red if schema drift exists → Mitigation: that is the bug we want visible; fix with Flyway.

## Testing Strategy

`CiDdlAutoNoneTest` — observed red with `update`, green with `none`.

## Regression Strategy

Existing Playwright/Bruno/DAST/k6 workflows; heavy CI owns runtime proof.

## Playwright Strategy

n/a — workflow flag only; E2E still run in CI.

## Deployment Strategy

Ships with next merge to main.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
