# registration-draft-hexagon Specification

## Purpose
TBD - created by archiving change refactor-1819-registration-draft-hexagon. Update Purpose after archive.

## Requirements

### Requirement: RegistrationDraftController is ports-only

`RegistrationDraftController` MUST NOT depend on JPA entities. Generate/query/submit/observe/register MUST go through inbound ports backed by an outbound OpsPort.

#### Scenario: get returns DTO via query port
- **WHEN** a client GETs `/api/v1/registration-drafts/{id}`
- **THEN** the controller calls `QueryRegistrationDraftsUseCase` only

#### Scenario: writes via manage port
- **WHEN** a client POSTs generate or PUTs submit/observe/register
- **THEN** the controller calls `ManageRegistrationDraftsUseCase` only and maps sealed results to HTTP

#### Scenario: ArchUnit R1 shrinks
- **WHEN** ArchUnit runs against the hexagon baseline
- **THEN** the RegistrationDraftController → RegistrationDraft R1 edge is absent

#### Scenario: REST contract unchanged
- **WHEN** clients call `/api/v1/registration-drafts` generate/query/submit/observe/register
- **THEN** status codes and JSON shapes match the pre-slice behaviour
