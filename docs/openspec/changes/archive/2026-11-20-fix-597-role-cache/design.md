> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Role list reads go to the database on every call. Writes already go through one management service and the catalog port's save and delete.

## Goals / Non-Goals

**Goals:** a second list read does not hit the port, and a write makes the next read see the new row.
**Non-Goals:** caching the other catalogs, or caching lookup by id or name.

## Decisions

1. Reuse the Caffeine cache already configured with expire-after-write 60 seconds. The role list is a separate cache name so another catalog's write does not drop it.
2. The cache is an outbound decorator in front of `RoleCatalogPersistenceAdapter`. Hexagon rule R2 forbids Spring cache annotations in the application layer. Save and delete drop the list. Lookup by name stays on the delegate, so creating a duplicate still sees the stored row. A writer that skips the adapter stays invisible only until the 60 second TTL.

## Riesgos / Trade-offs

- [A writer outside the catalog adapter is invisible for up to 60 seconds] → the TTL is short and the admin path evicts immediately.

## Testing Strategy

`RoleCatalogCacheTest` counts port reads: two list calls, one read; after create, the list includes the new row and the port was read again. A delete drops the list too.

## Regression Strategy

Existing role use-case tests stay. JaCoCo floors are not lowered.

## Playwright Strategy

No UI change. n/a.

## Deployment Strategy

Ships with the backend image. No migration. The cache is in memory per JVM.

## Rollback Strategy

Revert the commit. The API shape is unchanged.
