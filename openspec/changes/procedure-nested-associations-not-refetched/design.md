# Design — Re-fetch Procedure's nested associations on save

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) §2, §7.

## Context

`ProcedureController` binds the raw JPA `Procedure` entity as its request
body (pre-dating the hexagonal migration's DTO conventions elsewhere).
`ProcedurePersistenceAdapter.save()` is a thin pass-through to
`ProcedureRepository.save()`. When the client sends a nested reference
object for an association, Jackson builds a transient entity with only the
id populated — everything else defaults. Passing that straight to
`save()` risks the persisted/returned association reflecting the blank
transient state instead of the real, already-persisted row.

## Goals / Non-Goals

### Goals

- Every non-null nested association on a `Procedure` create/update
  resolves to its real, previously-persisted state.
- No REST contract change.
- No DB schema change.

### Non-Goals

- Introducing a request DTO for `Procedure` (would be a larger, separate
  change — out of scope here; the fix stays inside the existing outbound
  adapter, consistent with "adapt, don't replace", Constitución P10).
- Fixing any other entity's controller that has the same raw-entity
  pattern (only `Procedure`/#981 is in scope).

## Decision

`ProcedurePersistenceAdapter.save()` re-fetches each non-null nested
association by its id via the corresponding repository
(`DeedRepository`, `PropertyRepository`, `BudgetRepository`,
`DeedManagementRepository`, `ProcedureTypeRepository`) before delegating to
`ProcedureRepository.save()`. If a referenced id does not exist, throws
`ResourceNotFoundException` — the controller's existing generic
`catch (Exception e)` already converts that into a 500 with logging,
preserving the current error contract without introducing a new one.

### Alternatives considered

- **Introduce a `ProcedureRequest` DTO and map manually.** Rejected for
  this change: larger surface, touches the controller's request/response
  shape (risk of an accidental contract change), and is the kind of
  broader refactor better done as its own tracked change once more of
  `backend-api` has a DTO layer, not smuggled into a bug fix.
- **Rely on `@ManyToOne(cascade = MERGE)` to fix it at the JPA-mapping
  level.** Rejected: cascading merge on these associations would let a
  `Procedure` write mutate the referenced `Deed`/`Property`/etc. rows
  themselves if the client sent extra fields — a bigger, riskier change
  to entity semantics than re-fetching in the adapter.

## Riesgos / Trade-offs

- One extra `findById` per non-null association on every `Procedure`
  save (up to 5 additional queries). Accepted: `Procedure` writes are
  low-frequency (manual notary workflow), and the alternative (leaving
  the bug in place) breaks CU82's golden path.
- Re-fetching by id means a stale/nonexistent id now fails loudly
  (`ResourceNotFoundException` → 500) instead of silently persisting a
  placeholder. This is the intended, safer behavior per the spec.

## Testing Strategy

- Unit: `ProcedurePersistenceAdapterTest` — fake/mocked repositories,
  assert `save()` re-fetches each non-null association and passes the
  *real* fetched instance to `ProcedureRepository.save()`, not the
  transient one.
- Integration: `ProcedureNestedAssociationIntegrationTest` — Postgres-backed,
  reproduces the bug (currently fails), then verifies the fix per the
  spec's 4 scenarios.

## Regression Strategy

- Full backend suite (`mvn test -pl backend-api`) and the Postgres
  profile (`mvn test -pl backend-api -Ppg-integration`) both re-run
  before merge to catch any regression in other `Procedure` consumers.
- No other controller/adapter touches `ProcedurePersistenceAdapter`, so
  blast radius is limited to `/api/v1/tramites` create/update.

## Playwright Strategy

- No new Playwright test added — out of scope per `proposal.md`.
  `TS-0082-minuta-inscripcion-feature.spec.ts` already covers CU82's
  golden path end-to-end and should be re-verified (not modified) once
  this backend fix lands, per "Out of Scope".

## Deployment Strategy

- Standard rolling deploy via existing CI/CD pipeline; no schema
  migration, no config change, no feature flag needed.

## Rollback Strategy

- Revert the merge commit; `ProcedurePersistenceAdapter.save()` reverts
  to the pass-through behavior. No data migration to undo since no
  schema change was made.
