# copy-list-page Specification

## Purpose
The copy register is a growing table. The list endpoint returns one page.

## Requirements

### Requirement: Copies list is one page

`GET /api/v1/copies` SHALL return one page of copies. The default page is 0, the default size is 20, and the default sort is `idCopy,desc`. The body SHALL include `content` and `totalElements`. `page`, `size` and `sort` SHALL be documented query parameters.

#### Scenario: Default page

- **WHEN** a client requests `GET /api/v1/copies` with no query
- **THEN** the response is 200 and `content` holds at most 20 copies, newest id first, with `totalElements` counting every copy

#### Scenario: The list screen asks for that page

- **WHEN** the copies screen is opened
- **THEN** it requests `page`, `size` and `sort=idCopy,desc` and shows the pager
