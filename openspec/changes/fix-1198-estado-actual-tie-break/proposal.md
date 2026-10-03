# Deterministic current status when history dates tie

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1198 |
| Use Case | CU13 – gestion status and history (also CU14) |
| Branch | `fix/1198_estado_actual_tie_break` |
| Gate 1 status | draft — bug fix with minimum viable specification |

## Objetivo

`GET /api/v1/gestiones/{id}/estado-actual` must return the most recent history row even when two
rows share the same timestamp. Today the tie is resolved by an unordered query, so the stale
status can be returned, and a test added with #806 fails intermittently in the full suite.

## What Changes

- In `ManagementController.getStatusActual`, compare history rows by date and then by id.
- Add a regression test with two rows of identical date, failing before the fix.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The current status of a gestion is its latest history row; ties on date are broken by the higher id | #1198; endpoint description "latest History row by date" | Made explicit |

## Capabilities

### New Capabilities

- `estado-actual-tie-break`: deterministic latest-row selection for the current status endpoint.

### Modified Capabilities

- (none under `openspec/specs/`)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | One comparator in `ManagementController`; one test |
| `frontend` | no | — |
| `frontend-swing` | no | Removed module |
| `notaire-shared` | no | — |

### Surface area

- Entities / Flyway / `.env` / dependencies: none
- Endpoints: `GET /api/v1/gestiones/{id}/estado-actual` (behaviour only; contract unchanged)

### Architecture review

Behaviour fix inside an existing controller; no ADR. Moving the selection into a service is a
separate refactor.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | `[Unreleased]` Fixed entry |
| `docs/100-business/102-use-cases/CU13*.md` | Add #1198 to the GitHub ID table if one exists there |

## Out of Scope

- A history schema change or an ordered repository query.
- Other flaky tests (none identified).
