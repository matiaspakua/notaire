# controller-typed-returns Delta

## ADDED Requirements

### Requirement: Audit log list handler is typed

`AuditRecordController#getAll` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`. Unpaginated success stays a flat `DtoAuditRecord` array. Paginated success stays the existing page object with `content`, `page`, `size`, `totalElements`, `totalPages`, `first`, and `last`.

#### Scenario: getAll leaves the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** `AuditRecordController#getAll` is absent from the baseline and is not loosely typed

#### Scenario: Audit list keeps flat and paginated JSON shapes

- **GIVEN** a request without pagination parameters, or with `page` and `size`
- **WHEN** the controller handles it
- **THEN** the response is a flat array or a page object with the same keys as before
