> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Management mixed ports-only (#1772) and FE management-status catalog (#1769) done or in flight. History is the next mixed R1 hotspot (~6 edges).

## Goals / Non-Goals

**Goals:** ports-only HistoryController; ArchUnit R1 shrink of six edges; REST unchanged; omit CHANGELOG (#1766).
**Non-Goals:** Full domain model extraction; deleting business.History; retiring unused HistoryUseCase stubs; CHANGELOG while #1766 open.

## Decisions

1. Prefer inbound Manage/Query ports returning DTOs/commands so the controller never sees entities. 2. Keep chronological sort (HistoryOrder) inside the outbound adapter. 3. Preserve pageable sort direction (`property,asc|desc`). 4. Skip CHANGELOG (#1766).

## Riesgos / Trade-offs

- [Delete FK cleanup] → keep status.historyList.remove(entity) semantics inside the adapter.
- [Nested TX] → put @Transactional on adapter write methods carefully; avoid outer TX on caught not-found paths.

## Testing Strategy

SimpleControllersTest$HistoryControllerTests, History*IntegrationTest, HexagonArchitectureTest shrink.

## Regression Strategy

mvn test -pl backend-api -Dtest=SimpleControllersTest,HistoryPaginationIntegrationTest,HistoryDeleteIntegrationTest,HistoryOrderTieBreakerIntegrationTest,HistoryWriteAuthorizationIntegrationTest,HexagonArchitectureTest

## Playwright Strategy

n/a — no UI surface (history REST contract unchanged).

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
