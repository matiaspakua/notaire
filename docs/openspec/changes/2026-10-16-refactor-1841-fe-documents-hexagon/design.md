> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Epic #1693. Pattern: FE managements #1839.

## Goals / Non-Goals

**Goals:** Shrink eight document eslint suppressions; keep REST behaviour identical.
**Non-Goals:** Do not migrate reports, audit, users, or catalog-other hooks.

## Decisions

Three cores (submitted-document includes upcoming expirations; document-reentry; external-entity-document); preserve wire field names in gateways.

## Riesgos / Trade-offs

Wire field renames on create/update/reentry bodies.

## Testing Strategy

Vitest use-cases + gateways + architecture-boundaries; hook tests mock composition.

## Regression Strategy

Submitted CRUD; document reentry POST; external-entity PUT; upcoming-expirations days query.

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
