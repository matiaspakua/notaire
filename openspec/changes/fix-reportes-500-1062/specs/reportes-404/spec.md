## ADDED Requirements

### Requirement: GET /api/v1/reportes/presupuesto/{id} returns 404 when id not found

The report endpoint SHALL answer 404 Not Found when no budget has the requested id.

#### Scenario: Unknown budget id

- **WHEN** a client requests `/api/v1/reportes/presupuesto/99999` and no budget 99999 exists
- **THEN** the response status is 404

### Requirement: GET /api/v1/reportes/presupuesto-inmuebles/{id} returns 404 when id not found

The report endpoint SHALL answer 404 Not Found when no property budget has the requested id.

#### Scenario: Unknown property budget id

- **WHEN** a client requests `/api/v1/reportes/presupuesto-inmuebles/99999` and no property budget 99999 exists
- **THEN** the response status is 404

### Requirement: GET /api/v1/reportes/historial-gestion/{id} returns 404 when id not found

The report endpoint SHALL answer 404 Not Found when no history record has the requested id.

#### Scenario: Unknown history id

- **WHEN** a client requests `/api/v1/reportes/historial-gestion/99999` and no history 99999 exists
- **THEN** the response status is 404

### Requirement: GET /api/v1/reportes/documentos-por-vencer/{id} returns 404 when id not found

The report endpoint SHALL answer 404 Not Found when no documents-by-due-date record has the requested id.

#### Scenario: Unknown documents-por-vencer id

- **WHEN** a client requests `/api/v1/reportes/documentos-por-vencer/99999` and no documentos-por-vencer 99999 exists
- **THEN** the response status is 404
