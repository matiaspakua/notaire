# One status badge for every status column (#1366)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1366 |
| Use Case | RF-23 – Saber el estado de un trámite; RNF-09 – Interfaz consistente |
| Branch | `feat/1366_status_badge` |
| Gate 1 status | draft |

## Objetivo

Status is the column a notary office scans most, but each list rendered it differently: budgets, cases and testimonies as plain text, folios as the raw stored value in a grey badge, protocol and users as ad-hoc green/grey badges where color was the only difference. One `StatusBadge` now maps a domain and a stored value to a tone, a decorative icon and a translated label, so every status column looks and reads the same, and no status relies on color alone (WCAG 1.4.1).

## What Changes

- `frontend/src/lib/status-maps.ts`: per-domain vocabularies (`budget`, `management`, `testimony`, `folio`, `availability`, `user`) → `{ tone, labelKey, raw }`. Budget reuses `lib/budget-status.ts` (#1346) and its `presupuestos.status.*` labels. Management statuses are an admin catalog, so the label stays the stored name and only the 13 seeded names get a tone. Testimony state is derived by `testimonyStatus()` (generated / verified / flagged). Unknown values are neutral with the raw text.
- `frontend/src/components/shared/StatusBadge.tsx`: `Badge` variant per tone, lucide icon (`aria-hidden`), text label; a dash for an empty value; `data-testid="status-badge"`, `data-tone`, `data-domain`.
- Wired on `/dashboard/presupuestos`, `/dashboard/gestiones`, `/dashboard/testimonios`, `/dashboard/protocolo`, `/dashboard/administracion/usuarios` and `/dashboard/administracion/folios`; the page-local `statusLabel`/`estadoLabel` helpers and ad-hoc badges go.
- i18n `status.folio.{nuevo,utilizado,asignado,errose}` (es/en).
- Tests: Vitest `status-badge.test.tsx`; Playwright `TS-0124`.
- CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A status is shown as text plus icon, never by color alone | RNF-09, WCAG 1.4.1 | Made explicit |
| A value outside a domain's vocabulary is shown as stored, never blank | #1366 | New |
| Management status names are catalog data and are shown as stored | estados-gestion (admin catalog) | Made explicit |

## Capabilities

### New Capabilities

- `ui-status-badge`: Status columns render one shared, accessible status badge.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | StatusBadge, status-maps, six list pages, messages |
| `testing/e2e` | yes | TS-0124 |
| `backend-api` | no | — |

### Surface area

- Endpoints: none
- Entities / Flyway / configuration: none

### Architecture review

No architecture change: presentation only.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Added entry |
| `frontend/messages/{es,en}.json` | `status.folio` namespace |
