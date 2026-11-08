> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Epic #1693 R2 wave after management #1850. Workflow has ~30 R2 edges: 3 dead entity ports + Trace/Reentry helpers.

## Goals / Non-Goals

**Goals:** Clear all application-layer workflow R2 edges; keep R5; REST unchanged.
**Non-Goals:** New domain aggregates for trace/reentry rows; rewriting catalog hexagon.

## Decisions

1. Delete dead `Workflow*RepositoryPort` + legacy `*PersistenceAdapter` (no use-case consumers; `*CatalogPort` is the live path).
2. Relocate `WorkflowTraceService` / `DocumentReentryService` to `adapter.out.persistence.workflow` implementing inbound ports (same pattern as report R2).
3. Controllers inject `QueryWorkflowTraceUseCase` / `DocumentReentryUseCase` only.

## Riesgos / Trade-offs

Helpers remain large outbound adapters; further port/view extraction can follow.

## Testing Strategy

Existing WorkflowTrace/DocumentReentry unit tests + ManagementController wiring + HexagonArchitectureTest shrink.

## Regression Strategy

Bruno/management workflow paths; Playwright n/a for this slice.

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
