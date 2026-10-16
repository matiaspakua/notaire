> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Epic #1693 R2 wave after deed #1852. Report was the highest-fan domain (~56 R2 edges).

## Goals / Non-Goals

**Goals:** Clear all application-layer report R2 edges; keep R5 (inbound ↛ outbound); PDF REST unchanged.
**Non-Goals:** Full domain aggregates for every report row; rewriting Jasper-era templates.

## Decisions

1. Relocate entity-bound report builders/`ReportService` to `adapter.out.report` (same pattern as deed #1852).
2. Expose `GenerateReportsUseCase` inbound port returning `byte[]` so `ReportController` / `NotebookController` do not import outbound adapters (R5).
3. Delete dead `DocumentCostTemplateRepositoryPort` (no application callers; document cost hexagon uses other adapters).

## Riesgos / Trade-offs

`ReportService` remains a large outbound adapter; further port/view extraction can follow in later slices.

## Testing Strategy

Builder unit tests + `ReportControllerTest` + `ReportServicePaymentReceiptTest` + `HexagonArchitectureTest` shrink.

## Regression Strategy

Bruno/report PDF paths; Playwright n/a.

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
