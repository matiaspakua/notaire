# audit-tx-query-usecase Specification

## Purpose
Audit read TX lives on QueryAuditRecordsUseCase; web adapters declare none.
## Requirements
### Requirement: Audit read transactions live in the application layer

`AuditRecordController` SHALL NOT declare `@Transactional`. Audit read operations SHALL run inside a read-only transaction on the application use-case service that implements `QueryAuditRecordsUseCase`.

#### Scenario: No web-tier @Transactional on audit controller

- **GIVEN** the backend adapter.in.web tree
- **WHEN** sources are scanned for `@Transactional`
- **THEN** no match exists under `adapter/in/web` (including AuditRecordController)

#### Scenario: getById uses inbound query port

- **GIVEN** a mocked `QueryAuditRecordsUseCase`
- **WHEN** `GET /api/v1/audit-log/{id}` is called for an existing id
- **THEN** the controller returns 200 and the query use case was invoked

