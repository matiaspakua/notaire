# Managements: current-status section in the case summary (#1553)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1553 |
| Use Case | CU14 – Consultar estado de gestión |
| Branch | `feat/1553_management_current_status` |
| Gate 1 status | draft |

## Objetivo

CU14 consults a management's current status. `GET /managements/{id}/current-status` existed (#1250 bucket C) with no UI caller. Owner answered Q1: yes.

## What Changes

- `useManagementCurrentStatus(id)` (key under `managementsKeys.all`, so CU83 transitions refresh it; no retry on 404).
- `ManagementCurrentStatus`: status, date and notes; 'Sin estado registrado' on 404; rendered as the first section of the case summary dialog.
- Allowlist: `GET /api/v1/managements/{id}/current-status` removed. i18n `gestiones.estadoActual.*` and `gestiones.resumen.estadoActual` (es/en).
- Tests: Vitest `management-current-status.test.tsx` (5); Playwright TS-0144 (desktop + 390px). CHANGELOG.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The current status is the latest History row, else the management's status; none → 404 | CU14 | Existing (backend), now surfaced |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `management-current-status`: Showing a management's current status in the UI.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | hook, component, summary dialog, i18n |
| `testing/e2e` | yes | TS-0144 |
| `contracts` | yes | allowlist |

### Surface area

- Endpoints / entities / Flyway / configuration: none (reuses `GET /managements/{id}/current-status`)

### Architecture review

Frontend only; the section is its own component so the dialog stays declarative.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Added entry |
| `contracts/api-reachability-allowlist.yaml` | bucket C entry removed |
