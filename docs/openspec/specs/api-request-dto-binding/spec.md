# api-request-dto-binding Specification

## Purpose

Protect REST write endpoints from mass assignment by accepting only validated
request DTOs with client-writable fields, returning response DTOs, and keeping
audit-log entries server-authored.

## Requirements

### Requirement: Controllers MUST bind validated request DTOs, not JPA entities

Every create/update endpoint listed in issue #1068 (except audit-log create,
which is removed) SHALL accept a request DTO/record that exposes only
client-writable fields and SHALL be annotated with `@Valid`. The request DTO
SHALL NOT include server-managed fields `id` or `version`. Associations SHALL
be represented as identifier fields, not nested entity graphs.

#### Scenario: Property create ignores client-supplied id and version

- **WHEN** a client POSTs `/api/v1/inmueble` with a body that also contains
  `id` / `version` values alongside writable property fields
- **THEN** the server creates a new property with a generated id and
  server-managed version, and does not persist the client-supplied id/version

#### Scenario: Person update uses path id and existing version

- **WHEN** a client PUTs `/api/v1/people/{id}` with a validated person request
  DTO (no id/version in the contract)
- **THEN** the server updates the person identified by `{id}` using the
  existing optimistic-lock version and returns a response DTO

#### Scenario: Invalid person request is rejected

- **WHEN** a client POSTs `/api/v1/people` with a blank `firstName`
- **THEN** the server responds `400 Bad Request` without persisting

#### Scenario: Budget create accepts only writable budget fields

- **WHEN** a client POSTs `/api/v1/presupuestos` with a budget request DTO
  (number, date, heading, status, amounts, notes, personId) and extra
  server-managed keys
- **THEN** the server creates the budget from writable fields only and returns
  a response DTO with the generated id

### Requirement: Responses for those writes SHALL use response DTOs

Create, get-by-id, and update success payloads for the converted resources
SHALL be response DTOs/records (not undocumented Hibernate entity graphs as
the OpenAPI contract). Response DTOs MAY include server-managed id and
version for clients to read.

#### Scenario: Created resource returns response DTO with generated id

- **WHEN** a client successfully creates a property via the request DTO
- **THEN** the response body includes the generated id and does not require
  Hibernate-proxy workarounds in the documented schema

### Requirement: Public audit-log mutation MUST be disabled

`POST /api/v1/audit-log` SHALL NOT accept client-created audit entities.
Audit rows SHALL be written only by server-side auditing (`AuditoriaAspect`).

#### Scenario: Audit-log POST is rejected

- **WHEN** a client POSTs `/api/v1/audit-log` with any JSON body
- **THEN** the server responds with `405 Method Not Allowed` or `404 Not Found`
  and no new audit row is created from that request
