# Dates, numbers and currency follow the active locale (#1355)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1355 |
| Use Case | RNF-05 (i18n), ADR-015 |
| Branch | `feat/1355_locale_formatting` |
| Gate 1 status | draft |

## Objetivo

`formatDate`, `formatCurrency`, `formatCalendarDate` and `formatInstant` format in `es-AR` whatever the UI language, so the English UI showed `8/7/2026` and `$ 75.000,00`. The dashboard and `StatValue` each built their own `locale === "en" ? "en-US" : "es-AR"` switch. Every screen now formats through one locale-aware hook. Owner decision (epic #1336): English keeps the day first, and money stays ARS.

## What Changes

- `lib/format.ts`: one map from the app locale to the Intl locale (`es` to `es-AR`, `en` to `en-GB`), plus `formatMoney` (ARS) and `formatNumber`.
- `hooks/useFormat.ts`: `date` (calendar date, #1339), `instant`, `currency`, `number` and `longDate` for the active locale (next-intl `useLocale`).
- All 49 screen call sites (17 files, including the #1491 deed picker), the dashboard date and `StatValue` use the hook. `formatDate`/`formatCurrency` stay in `lib/utils.ts` as es-AR wrappers for non-React code.
- Guard tests: no screen names `es-AR`, `en-US` or `en-GB`, and none calls the es-AR helpers.
- TS-0040: pagos shows `08/07/2026` and `ARS 75,123.xx` in en, `8/7/2026` and `$ 75.123,xx` in es.
- CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Dates, numbers and currency follow the UI language; dates stay day first; money is ARS | #1355, epic #1336 Owner decision | New |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `frontend-i18n-page-coverage`: screens are fully localised, now including formatted values.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | lib/format, useFormat, 16 screens, dashboard, StatValue |
| `testing/e2e` | yes | TS-0040 |
| `backend-api` | no | — |

### Surface area

- Endpoints, entities, Flyway, configuration: none

### Architecture review

No architecture change: display formatting only.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Changed entry |
