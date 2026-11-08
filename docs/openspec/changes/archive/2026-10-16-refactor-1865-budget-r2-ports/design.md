> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Epic #1693 R2 wave after workflow #1851. Budget is the highest remaining fan-in (~20 edges).

## Goals / Non-Goals

**Goals:** Clear all application-layer budget R2 edges; keep R5; REST unchanged.
**Non-Goals:** Full `domain.budget` aggregate; item hexagon R2 beyond `existsById` via OpsPort.

## Decisions

1. Relocate four entity helpers to `adapter.out.persistence.budget`; they call `BudgetRepository` directly.
2. Delete `BudgetRepositoryPort` (entity + Spring Data on port); expose `existsById` on `BudgetOpsPort` for `ItemService`; procedure adapter uses Spring Data.
3. Inbound CU39/CU71 ports implemented by relocated helpers (report R2 pattern).

## Riesgos / Trade-offs

Helpers remain large outbound adapters.

## Testing Strategy

Budget unit/controller tests + HexagonArchitectureTest shrink + ItemServiceTest port mock update.

## Regression Strategy

Bruno/budget paths; Playwright n/a.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
