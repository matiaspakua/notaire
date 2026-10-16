> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Epic #1693. Pattern: FE payments #1832. Reuse #1785 folio hexagon and #1817/#1825 backends.

## Goals / Non-Goals

**Goals:** Shrink three protocol eslint suppressions; keep REST behaviour identical.
**Non-Goals:** Do not migrate testimonies (#1834); do not rewrite useReports globally.

## Decisions

New notebook + auxiliary-protocol + protocol-report cores; protocol home reuses folioUseCases/folioTypeUseCases for lists; PDF bytes via dedicated report/notebook gateways.

## Riesgos / Trade-offs

Create notebook must send idsFolio/idNotary/year; auxiliary StartDeedRequest needs idFolio+body.

## Testing Strategy

Vitest use-cases + gateways + architecture-boundaries.

## Regression Strategy

Protocol home reports PDF; notebook create/cover; auxiliary start deed.

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
