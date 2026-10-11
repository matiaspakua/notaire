# Design

## Context

#1340 slices 1-7 paged every list and replaced the person, budget and management selects with server-search pickers. Slice 8a (#1476) added `GET /escrituras?status=` so a deed picker can list signed deeds without filtering in the browser. Three forms still read `GET /escrituras?size=1000`.

## Goals / Non-Goals

Goals: no screen loads the deeds table whole; testimonies, registration drafts and folios can pick any signed deed; no size=1000 helper left in the frontend. Non-goals: the folio list itself (`GET /folio` is not a paged endpoint), a free-text deed search on the backend.

## Decisions

`DeedPicker` mirrors `BudgetPicker` and `ManagementPicker` on `SearchCombobox`, so keyboard, announcements and look are the same. Deeds have no client or heading to search by, so text that is not a number matches nothing; a number uses the existing `GET /escrituras/buscar`. The signed-only rule stays in the picker (server `status=Firmada` for the recent list, a filter on the search result) because every caller needs it. The folio exclusion is a caller rule, so it is an `exclude` prop and the folio page keeps computing it from its folio list. Removing `apiGetPaged` makes a new whole-table load a visible change rather than a one-line call.

## Riesgos / Trade-offs

The recent list shows the newest 20 signed deeds by id; older ones need their number, which users have on paper. The folio exclusion filters the 20 recent or the searched deeds, so the recent list can show fewer than 20 options.

## Testing Strategy

Vitest `deed-picker.test.tsx`: opens with the newest signed deeds (`status=Firmada`), never size=1000; finds an old deed by number and offers only signed ones; `exclude` hides a linked deed; the current value loads by id; no-results announcement; no screen calls `useEscrituras()`; the three forms use `<DeedPicker`. `no-whole-table-loads.test.ts`: no source defines or calls `apiGetPaged` or asks for size=1000. Both written first and red.

## Regression Strategy

`bash frontend/verify.sh`; full Playwright chromium suite.

## Playwright Strategy

TS-0123 (testimony form finds a signed deed and offers no unsigned one; registration-draft form; folio form hides a linked deed) failed 3/3 on the pre-change build. TS-0012, TS-0031, TS-0082, TS-0090 and folios-vinculacion pick deeds through the picker.

## Deployment Strategy

Frontend only.

## Rollback Strategy

Revert the commits.
