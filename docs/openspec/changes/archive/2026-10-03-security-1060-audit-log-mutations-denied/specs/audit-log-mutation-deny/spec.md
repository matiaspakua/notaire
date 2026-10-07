## Purpose

Guarantee the public audit-log HTTP API is consult-only: clients cannot create,
update, or delete audit rows; mutations are rejected with HTTP 405.

## ADDED Requirements

### Requirement: Audit-log HTTP mutations MUST be denied

The system SHALL NOT expose create, update, or delete handlers for
`/api/v1/audit-log`. Authenticated or unauthenticated clients that send
`POST`, `PUT`, or `DELETE` to the audit-log resource (collection or
item path) SHALL receive `405 Method Not Allowed`. No audit row SHALL be
created, updated, or deleted as a result of those requests. Audit rows
SHALL continue to be written only by the server-side audit aspect from the
authenticated JWT identity.

#### Scenario: POST audit-log is rejected

- **WHEN** a client sends `POST /api/v1/audit-log` with any JSON body
- **THEN** the server responds `405 Method Not Allowed` and no new audit row
  is created from that request

#### Scenario: PUT audit-log is rejected

- **WHEN** a client sends `PUT /api/v1/audit-log/{id}` with any JSON body
- **THEN** the server responds `405 Method Not Allowed` and the existing row
  is unchanged

#### Scenario: DELETE audit-log is rejected

- **WHEN** a client sends `DELETE /api/v1/audit-log/{id}`
- **THEN** the server responds `405 Method Not Allowed` and the existing row
  remains

### Requirement: Audit-log OpenAPI and contract tests MUST describe consult-only access

OpenAPI documentation for the audit-log resource SHALL describe consult
(read) operations only and SHALL NOT advertise create/update/delete.
Bruno/api-test coverage SHALL include negative cases that assert `405` for
`POST`, `PUT`, and `DELETE` with a valid JWT.

#### Scenario: OpenAPI tag is consult-only

- **WHEN** a consumer inspects the OpenAPI tag/description for the audit-log
  API
- **THEN** the description states consult/query access and does not claim
  administration or mutation of audit records

#### Scenario: Bruno rejects audit-log mutations

- **WHEN** the Bruno suite runs authenticated `POST`, `PUT`, and `DELETE`
  against `/api/v1/audit-log`
- **THEN** each request expects HTTP status `405`
