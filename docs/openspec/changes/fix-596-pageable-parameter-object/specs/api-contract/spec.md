# api-contract — delta

## Purpose

The OpenAPI contract describes the request parameters the implementation actually reads.

## MODIFIED Requirements

### Requirement: Documented paging parameters

Every paged list endpoint SHALL document optional `page`, `size` and `sort` query parameters and SHALL NOT document a `pageable` object parameter.

#### Scenario: Optional paging parameters

- **WHEN** the OpenAPI document is generated
- **THEN** each paged endpoint lists `page`, `size` and `sort` as optional query parameters

#### Scenario: No pageable object

- **WHEN** the OpenAPI document is generated
- **THEN** no paged endpoint lists a `pageable` parameter
