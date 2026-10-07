# rest-api-conventions Specification

## Purpose

Defines REST naming conventions (ADR-023) and the create-response contract
(`201 Created` + `Location`) for Notaire's public `/api/v1` surface under CU76.

## Requirements

### Requirement: Successful creates return 201 with Location

Successful resource-create POSTs SHALL return HTTP `201 Created` and a
`Location` response header whose value is the absolute or path URI of the
created resource (collection path + id). Controllers SHALL build that URI via a
shared helper (not ad-hoc string concatenation per controller).

#### Scenario: Registration draft create returns 201 and Location

- **WHEN** a client successfully POSTs `/api/v1/minutas-inscripcion` with a
  valid generate request
- **THEN** the response status is `201 Created`, the body includes the created
  draft id, and `Location` points at `/api/v1/minutas-inscripcion/{id}`

#### Scenario: Sample payment create includes Location

- **WHEN** a client successfully POSTs `/api/v1/pagos` with a valid payment body
- **THEN** the response status is `201 Created` and `Location` points at
  `/api/v1/pagos/{id}`

#### Scenario: Sample folio create includes Location

- **WHEN** a client successfully POSTs `/api/v1/folio` with a valid folio body
- **THEN** the response status is `201 Created` and `Location` points at
  `/api/v1/folio/{id}`

### Requirement: Unused payment params create route is removed

`POST /api/v1/pagos/params` SHALL NOT be exposed. Payment creation remains
available via `POST /api/v1/pagos` (JSON body).

#### Scenario: Payment params create is absent

- **WHEN** a client POSTs `/api/v1/pagos/params` with query parameters that
  previously created a payment
- **THEN** the server does not create a payment through that route (no matching
  handler: typically `404` or `405`)

### Requirement: Naming conventions are recorded in ADR-023

REST resource naming (language, plural collection nouns, search path, action
sub-resources, phased rename under ADR-003) SHALL be documented in
`docs/200-architecture/202-ADR/ADR-023-rest-resource-naming.md`. ADR-003 remains
versioning-only.

#### Scenario: ADR-023 exists and states English resource nouns for new paths

- **WHEN** an engineer opens ADR-023
- **THEN** it states that new `/api/v1` collection paths use English resource
  nouns matching the established English paths, prefer plurals, use `/search`
  for search, model actions as sub-resources, and phase renames without a
  big-bang rewrite in this slice
