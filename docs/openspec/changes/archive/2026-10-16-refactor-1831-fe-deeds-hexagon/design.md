> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Epic #1693 ADR-030. Pattern: FE budgets #1827 / PR #1830.

## Goals / Non-Goals

**Goals:** Shrink three deed-related eslint suppressions; keep REST behaviour identical.
**Non-Goals:** No OpenAPI or backend changes; do not migrate payments/protocol/testimonies in this PR.

## Decisions

Mirror budget hexagon: thin domain matching DeedResponse; folio list via folioUseCases.list(); signed-deed filter stays in useDeedSearch as today.

## Riesgos / Trade-offs

DeedPicker / pagination Vitest that stub fetch must still reach api-client through the new gateway.

## Testing Strategy

Vitest use-cases + gateway + architecture-boundaries; existing deed-picker and pagination tests.

## Regression Strategy

Deeds list paging, number search, sign, CRUD; DeedPicker recent/search.

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
