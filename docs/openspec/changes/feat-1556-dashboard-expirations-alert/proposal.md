# Dashboard: upcoming-expirations alert card (#1556)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1556 |
| Use Case | CU42 – Alertar vencimientos |
| Branch | `feat/1556_expirations_alert` |
| Gate 1 status | draft |

## Objetivo

CU42 asks the system to alert about documents that expire soon. The alert only existed as a separate page (`/dashboard/upcoming-expirations`); the home dashboard said nothing. Owner answered Q4: yes.

## What Changes

- `components/dashboard/ExpirationsAlertCard.tsx`: count, the 3 nearest (document · management · due date) and a 'Ver todos' link; renders nothing when empty or on error.
- `UPCOMING_EXPIRATIONS_DEFAULT_DAYS` (30) shared by the page and the card; the card reuses `useUpcomingExpirations`, so React Query shares the cache with the page.
- Dashboard home renders the card above the stats. i18n `dashboard.vencimientos.*` (es/en, ICU plural).
- TS-0020 CU42-GW01 implemented at desktop and 390px; feature-gap skips 11 → 10 (test, E2E-TEST-MAPPING, living spec).
- Tests: Vitest `dashboard-expirations-alert.test.tsx` (4). CHANGELOG.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Alert window equals the upcoming-expirations page default (30 days) | CU42 | New |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `dashboard-expirations-alert`: Alerting about upcoming document expirations on the home dashboard.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | dashboard page, card, hook constant, i18n |
| `testing/e2e` | yes | TS-0020 |
| `testing/tests` | yes | skip inventory |

### Surface area

- Endpoints / entities / Flyway / configuration: none (reuses `GET /submitted-documents/upcoming-expirations`)

### Architecture review

Frontend only; a self-contained card component keeps the dashboard page thin.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Added entry |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | CU42 skip resolved |
| `docs/openspec/specs/e2e-feature-gap-skip-tracking/spec.md` | count 10 |
