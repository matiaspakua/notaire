# Testimony edit and delete from the UI, with a read-only movement history; DELETE keeps a testimony that has movements (#1364)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1364 |
| Use Case | CU07 – Generar testimonio; CU44 – Reingreso testimonio; RF-31 |
| Branch | `feat/1364_testimony_edit_delete` |
| Gate 1 status | draft |

## Objetivo

`/dashboard/testimonies` could only generate and verify. A mistaken testimony couldn't be corrected or removed, and its registry movements weren't visible anywhere. `PUT`/`DELETE /testimonies/{id}` had no caller. `DELETE` on a testimony with movements answered 204 and cascade-deleted its movement history (`CascadeType.ALL`).

## What Changes

- Frontend `/dashboard/testimonies`: row actions **Ver movimientos**, **Editar** (number, notes; flagged, verified and the version read are sent as required by #655) and **Eliminar** (confirmation). A stale version (409) and a testimony in use (409) show their own Spanish message.
- New `TestimonyMovementsDialog`: movements newest first with entry, inscription, withdrawal and card number, observed flag and notes. Data comes from `GET /testimonies` (`testimonyMovements`), so no whole-table load of movements.
- Hooks `useUpdateTestimony`, `useDeleteTestimony`; `Testimony.version` in the types. Verify/download buttons use the same `icon-sm` size as the new ones.
- Backend `TestimonyController.delete`: 409 with the reason when the testimony has registry movements or issued copies (new `existsByFkIdTestimonyIdTestimony` on both repositories). OpenAPI documents the 409.
- Allowlist: `PUT`/`DELETE /testimonies/{id}` removed (now called); `GET /testimony-movements` moves to bucket A (history read from the testimony list). Manual movement CRUD stays bucket A (Owner, 2026-10-09).
- Bruno testimonies 06a/06b/06c (movement → DELETE 409 → remove movement) and COVERAGE row. Tests: JUnit `TestimonyDeleteKeepsHistoryIntegrationTest`, SimpleControllersTest case, Vitest `testimony-admin.test.tsx`, Playwright TS-0143. CHANGELOG.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A testimony with registry movements or issued copies can't be deleted | CU44 movement history; same rule as #1361 (no cascade delete of history) | New (was a cascade delete) |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `testimony-admin`: Testimonies can be corrected and deleted from the UI, and their movement history is visible.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | delete guard, repositories, OpenAPI, Bruno |
| `frontend` | yes | page actions, dialog, hooks, i18n |
| `contracts` | yes | reachability allowlist |
| `testing` | yes | Playwright TS-0143 |

### Surface area

- Endpoints: DELETE /api/v1/testimonies/{id} answers 409 when the testimony has movements or copies (additive response); no request change
- Entities / Flyway / configuration: none

### Architecture review

The guard sits in the web adapter next to the existing 404 check, like the in-use checks of #1361; the cascade mapping is left as is so other flows don't change.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Added and Fixed entries |
| `backend-api/api-test/COVERAGE.md` | testimonies row |
| `contracts/api-reachability-allowlist.yaml` | entries removed / moved to bucket A |
