> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Epic #1693. Pattern: FE documents #1841; protocol page reuses folio composition.

## Goals / Non-Goals

**Goals:** Shrink eight cross-cutting eslint suppressions; keep behaviour identical.
**Non-Goals:** Do not migrate catalog-other hooks (items/copies/substitutions/properties).

## Decisions

Reuse protocol-report for PDF bytes; reuse folioUseCases.listAvailableNotaries and folioTypeUseCases.list on reports page; dedicated auth core for login/logout.

## Riesgos / Trade-offs

Login lockout 429 body parsing must remain; logout stays best-effort.

## Testing Strategy

Vitest use-cases + gateways + architecture-boundaries.

## Regression Strategy

Login 429; logout; users/roles CRUD; audit page filters; report PDF downloads; registration-draft lifecycle.

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
