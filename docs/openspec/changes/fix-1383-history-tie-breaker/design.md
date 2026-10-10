# Design

## Context

`History.date` is mapped as a timestamp but stored in a `DATE` column, so writes lose the time of day. Five read paths each ordered the same rows differently (date only, unsorted, date + id).

## Goals / Non-Goals

Goals: one total, deterministic order on every read path. Non-goals: keeping the time of day (`event_date` → `timestamp` migration), pagination of the per-management history.

## Decisions

- Sort in Java with one shared comparator rather than a derived `...OrderByDateAscIdHistoryAsc` query: it covers the paths that receive an already loaded list (`computeNodeStatuses`, the report), keeps the unit tests independent of the database, and the lists are small (one management's history).
- `idHistory` is an identity column, so ascending id equals insertion order.
- Nulls last on both keys, as the report already did, so a legacy row without a date cannot throw.

## Riesgos / Trade-offs

A client that relied on the unsorted `GET /historial/gestion/{id}` order (insertion order in practice) sees date order now; for rows written by the app the two coincide, because each write uses the current day.

## Testing Strategy

JUnit unit: `ManagementBitacoraServiceTest` (same-day entries in reverse id order come back by id), `WorkflowTraceServiceTest` (same-day tie: the higher id is the in-progress status). JUnit H2 integration `HistoryOrderTieBreakerIntegrationTest` (both endpoints: date ascending, same-day in insertion order). All written first (red commit: 3 failing).

## Regression Strategy

`bash backend-api/verify.sh`, full Bruno collection on the branch jar.

## Playwright Strategy

No new spec: TS-0011 and TS-0028 already assert the bitácora order and were the flaky symptom; they are run with `--repeat-each` against the branch backend.

## Deployment Strategy

Backend only; no migration.

## Rollback Strategy

Revert the merge commit.
