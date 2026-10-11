# reportes-404 Specification

## Purpose

Defines that report-generation endpoints under `/api/v1/reports/*/{id}` return
HTTP 404 when the requested id does not exist, instead of an unexpected 5xx.

## Requirements

### Requirement: GET /api/v1/reports/budget/{id} returns 404 when id not found

The report endpoint SHALL answer 404 Not Found when no budget has the requested id.

#### Scenario: Unknown budget id

- **WHEN** a client requests `/api/v1/reports/budget/99999` and no budget 99999 exists
- **THEN** the response status is 404

### Requirement: GET /api/v1/reports/budget-properties/{id} returns 404 when id not found

The report endpoint SHALL answer 404 Not Found when no property budget has the requested id.

#### Scenario: Unknown property budget id

- **WHEN** a client requests `/api/v1/reports/budget-properties/99999` and no property budget 99999 exists
- **THEN** the response status is 404

### Requirement: GET /api/v1/reports/management-history/{id} returns 404 when id not found

The report endpoint SHALL answer 404 Not Found when no history record has the requested id.

#### Scenario: Unknown history id

- **WHEN** a client requests `/api/v1/reports/management-history/99999` and no history 99999 exists
- **THEN** the response status is 404

### Requirement: GET /api/v1/reports/documents-due/{id} returns 404 when id not found

The report endpoint SHALL answer 404 Not Found when no documents-by-due-date record has the requested id.

#### Scenario: Unknown documents-due id

- **WHEN** a client requests `/api/v1/reports/documents-due/99999` and no documents-due 99999 exists
- **THEN** the response status is 404
