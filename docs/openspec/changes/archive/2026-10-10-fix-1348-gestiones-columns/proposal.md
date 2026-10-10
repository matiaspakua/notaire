# Managements list: case header, start date and a correctly titled procedure count

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1348 |
| Use Case | RF-22 – Consultar estado e historial de los trámites; RF-40 – Buscar gestiones de cliente |
| Branch | `fix/1348_gestiones_columns` |
| Gate 1 status | draft |

## Objetivo

On /dashboard/gestiones the column titled "Tipo de Trámite" rendered `procedureCount` (1 or 2 on every row), while the case header (`encabezado`) and start date (`dateStart`) that identify a case were not shown, although `GET /gestiones` returns them.

## What Changes

- Columns: Número (with the internal id as secondary text) · Carátula (`encabezado`, truncated with a `title` tooltip) · Inicio (`dateStart` via `formatCalendarDate`, #1339) · Trámites (the count, right-aligned) · Estado · actions; test ids unchanged.
- i18n `gestiones.fields.encabezado`, `inicio`, `tramites` (es/en); `fields.tipo` stays for the procedure-type select of the form.
- Playwright `TS-0116`; CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A list column shows what its title says | #1348, #1336 | Made explicit |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `ui-gestiones`: The managements list screen.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | gestiones page, i18n |
| `testing` | yes | Playwright TS-0116 |
| `backend-api` | no | DtoManagementSummary already returns encabezado and dateStart |

### Surface area

- Route: /dashboard/gestiones

### Architecture review

No architecture change.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
