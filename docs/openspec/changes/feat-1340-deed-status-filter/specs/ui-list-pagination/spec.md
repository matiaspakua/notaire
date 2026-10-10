# ui-list-pagination — delta

## Purpose

Lists and pickers backed by growing tables never load the whole table.

## MODIFIED Requirements

### Requirement: Deed list filters by status on the server

`GET /api/v1/escrituras` SHALL accept an optional `status` query parameter (Sin Firmar, Firmada, Anulada, No Paso, Inscripta, any letter case) and return one page of deeds in that status with its total; an unknown value SHALL answer 400 naming the accepted values; without the parameter the list SHALL be unchanged.

#### Scenario: Signed deeds only

- **WHEN** a client requests `GET /api/v1/escrituras?status=firmada`
- **THEN** every deed in the page has status Firmada and totalElements counts only signed deeds

#### Scenario: Unknown status

- **WHEN** a client requests `GET /api/v1/escrituras?status=Signed`
- **THEN** the API answers 400 and the message lists the accepted statuses

#### Scenario: No filter

- **WHEN** a client requests `GET /api/v1/escrituras` without status
- **THEN** every deed is listed, paged as before
