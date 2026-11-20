> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Procedure-type list reads go to the database on every call. Writes already go through the ops port's create, update, delete, and workflow assignment.

## Goals / Non-Goals

**Goals:** a second list read does not hit the port, and a write makes the next read see the new row, including a workflow assignment.
**Non-Goals:** caching get-by-id or name search.

## Decisions

1. Reuse the Caffeine cache already configured with expire-after-write 60 seconds. The procedure-type list is a separate cache name so another catalog's write does not drop it.
2. The cache is an outbound decorator in front of `ProcedureTypeOpsAdapter`. Hexagon rule R2 forbids Spring cache annotations in the application layer. Create, update, delete, and assign-workflow drop the list, because assigning a workflow changes the row the list shows. A writer that skips the adapter stays invisible only until the 60 second TTL.

## Riesgos / Trade-offs

- [A writer outside the catalog adapter is invisible for up to 60 seconds] → the TTL is short and the admin path evicts immediately.

## Testing Strategy

`ProcedureTypeCatalogCacheTest` counts port reads: two list calls, one read; after create, the list includes the new row and the port was read again. Assigning a workflow and a delete each drop the list too.

## Regression Strategy

Existing procedure-type use-case tests stay. JaCoCo floors are not lowered.

## Playwright Strategy

No UI change. n/a.

## Deployment Strategy

Ships with the backend image. No migration. The cache is in memory per JVM.

## Rollback Strategy

Revert the commit. The API shape is unchanged.
