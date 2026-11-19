> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Identification-type list reads go to the database on every call. Writes already go through one management service.

## Goals / Non-Goals

**Goals:** a second list read does not hit the port, and a write makes the next read see the new row.
**Non-Goals:** caching the other catalogs, or caching get-by-id.

## Decisions

1. Caffeine, expire-after-write 60 seconds, plus evict-on-write. The TTL covers a writer that does not go through the management service.
2. The name-uniqueness check keeps reading the port, so it cannot approve a duplicate from a stale list.

## Riesgos / Trade-offs

- [A writer outside the management service is invisible for up to 60 seconds] → the TTL is short and the admin path evicts immediately.

## Testing Strategy

`IdentificationTypeCatalogCacheTest` counts port reads: two list calls, one read; after create, the list includes the new row and the port was read again.

## Regression Strategy

Existing identification-type use-case tests stay. JaCoCo floors are not lowered.

## Playwright Strategy

No UI change. n/a.

## Deployment Strategy

Ships with the backend image. No migration. The cache is in memory per JVM.

## Rollback Strategy

Revert the commit. The API shape is unchanged.
