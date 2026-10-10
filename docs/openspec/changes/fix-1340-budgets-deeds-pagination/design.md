# Design

## Context

Slices 2–4 (#1392, #1394, #1396) added `useUrlPagination`, `useClampPage`, the `DataTable` `pagination` prop and the managements list. This slice is stacked on #1396 and applies the same pattern to budgets and deeds.

## Goals / Non-Goals

- Goal: both lists read one server page, show the total and reach every row; their filters ask the backend with the parameters it reads; the dashboard counts budgets with `totalElements`.
- Non-goal: the budget and deed selects on other screens (later slices); any backend change.

## Decisions

1. **Newest first** (`idBudget,desc`, `idDeed,desc`): a row just created is on page 1.
2. **Filters leave paging.** The status list and the number search come from their own endpoints (unpaged) and hide the footer; clearing them returns to the URL page.
3. **Budget number via `GET /presupuestos/{id}`.** The old client-side search only looked in the loaded rows; a number is now found on any page. A client name still filters the rows shown, since the backend has no such search.
4. **Fix the parameter names in the frontend** (`status`, `number`): the backend contract is right; the UI sent names it ignores.

## Riesgos / Trade-offs

- A client-name search now covers the current page (or the status list), not the first 1000 rows; reported as a backend gap.

## Testing Strategy

Vitest `budgets-deeds-pagination.test.tsx`: hook URLs, page render with the footer and no size=1000, number lookup, clamping, deed search parameter.

## Regression Strategy

`bash frontend/verify.sh`; Playwright chromium suite against the branch production build.

## Playwright Strategy

`TS-0113`: budgets and deeds pages of 20 newest first with the total and the oldest row on the last page; dashboard budgets total; status filter sends `status`; deed number search sends `number`.

## Deployment Strategy

Frontend only; ships with the next frontend deploy.

## Rollback Strategy

Revert the PR; the backend contract is unchanged.
