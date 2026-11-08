> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Identification-type FE hexagon landed (#1732). Backend management status is ports-only (#1719).

## Goals / Non-Goals

**Goals:** FE management-status core + HTTP adapter + composition; suppressions shrink; REST unchanged.
**Non-Goals:** Other FE catalogs; CHANGELOG while #1766 open.

## Decisions

1. Mirror #1732 layout. 2. Include search + in-use on the gateway. 3. Skip CHANGELOG (#1766).

## Riesgos / Trade-offs

- [Page also uses workflows hooks] → leave those as-is; only move management-status API calls.

## Testing Strategy

management-status use-case + HTTP gateway unit tests; architecture-boundaries pin; existing management-statuses page tests.

## Regression Strategy

cd frontend && npm test -- --run architecture-boundaries management-status management-statuses

## Playwright Strategy

n/a — existing unit coverage for admin page; no new Playwright surface required for hexagon move.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
