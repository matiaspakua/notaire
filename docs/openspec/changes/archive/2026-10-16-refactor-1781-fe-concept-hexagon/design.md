> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Document-type FE hexagon (#1777) in flight. Concepts admin still on api-client suppressions.

## Goals / Non-Goals

**Goals:** FE concept core + HTTP adapter + composition; suppressions shrink by two; REST unchanged.
**Non-Goals:** Other FE catalogs; CHANGELOG while #1766 open.

## Decisions

1. Mirror #1777 layout. 2. Include search + in-use on the gateway. 3. Skip CHANGELOG (#1766).

## Riesgos / Trade-offs

- [value/percentage optional fields] → map Partial Concept faithfully.

## Testing Strategy

concept use-case + HTTP gateway unit tests; architecture-boundaries pin.

## Regression Strategy

cd frontend && npm test -- --run architecture-boundaries concept

## Playwright Strategy

n/a — hexagon move; no new Playwright surface required.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
