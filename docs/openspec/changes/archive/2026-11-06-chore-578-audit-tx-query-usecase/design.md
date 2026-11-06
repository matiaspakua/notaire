> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

#578: only AuditRecordController still has `@Transactional` in `adapter.in.web`.

## Goals / Non-Goals

**Goals:** ['TX on AuditRecordQueryUseCaseService', 'Controller uses QueryAuditRecordsUseCase', 'Guard: no @Transactional under adapter.in.web']
**Non-Goals:** ['Closing entire #578 business-logic epics beyond TX placement']

## Decisions

['Mirror HistoryQueryUseCaseService pattern']

## Riesgos / Trade-offs

['None — same read-only TX, different layer']

## Testing Strategy

['AuditRecordControllerNoWebTransactionalTest', 'update AuditRecordControllerTest mocks']

## Regression Strategy

['Existing audit controller + service unit tests']

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
