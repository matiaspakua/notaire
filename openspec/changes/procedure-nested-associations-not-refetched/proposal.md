# POST/PUT /api/v1/tramites loses persisted state of nested associations

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal
> documents only this change; permanent documentation remains the single
> source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #981 |
| Use Case | CU82 – Generar y hacer seguimiento de la minuta de inscripción |
| Branch | `fix/981_procedure-nested-associations-not-refetched` |
| Gate 1 status | complete |

## Objetivo

`ProcedureController` (`adapter.in.web.procedure`) accepts the raw JPA
`Procedure` entity as its request body (no DTO). When a client sends a
nested reference object for `fkIdDeed`/`fkIdProperty`/`fkIdBudget`/
`fkIdManagement`/`fkIdProcedureType` — e.g. `{"fkIdDeed": {"idDeed": 96}}`
— Jackson deserializes a brand-new **transient** entity carrying only the
id field, all other fields default/blank. `ProcedurePersistenceAdapter.save()`
passes that transient object straight to `ProcedureRepository.save()`
without re-fetching the real, already-persisted row, so Hibernate cascades
the blank/default state onto the association (for `@ManyToOne(fetch =
EAGER)` associations without `CascadeType.MERGE`/`PERSIST` this typically
means the FK id is retained but Hibernate returns whatever managed instance
it already has in the persistence context/session-level cache rather than
the caller's stale field values — in practice, callers doing an immediate
`GET` after `POST`/`PUT` observe defaulted association data). This breaks
CU82's golden path: `RegistrationDraftService` looks up "a trámite with a
property associated to the deed" via the just-created `Procedure`, and gets
an empty/defaulted association back.

## What Changes

- `ProcedurePersistenceAdapter.save()` re-fetches each non-null nested
  association (`fkIdDeed`, `fkIdProperty`, `fkIdBudget`, `fkIdManagement`,
  `fkIdProcedureType`) by its id from the corresponding repository before
  delegating to `ProcedureRepository.save()`, so the persisted `Procedure`
  always carries the real, previously-persisted association data — never
  the client-supplied placeholder.
- If a referenced id does not exist, the adapter throws
  `ResourceNotFoundException` (caught by the controller's existing
  try/catch, same 500 contract as today for any other persistence failure
  — no new error-response shape).

**BREAKING CHANGES:** None — the REST contract (URL, request/response
shape, status codes) is unchanged; only the persisted/returned association
data becomes correct instead of defaulted.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A `Procedure`'s nested association references must resolve to the real, previously-persisted entity, never a client-supplied placeholder | Issue #981 | Made explicit (implicit expectation of any FK reference, now enforced) |

## Capabilities

- `procedure-nested-association-integrity`: `POST`/`PUT /api/v1/tramites`
  persist and return the real state of every referenced association.

## Impact Analysis

### Módulos afectados

| Módulo | Archivo | Cambio |
|--------|---------|--------|
| backend-api | `adapter/out/persistence/procedure/ProcedurePersistenceAdapter.java` | Re-fetch associations before save |
| backend-api (test) | `unit/procedure/ProcedurePersistenceAdapterTest.java` (new) | Unit tests with fake ports/mocked repositories |
| backend-api (test) | `integration/ProcedureNestedAssociationIntegrationTest.java` (new) | Postgres-backed reproduction of the bug + fix verification |

No DB schema change. No frontend change (the frontend already sends nested
`{id}` references, consistent with existing convention elsewhere in the
codebase — e.g. `{"person": {"personId": N}}`). No new REST endpoint.

## Documentation Impact

- None required beyond this specification: no permanent-doc-described
  behavior changes (the correct behavior — associations resolve to their
  real persisted state — was always the implicit contract; this fixes a
  bug against it, not a documented behavior change).

## Out of Scope

- `TS-0082-minuta-inscripcion-feature.spec.ts` itself is not modified by
  this change (it should pass once this backend fix lands; if it still
  fails afterward that is a separate, new finding).
- The legacy `jpa.ProcedureJpaController` (if any) is not touched —
  `ProcedureController` already fully bypasses it per the #993/#995
  hexagonal migration.
