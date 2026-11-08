> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Identification-type (#1732) and management-status (#1769) FE hexagons landed. Documents admin still on api-client suppressions.

## Goals / Non-Goals

**Goals:** FE document-type core + HTTP adapter + composition; suppressions shrink by two; REST unchanged.
**Non-Goals:** Other FE catalogs; CHANGELOG while #1766 open.

## Decisions

1. Mirror #1769 layout. 2. Include search + in-use on the gateway. 3. Skip CHANGELOG (#1766).

## Riesgos / Trade-offs

- [Partial DocumentType fields on create] → map optional enabled/returned/expires/dueDays/deliveredBy faithfully.

## Testing Strategy

document-type use-case + HTTP gateway unit tests; architecture-boundaries pin.

## Regression Strategy

cd frontend && npm test -- --run architecture-boundaries document-type

## Playwright Strategy

n/a — hexagon move; existing documents page coverage; no new Playwright surface required.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
