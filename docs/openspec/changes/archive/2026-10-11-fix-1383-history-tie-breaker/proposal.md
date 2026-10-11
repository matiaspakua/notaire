# One total order for a management's history (#1383)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1383 |
| Use Case | CU13 – Ver bitácora de una gestión |
| Branch | `fix/1383_history_tie_breaker` |
| Gate 1 status | draft |

## Objetivo

`history.event_date` is a SQL `DATE`, so every entry recorded on the same day has the same instant. `GET /managements/{id}/history` sorted by date only (same-day order = whatever the database returned), `GET /history/management/{id}` did not sort at all, and the workflow trace picked the "latest" status by date only, while the current-state lookup and the history report already broke ties by `idHistory`. The bitácora's newest entry could therefore be wrong and change between calls; this is the cause of the recurring TS-0011 / TS-0028 e2e flakes.

## What Changes

- New `business/HistoryOrder.CHRONOLOGICAL` comparator: event date ascending, then `idHistory` ascending (nulls last).
- Used by every history read path: `ManagementBitacoraService.getHistory` (`GET /managements/{id}/history`), `HistoryController.getByManagement` (`GET /history/management/{id}`, was unsorted), `WorkflowTraceService` (trace `history` list and `computeNodeStatuses`), `ManagementController` current state, `ReportDocumentFactory.managementHistory`.
- Bruno `history/05-get-by-management.yml` asserts the order of both endpoints.
- CHANGELOG.

Out of scope: migrating `event_date` to `timestamp` (stores the time of day; needs an Owner decision, see #1383).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A management's history is ordered by event date, then insertion order (`idHistory`) | #1383, CU13 | Made explicit (was partial / undefined) |
| All history read paths return the same order | #1383 | New |

## Capabilities

### New Capabilities

- `history-order`: Deterministic chronological order of a management's history.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | comparator + five read paths, tests, Bruno |
| `frontend` | no | renders the API order unchanged |

### Surface area

- Endpoints: `GET /api/v1/managements/{id}/history`, `GET /api/v1/history/management/{id}`, `GET /api/v1/managements/{id}/workflow-trace`, `GET /api/v1/managements/{id}/current-status`, the history report PDF. Response schemas unchanged; only the order of list items is now defined. No migration.

### Architecture review

No architecture change; ordering stays in the application/adapter layer, independent of the database's row order.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
| `backend-api/api-test/COVERAGE.md` | none (same requests, stronger assertions) |
