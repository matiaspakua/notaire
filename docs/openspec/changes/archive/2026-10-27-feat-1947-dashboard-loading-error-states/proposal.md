# Dashboard hygiene + shared loading/error states

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1947 |
| Use Case | RNF-09 – Uso de colores en la GUI |
| Branch | `feat/1947_dashboard-loading-error-states` |
| Gate 1 status | passed |

## Objetivo

List pages treat query failures like empty data, and loading/error chrome is ad-hoc.
Wave 3 of epic #1941 adds shared `LoadingState` / `ErrorState` and pilots them on
dashboard home, people, and managements so empty / loading / error read as one system
with #1942 page chrome.

## What Changes

- Add `LoadingState` and `ErrorState` under `frontend/src/components/shared/`.
- Extend `DataTable` with optional `error` + `onRetry` (renders ErrorState).
- Wire people and managements list queries to surface `loadError` + retry.
- Dashboard home shows ErrorState when a home stat query fails (with retry).
- Document in `FRONTEND-DESIGN-SYSTEM.md`; CHANGELOG; Vitest; OpenSpec archive.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Failed loads explain the failure and offer retry when possible | RNF-09 / UX epic #1941 | Made explicit |
| Loading placeholders must not look like zero data | #1358 / #1359 | Unchanged for StatValue; LoadingState for panels |

## Capabilities

### New Capabilities

- `ui-async-states`: LoadingState and ErrorState behaviour for async surfaces.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | – |
| `frontend` | yes | primitives, DataTable, dashboard/people/managements, i18n |
| `frontend-swing` | no | – |
| `infra` / observability | no | – |
| CI/CD | no | – |

### Surface area

- Entities / endpoints / Flyway / env: none
- BREAKING: no

### Architecture review

Follows ADR-011. No new ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/203-design/FRONTEND-DESIGN-SYSTEM.md` | Document LoadingState / ErrorState |
| `CHANGELOG.md` | Feature entry |

## Out of Scope

- Migrating every list page (only people + managements pilots)
- Workflow diagram polish (wave 4)
- axe E2E (#1251)
