# management-history — delta

## Purpose

Recording and reading the state-change history of deed managements.

## MODIFIED Requirements

### Requirement: Paginated history list

`GET /api/v1/historial` SHALL answer a Spring Data page bounded by `size` (default 20, sorted by `idHistory`) and SHALL honour `sort`.

#### Scenario: Requested page size

- **WHEN** GET /api/v1/historial?size=2 with at least three history rows
- **THEN** content has two elements, size is 2 and totalElements is at least 3

#### Scenario: Default page

- **WHEN** GET /api/v1/historial without parameters
- **THEN** size is 20 and number is 0

#### Scenario: Newest first

- **WHEN** GET /api/v1/historial?size=1&sort=idHistory,desc
- **THEN** the only element is the most recently created history row
