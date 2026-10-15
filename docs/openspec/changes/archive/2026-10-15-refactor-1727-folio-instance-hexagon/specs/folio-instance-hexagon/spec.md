## ADDED Requirements

### Requirement: Folio instances are managed through domain-typed ports

The folio HTTP adapter MUST call inbound ports only. Use cases MUST own Utilizado lock and deed-link status forcing.

#### Scenario: CRUD and search keep the wire contract

- **WHEN** a client creates, lists, searches, gets, updates, or deletes a folio
- **THEN** status codes and JSON fields match the pre-refactor contract

#### Scenario: Utilizado cannot be modified or deleted except same-deed re-save

- **WHEN** a folio is Utilizado and the client updates to another deed or deletes
- **THEN** the response is 409 with the existing error body

#### Scenario: linking a deed forces Utilizado

- **WHEN** create or update supplies a deedId
- **THEN** the folio is saved as Utilizado

#### Scenario: in-use endpoint reports Utilizado

- **WHEN** GET /{id}/in-use runs
- **THEN** inUse is true iff status is Utilizado

#### Scenario: controller has no persistence imports

- **WHEN** ArchUnit R1 runs after the slice
- **THEN** FolioController has no baseline edges to repository or business entities
