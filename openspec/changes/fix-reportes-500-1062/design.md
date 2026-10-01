> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

The current implementation answers all report queries by delegating to the repository.
When a non‑existent identifier is provided the repository throws a generic exception that propagates as an HTTP 500. The change introduces a service‑level guard that throws a `ResourceNotFoundException`, which the existing controller advice translates to HTTP 404. This guarantees that clients receive a clear, idempotent error code when a report cannot be located.

## Goals / Non-Goals

**Goals:**

- Ensure all report endpoints return **404 Not Found** for unknown identifiers instead of unexpected 5xx responses.
- Preserve existing behavior for valid report requests.

**Non-Goals:**

- No changes to authentication, authorization, or caching layers.
- No new API endpoints or client‑facing contracts.

**Non-Goals:**

## Decisions

The change is implemented by

- adding a check in `ReportService` that maps a missing entity to a `ResourceNotFoundException`.
- wiring that exception to a `@ControllerAdvice` mapping returning HTTP 404.

**Alternatives**:

- Throwing a generic `RuntimeException` would result in 500 errors – rejected.
- Returning `Optional.empty()` from the controller would make the client payload
    larger and would require extra null checks. The chosen approach keeps the
    controller thin and the semantics clear.
- Adjusting the repository layer to throw a custom exception directly was
    considered but would tangle persistence concerns with API semantics.
The selected path respects separation of concerns and leverages existing
exception‑handling conventions.

## Riesgos / Trade-offs

**Risk 1 – Existing consumers reading the 5xx code**
   *Some clients may currently depend on the payload produced by the legacy
   5xx responses (e.g., an error body). Returning 404 removes that body.
   *Mitigation*: Update the exception handler to serialise a standard error
   DTO; document the change in the API reference and in the contract‑tests.

**Risk 2 – Repository slowness when ID not found**
   *A missing ID triggers a query that touches the database. In high‑traffic
   environments this could become a hotspot.
   *Mitigation*: Ensure the query is indexed on the primary key (it is by
   definition) and rely on the existing `JdbcTemplate` optimisation.

**Risk 3 – Test flakiness due to shared state**
   *Integration tests run against a shared test DB. If a previous test
   creates a report with a known ID that is then deleted, a subsequent test
   could pass incorrectly.
   *Mitigation*: use separate transaction rollbacks or an H2 in‑memory DB
   per test class.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| GET /api/v1/reportes/presupuesto/{id} — 404 when id unknown | integration | ReportesUseCaseIntegrationTest |

- New unit tests (`src/test/java/.../unit/`):
- New integration tests (`src/test/java/.../integration/`):
- Coverage impact (JaCoCo ratchet floor; 80% target):

## Regression Strategy

- Existing tests affected:
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash integration-test/scripts/test.sh`
- Legacy paths at risk (e.g. `jpa` package, `frontend-swing`):

## Playwright Strategy

n/a — no UI change (`UI_CHANGE=no` in triage).

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling:
- Configuration or `.env` keys to add (add to `.env.example`, never commit secrets):
- Feature flag: no
- Smoke test after deploy (Gate 5):

## Rollback Strategy

- Revert safe:

- Database rollback: none needed
- Data written under the new behavior after revert:
- Blast radius if rollback is delayed:

## Migration Plan

None — no schema change.

## Open Questions

None.

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| docs/100-business/102-use-cases/CU24 – Generar libro de índices.md | yes | pending |
