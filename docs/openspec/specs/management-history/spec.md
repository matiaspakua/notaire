# management-history Specification

## Purpose
Recording and reading the state-change history of deed managements.
## Requirements
### Requirement: ADMIN-only history writes

`PUT` and `DELETE /api/v1/history/{id}` SHALL require the administrator role; reading and creating history SHALL stay available to any authenticated user.

#### Scenario: Employee rewrites history

- **WHEN** an employee sends PUT /api/v1/history/{id}
- **THEN** the response is 403 and the row is unchanged

#### Scenario: Employee deletes history

- **WHEN** an employee sends DELETE /api/v1/history/{id}
- **THEN** the response is 403 and the row still exists

#### Scenario: Administrator corrects history

- **WHEN** an administrator updates or deletes a history row
- **THEN** the response is 200 and the change is applied

#### Scenario: Employee reads and records history

- **WHEN** an employee reads history or records a new row
- **THEN** the responses are 200 and 201

### Requirement: Paginated history list

`GET /api/v1/history` SHALL answer a Spring Data page bounded by `size` (default 20, sorted by `idHistory`) and SHALL honour `sort`.

#### Scenario: Requested page size

- **WHEN** GET /api/v1/history?size=2 with at least three history rows
- **THEN** content has two elements, size is 2 and totalElements is at least 3

#### Scenario: Default page

- **WHEN** GET /api/v1/history without parameters
- **THEN** size is 20 and number is 0

#### Scenario: Newest first

- **WHEN** GET /api/v1/history?size=1&sort=idHistory,desc
- **THEN** the only element is the most recently created history row

