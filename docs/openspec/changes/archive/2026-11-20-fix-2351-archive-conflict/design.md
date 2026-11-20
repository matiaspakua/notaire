> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Archive rejection already has `ManagementArchiveConflictException` with folder numbers. The service still throws `FoldersInWaitException`, which carries the JPA entity, and the adapter unwraps it.

## Goals / Non-Goals

**Goals:** the hold rejection is the domain exception. The HTTP 409 body stays the message plus the folder numbers.
**Non-Goals:** moving report, audit, security, or seed code off `business.*`.

## Decisions

1. Throw `ManagementArchiveConflictException` from the archive service with `ProcedureFolder.getNumber()`. The ops adapter catches that type. The old exception is deleted.

## Riesgos / Trade-offs

- [A caller that caught `FoldersInWaitException` by type breaks] → the only caller is `ManagementOpsAdapter`, updated in this change. The HTTP result type is unchanged.

## Testing Strategy

`ManagementArchiveDebtServiceTest` expects `ManagementArchiveConflictException` and folder number 1. It fails while the service still throws `FoldersInWaitException`.

## Regression Strategy

The confirmed-archive test stays. JaCoCo floors are not lowered.

## Playwright Strategy

No UI change. n/a.

## Deployment Strategy

Ships with the backend image. No migration.

## Rollback Strategy

Revert the commit. The API shape is unchanged.
