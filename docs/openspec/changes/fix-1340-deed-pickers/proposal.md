# Pick deeds by server search (#1340 slice 8b)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1340 |
| Use Case | CU07 – Generar testimonio; CU08 – Generar minuta de inscripción; CU87 – Vincular escritura y folio |
| Branch | `fix/1340_deed_pickers` |
| Gate 1 status | draft |

## Objetivo

The testimony, registration-draft and folio forms chose their deed from `useEscrituras()`, which read `GET /escrituras?size=1000` and kept the signed deeds in the browser. Once the deeds table passes 1000 rows a newly signed deed is not offered, and a page of mostly unsigned deeds offers almost nothing. The three forms now pick the deed with a server-search combobox that lists the newest signed deeds (`GET /escrituras?status=Firmada`, added in #1476) and finds any other signed deed by number. This is the last screen family on the #1340 plan, so the size=1000 helper and the last unused size=1000 hook go too.

## What Changes

- `DeedPicker` on the shared `SearchCombobox`, with `useDeedSearch`: an empty query lists the 20 newest signed deeds (`GET /escrituras?status=Firmada&sort=idDeed,desc`); a number searches `GET /escrituras/buscar?number=` and keeps signed deeds only; the current value loads by id (`GET /escrituras/{id}`). Label `Escritura Nº {number}` / `Deed No. {number}`, detail = recording date.
- `/dashboard/testimonios`, `/dashboard/minutas-inscripcion` and `/dashboard/administracion/folios` use it. The folio form keeps the CU87 rule through `exclude`: a deed already linked to another folio is not offered.
- `useEscrituras()`, `usePersonas()` (no callers) and `apiGetPaged` (the size=1000 helper) are removed; a guard test fails if a source asks for a whole table again.
- i18n `deedPicker.*`; unused `selectEscritura` and `escrituraOption` keys removed.
- E2E: shared `setup/deed-picker.ts` helpers; TS-0012, TS-0031, TS-0082 and folios-vinculacion pick deeds by typing the number; new TS-0123.
- CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A list or picker over a growing table never loads the whole table | `ui-list-pagination` (#1340) | Made explicit for deeds |
| Only a signed deed can get a testimony, a registration draft or a folio link | CU07, CU08, CU87 | Unchanged (now filtered on the server) |
| A deed linked to one folio is not offered for another | CU87 | Unchanged |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `ui-list-pagination`: Lists and pickers backed by growing tables never load the whole table.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | DeedPicker, useDeedSearch, three forms, api-client, usePersonas, messages |
| `testing/e2e` | yes | deed-picker helpers, four specs, TS-0123 |
| `backend-api` | no | — (`GET /escrituras?status=` merged in #1476) |

### Surface area

- Endpoints: none new (`GET /escrituras` with `status`, `/escrituras/buscar`, `/escrituras/{id}`)
- Entities / Flyway / configuration: none

### Architecture review

No architecture change: client-side data loading only.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
| `frontend/messages/{es,en}.json` | deedPicker namespace; unused keys removed |
