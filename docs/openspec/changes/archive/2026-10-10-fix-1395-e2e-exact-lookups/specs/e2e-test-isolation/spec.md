# e2e-test-isolation — delta

## Purpose

E2E specs find their own records independently of other data in the database.

## MODIFIED Requirements

### Requirement: E2E lookups are exact

Playwright specs SHALL find table rows by a cell whose full text equals the value and SHALL select catalog options by the exact name they created.

#### Scenario: Row by exact cell

- **WHEN** another row contains the same digits
- **THEN** only the test's own row is found

#### Scenario: Own procedure type

- **WHEN** parallel tests create other "Tipo Tramite E2E" types
- **THEN** the test selects its own type and loads its template
