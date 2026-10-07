> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #1198, CU13. Evidence: `getStatusActual` streams `findByFkIdManagementIdManagement` (no ORDER BY)
and applies `max(Comparator.comparing(History::getDate))`; `History.date` maps `event_date TIMESTAMP`.
Two writes in one millisecond tie and `Stream.max` returns the first of equal elements, whose
order is unspecified. The failure was `expected:<21> but was:<20>` once in a full run, 0 of 3 in isolation.

## Goals / Non-Goals

**Goals:** deterministic selection; a regression test that fails first.
**Non-Goals:** schema changes, new repository queries, moving logic to a service.

## Decisions

1. **`thenComparing(History::getIdHistory)`** — ids are generated increasing, so the higher id is the
   later insert. Smallest change; no query or schema impact.
   - Rejected: `ORDER BY event_date DESC, id DESC` in the repository — a wider change for the same result.
2. **Test forces the tie** by setting both rows to the same `Date` through the repository, so the failure is
   deterministic instead of probabilistic.

## Riesgos / Trade-offs

- [Ids not monotonic with insert order] → they come from a sequence; documented assumption.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Identical dates return the later insert | integration (H2) | `ManagementHistorialOrphanWriteIntegrationTest` |
| Distinct dates still return the latest date | integration (H2) | same (existing test, made deterministic) |

- New unit tests (`src/test/java/.../unit/`): n/a
- New integration tests: one
- Coverage impact (JaCoCo): none (adds a line)

## Regression Strategy

- Existing tests affected: the #806 test keeps its assertions.
- Full suite command: `mvn verify -pl backend-api`, then `bash scripts/run_pipeline.sh`.
- HTTP/Bruno API suite: unchanged contract; run by the pipeline.
- Legacy paths at risk: none.

## Playwright Strategy

n/a — no UI change.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: single PR.
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): `estado-actual` after create then update returns the new status.

## Rollback Strategy

- Revert the PR; no data or schema involved.
