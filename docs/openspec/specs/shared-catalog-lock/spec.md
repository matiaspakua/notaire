# shared-catalog-lock Specification

## Purpose
TBD - created by archiving change refactor-1722-shared-catalog-lock. Update Purpose after archive.

## Requirements

### Requirement: Shared write lock preserves catalog HTTP behaviour

The shared catalog lock helper MUST drive update and delete for folio types, document types and management statuses without changing status codes or error bodies those catalogs already expose.

#### Scenario: locked update and delete keep 409 bodies

- **WHEN** a referenced folio type, document type or management status is updated or deleted
- **THEN** the answer remains 409 with the same `error` (or legacy) body text as before this extract

#### Scenario: stale version keeps the standard 409

- **WHEN** an unused folio type, document type or management status is updated with a version that is not the stored one
- **THEN** the answer remains the standard stale-version 409 message clients already receive

#### Scenario: unknown id keeps empty 404

- **WHEN** update or delete targets an unknown id for those catalogs
- **THEN** the answer remains an empty 404

### Requirement: The shared rule is tested without Spring

The lock helper MUST be unit-tested with fakes (no Spring context, no database) so the control flow is proven independent of each catalog's persistence adapter.

#### Scenario: helper unit tests cover lock and version paths

- **WHEN** the helper unit tests run
- **THEN** they cover happy update, locked update, stale version, happy delete, locked delete and missing id

### Requirement: Identification asymmetry is intentional

Identification types MUST NOT gain an update lock as part of this change. Delete may use the shared helper only if the HTTP body stays identical.

#### Scenario: identification update stays unlocked

- **WHEN** an identification type that people use is updated with a valid unique name
- **THEN** the update succeeds as today (no new 409 from this extract)
