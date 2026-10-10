# ui-list-pagination — delta

## Purpose

Lists backed by growing tables are paged server-side with a shared footer.

## MODIFIED Requirements

### Requirement: The budgets and deeds lists are read one page at a time

`/dashboard/presupuestos` and `/dashboard/escrituras` SHALL request one page of `GET /api/v1/presupuestos` and `GET /api/v1/escrituras` (default 20, newest first), show the total, reach the last page and keep page/size in the URL; the dashboard SHALL count budgets with `totalElements`. The budget status filter SHALL send `status` and the deed search SHALL send `number`.

#### Scenario: Page and total

- **WHEN** the budgets or deeds screen opens
- **THEN** 20 rows, newest first, and the total are shown and no size=1000 request is made

#### Scenario: Oldest row reachable

- **WHEN** the user goes to the last page
- **THEN** the oldest budget or deed is listed and the URL keeps the page

#### Scenario: Dashboard total

- **WHEN** the dashboard opens
- **THEN** the budgets counter shows the real total

#### Scenario: Filters reach the backend

- **WHEN** the user filters budgets by status or searches a deed number
- **THEN** the request carries `status` or `number` and only matching rows are listed

#### Scenario: Budget number on any page

- **WHEN** the user types a budget number
- **THEN** that budget is listed even if it is not on the current page
