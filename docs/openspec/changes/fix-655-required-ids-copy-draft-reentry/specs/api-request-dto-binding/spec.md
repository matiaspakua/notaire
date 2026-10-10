# api-request-dto-binding — delta

## Purpose

Protect REST write endpoints by accepting only validated request DTOs.

## MODIFIED Requirements

### Requirement: Copy, registration draft and re-entry require their fields

`POST /copia` and `PUT /copia/{id}` SHALL require `number` and `datePrinting`, `POST /minutas-inscripcion` SHALL require `idDeed`, and `POST /gestiones/{id}/reingreso-documentacion` SHALL require `idProcedure` and `idDocumentType`; a missing field SHALL answer 400 naming it and store nothing, and the contract SHALL mark the fields required.

#### Scenario: Empty copy

- **WHEN** a copy create sends {}
- **THEN** the response is 400 naming number and datePrinting and nothing is stored

#### Scenario: Copy update without print date

- **WHEN** a copy update omits datePrinting
- **THEN** the response is 400 naming it

#### Scenario: Registration draft and re-entry without ids

- **WHEN** a registration draft or a document re-entry sends {}
- **THEN** the response is 400 naming the missing ids

#### Scenario: Copies dialog

- **WHEN** the number or the print date is empty in the copies dialog
- **THEN** Save is disabled

#### Scenario: Contract

- **WHEN** the OpenAPI document is generated
- **THEN** the fields are required in the four request schemas
