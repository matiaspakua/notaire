> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Epic #1693. Pattern: FE testimonies #1834 / FE budgets #1827.

## Goals / Non-Goals

**Goals:** Shrink eight management eslint suppressions; keep REST behaviour identical.
**Non-Goals:** Do not migrate documents, reports, or catalog-other hooks.

## Decisions

Single core/management covering CRUD, summaries, folders, and search helpers; preserve CompleteCaseRequest wire names in the gateway; duck-type 404/409 on UI.

## Riesgos / Trade-offs

Complete-case typeProcedureId mapping regression; archive confirmado 409 path.

## Testing Strategy

Vitest use-cases + gateway + architecture-boundaries; keep hook wire regressions via gateway tests.

## Regression Strategy

Page list; complete-case create/update; archive with confirmado; transition; current-status 404; picker search.

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
