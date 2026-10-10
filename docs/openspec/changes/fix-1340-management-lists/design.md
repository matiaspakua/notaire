# Design

## Context

#1340 slices 1-6 paged the audit log, people, managements, budgets and deeds lists and replaced the person and budget selects with server-search pickers. Three management consumers were parked because the dev DB had 593 managements; it now has more than 1000.

## Goals / Non-Goals

Goals: no screen loads `GET /gestiones?size=1000`; the two lists page on the server; the documentos form can pick any management. Non-goals: deed pickers and the `GET /escrituras?status=` filter (slice 8, separate PR), a backend free-text management search.

## Decisions

Reuse `useGestionesPage`, `useUrlPagination`, `useClampPage` and the `DataTable` pagination prop exactly as `/dashboard/gestiones` does, so the three lists behave the same. `ManagementPicker` mirrors `BudgetPicker` on `SearchCombobox`, built from existing endpoints (number lookup plus client search), so no API change is needed. The label keeps the old `<number> — <heading>` text so users and tests recognise it.

## Riesgos / Trade-offs

A client search loads managements for at most 5 matching clients, the same bound as the budget picker. Newest-first ordering changes the order the two lists showed (backend order before); new cases now appear on page 1.

## Testing Strategy

Vitest `management-lists-pagination.test.tsx`: both lists request `page=0&size=20&sort=idManagement,desc`, show the footer, never ask for size=1000, and clamp a page past the end; the picker opens with the newest managements, finds by number, client name and document number, and shows the current value loaded by id. Written first (module missing, red).

## Regression Strategy

`bash frontend/verify.sh`; full Playwright chromium suite.

## Playwright Strategy

TS-0018 (re-entry), TS-0033 (external-entity documents), TS-0099 (document → procedure link) and TS-0090 (two full cases) create a new case and must find it on these screens; they failed on the pre-change build with more than 1000 managements and pass after.

## Deployment Strategy

Frontend only.

## Rollback Strategy

Revert the commit.
