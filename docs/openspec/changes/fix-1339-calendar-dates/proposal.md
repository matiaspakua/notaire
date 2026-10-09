# Calendar dates show and round-trip the stored day in any browser zone (#1339, #1338)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1339, #1338 |
| Use Case | RNF-08 – Especificación de campos; RF-06 – Modificar presupuestos; RF-16/RF-17 – Seguimiento de documentación; RF-29 – Modificar escritura; RF-31 – Testimonios |
| Branch | `fix/1339_calendar_dates` |
| Gate 1 status | draft |

## Objetivo

Date-only fields are `@Temporal(DATE)` columns that the backend serializes as midnight in its JVM zone (2026-09-05 leaves a Madrid server as `2026-09-04T22:00:00.000Z`). The UI read that instant in the browser zone, so every date showed one day early in Argentina. Edit dialogs bound the instant to `<input type="date">` (empty field, #1338) or split it on `T` (previous day, written back on save). Today defaults used the UTC day. One helper module now recovers the calendar day independently of server and browser zone.

## What Changes

- New `frontend/src/lib/dates.ts`: `BUSINESS_TIME_ZONE` (`America/Argentina/Buenos_Aires`, default decision on #1339), `parseCalendarDate`, `toDateInputValue`, `formatCalendarDate`, `formatInstant`, `todayInputValue`.
- `formatDate` (`lib/utils.ts`) keeps its signature and uses `formatCalendarDate`.
- Edit dialogs bind `toDateInputValue(...)`: presupuestos, pagos, escrituras, suplencias, documentos-entidades-externas (4 dates), documentos, copias. `split("T")` and `toISOString().split` are gone, and today defaults use `todayInputValue()`.
- Audit-log timestamps use `formatInstant` in the business zone; management history (a TIMESTAMP column holding the day's midnight), copias and testimony movements use `formatCalendarDate`.
- Vitest `dates.test.ts` (with a source guard); Playwright `TS-0102`, `TS-0103`; CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A calendar date shows the stored day in any browser zone | #1339 | Made explicit |
| Business time zone is America/Argentina/Buenos_Aires | #1339 default decision (Owner did not answer, 2026-10-09) | New |
| Edit dialogs pre-fill the stored date; saving unchanged keeps it | #1338 | Made explicit |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `ui-calendar-dates`: Date-only fields are shown, edited and defaulted as calendar days; timestamps are shown in the business time zone.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | lib/dates.ts, utils.formatDate, 11 pages/components |
| `testing` | yes | Playwright TS-0102, TS-0103 |
| `backend-api` | no | LocalDate migration is a separate backend follow-up |

### Surface area

- Routes: pagos, presupuestos, escrituras, suplencias, documentos, documentos-entidades-externas, copias, auditoria, gestiones, dashboard workflow tracker
- API / entities / configuration: none (requests still send yyyy-MM-dd or the unchanged instant)

### Architecture review

No architecture change: one pure helper module in `lib/`, no new dependency (Intl only).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
