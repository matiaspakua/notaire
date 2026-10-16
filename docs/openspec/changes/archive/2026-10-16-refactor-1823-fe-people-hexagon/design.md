> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Concepts (#1781) and templates (#1788) FE hexagons landed. People hooks still on api-client suppressions.

## Goals / Non-Goals

**Goals:** FE person core + HTTP adapter + composition; suppressions shrink by two; REST unchanged.
**Non-Goals:** Other FE catalogs; CHANGELOG while #1766 open.

## Decisions

1. Mirror #1781 layout. 2. Gateway covers getById, page, search, create/update/delete. 3. Skip CHANGELOG (#1766).

## Riesgos / Trade-offs

- [personId vs domain id] → map personId on the wire; domain uses `id`.

## Testing Strategy

person use-case + HTTP gateway unit tests; architecture-boundaries pin.

## Regression Strategy

cd frontend && npm test -- --run architecture-boundaries person people-search people-pagination

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
