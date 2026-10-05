> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates, §7 Testing Rules, §11 Release Rules.

## Context

Issue #1279, Use Case CU76; slice 1 of #577. Measured on `main`: 38 REST controllers; 8 expose an entity in 14 handler signatures; 69 handlers return a loose type; 28 controllers call repositories directly; 51 `Dto*` classes of which 2 are unused and 14 are used only by the entities' own `getDto()`/`setAtributo()`.

## Goals / Non-Goals

**Goals:** records at the 14 handlers, an enforced guard, 2 unused DTOs deleted.
**Non-Goals:** entity `getDto()` removal, repository-calling controllers, the 69 loose returns, deleting the `dto` package (later slices).

## Decisions

1. Records are nested in their controller and built by a `from` factory on the record, following `DeedController.DeedResponse`. Rejected: a shared mapper class (adds a layer and an `-Er` name for no gain), `Dto*` classes (the thing being removed).
2. Related entities become slim references so no personal data or lazy collection leaks. Rejected: reusing the full `PersonResponse` for a notary (carries tax id and address).
3. The guard uses reflection and Spring's classpath scanner, no ArchUnit (one assertion does not justify a dependency).
4. Loose returns are held by a baseline file that may only shrink, the same ratchet policy as JaCoCo. Rejected: banning them now (69 handlers, mostly error-or-body unions, would make this slice unreviewable).

## Riesgos / Trade-offs

- A client reading a dropped nested field breaks; the frontend types already declare only the slim shapes.
- `ManagementController.getByClient` serialized an entity with fields the frontend type (`statusActual`, `procedureCount`) never received; the record keeps the entity fields and does not invent them.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| No entity in a handler signature | unit | `ControllerSignatureArchitectureTest` |
| Loose returns do not grow | unit | `ControllerSignatureArchitectureTest` |
| Folio and notebook expose a slim notary | integration | `ControllerResponseRecordsIntegrationTest` |
| Cost templates and procedures expose references by id and name | integration | `ControllerResponseRecordsIntegrationTest` |
| Available notaries are person records | integration | `ControllerResponseRecordsIntegrationTest` |
| Unused DTOs are gone | unit | `DtoRemovalTest` |

- Updated tests: existing controller tests that assert entity JSON
- Coverage impact: neutral to positive

## Regression Strategy

- Full suite command: `mvn verify -pl backend-api; python3 -m unittest discover -s scripts/tests; bash scripts/run_pipeline.sh`
- HTTP/Bruno API suite and Playwright: unchanged, must stay green
- OpenAPI export regenerated and committed

## Playwright Strategy

No UI change; the suite runs as regression evidence because the frontend consumes these endpoints (folios, notebooks, protocol, budgets).

## Deployment Strategy

- Flyway migration required: no; configuration keys: none
- Smoke test after deploy (Gate 5): CD green; folio, notebook and procedure endpoints answer 200

## Rollback Strategy

- Revert the PR.
