# ui-list-pagination — delta

## Purpose

Lists backed by growing tables are paged server-side with a shared footer.

## MODIFIED Requirements

### Requirement: The managements list is read one page at a time

`/dashboard/gestiones` SHALL request one page of `GET /api/v1/gestiones` (default 20, newest first), show the total, reach the last page and keep page/size in the URL; the dashboard SHALL count managements with `totalElements` and SHALL NOT load the list.

#### Scenario: Page and total

- **WHEN** the managements screen opens
- **THEN** 20 rows, newest first, and the total are shown and no size=1000 request is made

#### Scenario: Oldest management reachable

- **WHEN** the user goes to the last page
- **THEN** the oldest management is listed and the URL keeps the page

#### Scenario: Dashboard total

- **WHEN** the dashboard opens
- **THEN** the managements counter shows the real total and no size=1000 managements request is made

#### Scenario: Out-of-range page

- **WHEN** the URL names a page past the end
- **THEN** the screen moves to the last page
