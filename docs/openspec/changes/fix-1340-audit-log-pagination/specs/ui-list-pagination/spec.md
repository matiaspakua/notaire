# ui-list-pagination — delta

## Purpose

Lists backed by growing tables are paged server-side with a shared footer.

## MODIFIED Requirements

### Requirement: The audit log is read one page at a time

`/dashboard/auditoria` SHALL request one page of `GET /api/v1/audit-log` (default 20, newest first), show the total, reach the last page, keep page/size/module in the URL and apply the module filter on the server.

#### Scenario: Page and total

- **WHEN** the audit screen opens
- **THEN** 20 rows and the total are shown and no size=1000 request is made

#### Scenario: Oldest record reachable

- **WHEN** the user goes to the last page
- **THEN** the oldest audit record is listed and the URL has the page

#### Scenario: Server-side module filter

- **WHEN** a module is chosen
- **THEN** the URL has the module, the total is that module's and every row belongs to it

#### Scenario: Footer and helper

- **WHEN** Pagination, DataTable and apiGetPage are exercised
- **THEN** range, buttons, sizes, aria-busy and query building behave as specified
