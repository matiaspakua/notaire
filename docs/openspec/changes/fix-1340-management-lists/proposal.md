# Page the management lists and pick managements by search (#1340 slice 7)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1340 |
| Use Case | CU10 – Registrar movimientos documentación de entidades externas; CU43 – Reingresar documentación; CU04 – Registrar documentación cliente |
| Branch | `fix/1340_management_lists` |
| Gate 1 status | draft |

## Objetivo

Three screens still read the managements table with `GET /gestiones?size=1000` through `useGestiones()`: the external-entity documents list, the documentation re-entry list and the management select of the presented-documents form. The dev DB now has more than 1000 managements, so every case after the 1000th was missing from them (local e2e TS-0018, TS-0033, TS-0090 and TS-0099 failed for that reason). The two lists now read one server page at a time and the form picks the management with a server-search combobox.

## What Changes

- `/dashboard/documentos-entidades-externas` and `/dashboard/reingreso-documentacion`: one server page of `GET /gestiones` (newest first, `page`/`size` in the URL, shared pagination footer, a page past the end moves to the last page), like `/dashboard/gestiones` (#1396).
- `/dashboard/documentos` form: the management `Select` becomes `ManagementPicker` (shared `SearchCombobox`): newest 20 on open; a number searches `GET /gestiones/numero/{n}` and also clients by document number; text searches clients (`GET /people/search`) and lists their managements (`GET /gestiones/cliente/{id}`, at most 5 clients); the current value is loaded by id (`GET /gestiones/{id}`). The option label keeps `<number> — <heading>`.
- `useGestiones()` (the size=1000 hook) is removed; it has no callers left.
- i18n `managementPicker.*` in es/en. E2E helpers in TS-0090 and TS-0099 type the case number into the picker.
- CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A list or picker over a growing table never loads the whole table | `ui-list-pagination` (#1340) | Made explicit for managements |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `ui-list-pagination`: Lists and pickers backed by growing tables never load the whole table.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | two list pages, documentos form, ManagementPicker, useManagementSearch, messages |
| `testing/e2e` | yes | TS-0090, TS-0099 picker helpers |
| `backend-api` | no | — (all endpoints already exist and are paged) |

### Surface area

- Endpoints: none new (`GET /gestiones` paged, `/gestiones/{id}`, `/gestiones/numero/{n}`, `/gestiones/cliente/{id}`, `/people/search`)
- Entities / Flyway / configuration: none

### Architecture review

No architecture change: client-side data loading only.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
| `frontend/messages/{es,en}.json` | managementPicker namespace |
