# ui-list-pagination — delta

## Purpose

Lists backed by growing tables are paged server-side with a shared footer.

## MODIFIED Requirements

### Requirement: The people list is read one page at a time

`/dashboard/personas` SHALL request one page of `GET /api/v1/people` (default 20, newest first), show the total, reach the last page and keep page/size in the URL; a search SHALL list every match from `GET /api/v1/people/search`.

#### Scenario: Page and total

- **WHEN** the people screen opens
- **THEN** 20 rows, newest first, and the total are shown and no size=1000 request is made

#### Scenario: Oldest person reachable

- **WHEN** the user goes to the last page and reloads
- **THEN** the oldest person is listed and the URL keeps the page

#### Scenario: Duplicate link outside the page

- **WHEN** a person is created with the document of a person not on the loaded page
- **THEN** the toast offers the link and opens that person

#### Scenario: Hook and URL

- **WHEN** usePersonasPage and the page are rendered
- **THEN** one sorted page is requested with the page and size from the URL
