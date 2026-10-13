<!-- Governed by CONSTITUTION.md. CU78, issue #1635. Evidence: AuditAspect skips every GET. -->

## ADDED Requirements

### Requirement: Reads of Person data are audited without PII

An authenticated GET handler annotated `@AuditPersonalDataRead` MUST write exactly one audit record containing the module, the acting user and the endpoint, plus the person id for a single read. List and search reads MUST write one aggregated row. The record MUST NOT contain personal values (names, DNI, email) or search terms. Unannotated GET handlers MUST stay unaudited.

#### Scenario: single read writes one row with the id

- **WHEN** an authenticated user reads person 7
- **THEN** exactly one audit record is saved whose detail names person ID 7

#### Scenario: search writes one aggregated row without the search terms

- **WHEN** an authenticated user searches people by identification number `12345678`
- **THEN** exactly one audit record is saved and its detail does not contain `12345678`

#### Scenario: list writes one aggregated row

- **WHEN** an authenticated user lists people
- **THEN** exactly one audit record is saved

#### Scenario: unannotated reads stay unaudited

- **WHEN** an authenticated user performs a GET on a handler without the annotation
- **THEN** no audit record is saved

#### Scenario: person read handlers carry the annotation

- **WHEN** the three `PersonController` GET handlers are inspected
- **THEN** each is annotated `@AuditPersonalDataRead`
