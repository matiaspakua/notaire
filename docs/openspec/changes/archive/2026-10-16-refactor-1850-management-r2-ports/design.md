> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Epic #1693 R2 wave after report #1849. Management is the next highest-fan domain (~56 R2 edges).

## Goals / Non-Goals

**Goals:** Clear all application-layer management R2 edges; keep R5 (inbound ↛ outbound); management REST unchanged.
**Non-Goals:** Full domain aggregate for every management field; rewriting workflow hexagon (#1851).

## Decisions

1. Convert CU83 ports to domain/ID shapes: `ManagementTransitionSnapshot` (id + status id/name), `domain.management.ManagementStatus` for status lookup, workflow definition id + status names for validation/logging.
2. Relocate entity-bound helpers (`ManagementQueryService`, archive/case/financial/status-log/substitution/transition, `ManagementStatusWriteGuard`) to `adapter.out.persistence.management` (same pattern as deed/report R2).
3. Extend `ManagementOpsPort` + `QueryManagementsUseCase` / `ManageManagementsUseCase` for case-summary, financial summary, pending balance; wire through existing use-case services.
4. Introduce `ManagementAutoTransitionPort` (management id + destination status name) so `ExternalEntityDocumentService` does not depend on the relocated transition adapter.

## Riesgos / Trade-offs

Helpers remain large outbound adapters; further aggregate extraction can follow. `TransitionManagementUseCaseImpl` stays in application with domain-only ports (not relocated) so CU83 unit tests with fakes remain meaningful.

## Testing Strategy

Existing management unit/controller tests + `TransitionManagementUseCaseTest` fake-port updates + `HexagonArchitectureTest` shrink.

## Regression Strategy

Bruno/management paths; Playwright n/a.

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
