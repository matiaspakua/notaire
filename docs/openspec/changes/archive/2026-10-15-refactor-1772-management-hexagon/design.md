> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Repo-only bypass wave (#1726–#1731, #1770) done. Management remains the largest mixed R1 hotspot; management-status catalog already ports-only (#1719).

## Goals / Non-Goals

**Goals:** ports-only ManagementController + TransitionManagementWebMapper; ArchUnit R1 shrink; REST unchanged; omit CHANGELOG (#1766).
**Non-Goals:** Full domain model extraction; deleting business.DeedManagement; FE catalog #1769; CHANGELOG while #1766 open.

## Decisions

1. Prefer inbound Manage/Query ports returning DTOs/commands so the controller never sees entities. 2. Keep application-layer entity/repository use for this slice (R2 shrink optional). 3. TransitionManagementWebMapper uses QueryManagementsUseCase (or equivalent) instead of DeedManagementRepository. 4. Skip CHANGELOG (#1766).

## Riesgos / Trade-offs

- [Large controller surface] → move methods in one PR but keep REST characterization green.
- [Complete-case orchestration] → preserve substitution, status write guard, procedure folder seed behaviour.

## Testing Strategy

ManagementController* tests, TransitionManagement*, HexagonArchitectureTest shrink, management integration smoke.

## Regression Strategy

mvn test -pl backend-api -Dtest=ManagementController*,ManagementQuery*,TransitionManagement*,HexagonArchitectureTest,ManagementCompleteCase*,ManagementControllerIntegrationTest

## Playwright Strategy

n/a — no UI surface (management REST contract unchanged).

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
