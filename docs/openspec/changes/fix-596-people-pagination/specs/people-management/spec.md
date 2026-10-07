# people-management — delta

## Purpose

Listing and maintaining people (clients, notaries).

## MODIFIED Requirements

### Requirement: Paginated people list

`GET /api/v1/people` SHALL answer a Spring Data page bounded by `size` (default 20).

#### Scenario: Requested page size

- **WHEN** GET /api/v1/people?size=1 with at least two people
- **THEN** content has one element, size is 1 and totalElements is at least 2

#### Scenario: Default page

- **WHEN** GET /api/v1/people without parameters
- **THEN** size is 20 and number is 0

#### Scenario: People screen lists people

- **WHEN** the personas screen loads
- **THEN** usePersonas unwraps the page content
