> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Child of #1693 after registrationdraft #1891 merged (R2=22 on main @ 668d413fc).

## Goals / Non-Goals

**Goals:** Clear ~5 history R2 edges without behaviour change.
**Non-Goals:** Other leftover R2 domains; FE changes; API contract changes.

## Decisions

Delete dead entity HistoryUseCase/RepositoryPort (same as person R2); move HistoryMapper beside OpsAdapter; keep existing OpsPort/DTO path.

## Riesgos / Trade-offs

ManagementOpsAdapter imports HistoryMapper — update package after move.

## Testing Strategy

HistoryMapperTest + HexagonArchitectureTest shrink; existing history integration tests.

## Regression Strategy

History Bruno/API and CU history integration tests.

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
