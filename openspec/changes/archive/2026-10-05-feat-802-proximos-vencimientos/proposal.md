# Report the documents approaching their expiration date (CU42)

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #802 |
| Use Case | CU42 – Informar próximos vencimientos (#195) |
| Branch | `feat/802_proximos_vencimientos` |
| Gate 1 status | draft |

## Objetivo

Document types already carry vence/diasVencimiento and #837 inherits the due date into each submitted document, but nothing queries or shows the documents about to expire. Add the CU42 report: an endpoint listing submitted documents due within N days and a dashboard screen showing them.

## What Changes

- `GET /api/v1/documento-presentado/proximos-vencimientos?dias=N` (default 30, valid 1 to 365) lists the documents that expire, are not released and whose due date is between today and today plus N days, ordered by due date.
- Each row carries the CU42 data: document name, management number and heading, prepared, entry date, exit date, cartón number, observed, amount to pay, payment date, release date, notes, due date and days remaining.
- `UpcomingExpirationService` owns the rule; a thin controller exposes it; a JPQL repository query selects the window.
- New screen `/dashboard/proximos-vencimientos` with a window selector, the table, and an empty state; navigation entry and i18n (es/en).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A document is an upcoming expiration when it expires, is not released and its due date is between today and today plus the window, both inclusive | CU42 step 1 | Made explicit |
| The window must be between 1 and 365 days | #802 | New |

## Capabilities

### New Capabilities

- `upcoming-expirations`: Report the submitted documents that are about to expire.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | query, service, controller |
| `frontend` | yes | screen, hook, nav, i18n |
| `notaire-shared` | no | — |
| Docs / scripts / CI | yes | notaire-shared DTO, OpenAPI artifact, CU42 doc, E2E spec |

### Surface area

- Entities / Endpoints / Flyway / Configuration: none
- Dependencies: none

### Architecture review

Read-only report on existing columns; no schema change and no ADR. Follows the service and thin controller layering of CU43.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU42 – Informar próximos vencimientos.md` | implementation note |
| `backend-api/openapi/openapi.yaml` | regenerated |
| `CHANGELOG.md` | one entry |
