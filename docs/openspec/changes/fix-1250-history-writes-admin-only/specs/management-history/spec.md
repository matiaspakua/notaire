# management-history — delta

## Purpose

Recording and reading the state-change history of deed managements.

## MODIFIED Requirements

### Requirement: ADMIN-only history writes

`PUT` and `DELETE /api/v1/historial/{id}` SHALL require the administrator role; reading and creating history SHALL stay available to any authenticated user.

#### Scenario: Employee rewrites history

- **WHEN** an employee sends PUT /api/v1/historial/{id}
- **THEN** the response is 403 and the row is unchanged

#### Scenario: Employee deletes history

- **WHEN** an employee sends DELETE /api/v1/historial/{id}
- **THEN** the response is 403 and the row still exists

#### Scenario: Administrator corrects history

- **WHEN** an administrator updates or deletes a history row
- **THEN** the response is 200 and the change is applied

#### Scenario: Employee reads and records history

- **WHEN** an employee reads history or records a new row
- **THEN** the responses are 200 and 201
