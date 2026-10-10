# Searchable person picker instead of the first 1000 people (slice 3 of #1340)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1340 |
| Use Case | CU01 – Preparar presupuesto; CU02 – Iniciar gestión; CU19 – Consultar gestiones por cliente; CU39 – Cargar ítems desde la plantilla |
| Branch | `fix/1340_person_picker` |
| Gate 1 status | draft |

## Objetivo

The budget client, the management notary and the management client filter were Radix selects fed by `usePersonas()`, i.e. `GET /people?size=1000`. On the dev DB (more than 1000 people) anyone created after the 1000th could not be chosen, so creating a budget or a management for a new client failed (Playwright presupuesto-plantilla, TS-0071, TS-0090 and TS-0092 failed on it), and the dashboard showed 1000 as the people count.

## What Changes

- New `components/shared/PersonPicker.tsx`: ARIA 1.2 combobox (input `role=combobox` with `aria-expanded`, `aria-controls`, `aria-activedescendant`, `aria-autocomplete=list`) and a listbox popup; opens on click, typing or ArrowDown (not on focus), ArrowUp/Down/Home/End/Enter/Tab, Escape closes the popup without closing the surrounding dialog; a polite status line (searching, results, no results, error); optional clear button; `clientsOnly`; respects reduced motion.
- New `hooks/usePersonSearch.ts`: `buildPersonSearchQueries` turns one free-text query into `GET /people/search` calls (a number searches the document; one word is tried as first and last name; several words as first + rest and as a compound last name) and merges the results; `useRecentPersonas` lists the newest 20 for an empty query; `usePersona(id)` loads the selected person by id for edit forms.
- New `hooks/useDebouncedValue.ts` (250 ms). While typing, only the settled results of the typed text are listed, so Enter cannot pick a stale option.
- `usePersonasPage` takes `{ enabled }`.
- presupuestos (client), gestiones (notary and client filter) use the picker; the dashboard people count reads `totalElements` of a 1-row page.
- i18n `personPicker.*` in es and en.
- Vitest `person-picker.test.tsx`; Playwright `TS-0111`; TS-0071/TS-0090/TS-0092/presupuesto-plantilla/TS-0011 type the name into the picker; CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Every person can be chosen in a form, however many people exist | #1340, CU01, CU02 | Made explicit |
| An edit form shows its current person | #1340, CU01 | Made explicit |
| A combobox popup inside a dialog closes on Escape without closing the dialog | #1336, WCAG 2.1.1 | New |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `ui-list-pagination`: Lists and pickers backed by growing tables never load the whole table.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | PersonPicker, usePersonSearch, useDebouncedValue, presupuestos, gestiones, dashboard |
| `testing` | yes | Playwright TS-0111 and picker steps in five specs |
| `backend-api` | no | GET /people/search, GET /people/{id} and the paged GET /people already exist |

### Surface area

- Routes: /dashboard/presupuestos, /dashboard/gestiones, /dashboard
- API: GET /api/v1/people/search, GET /api/v1/people/{id}, GET /api/v1/people?page=0&size=20&sort=idPerson,desc (unchanged contract)

### Architecture review

No architecture change. `usePersonas()` stays exported for the unit tests but no page or component calls it (a unit test enforces that).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
