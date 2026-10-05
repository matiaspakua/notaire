## Purpose

REST handlers answer with records and never expose JPA entities. Source: #577, #1279; owner CU76.

## ADDED Requirements

### Requirement: Handlers do not expose entities

A public handler method of a `@RestController` MUST NOT use a `com.licensis.notaire.business` type in its return type, generic arguments or parameters, and MUST NOT add a loosely typed return (`Object`, `?`) beyond the recorded baseline.

#### Scenario: No entity in a handler signature

- **WHEN** the REST controllers are inspected by reflection
- **THEN** no handler signature mentions a `business` type

#### Scenario: Loose returns do not grow

- **WHEN** the loosely typed handler returns are compared with the baseline
- **THEN** none is missing from the baseline and none is listed that is no longer loose

### Requirement: Related entities are slim references

A response record MUST expose a related notary as `personId` and `notaryRegistrationNumber` only, and other related entities by id and name.

#### Scenario: Folio and notebook expose a slim notary

- **WHEN** a folio or a notebook is requested
- **THEN** its `fkIdNotaryPerson` has `personId` and `notaryRegistrationNumber` and no personal data, and the body has no `atributos`, `dto` or `dtoDocument`

#### Scenario: Cost templates and procedures expose references by id and name

- **WHEN** the cost templates of a procedure type or a procedure are requested
- **THEN** related types appear as objects with their id and name

#### Scenario: Available notaries are person records

- **WHEN** the available notaries are requested
- **THEN** each item is a person record with `personId`

### Requirement: Unused DTO classes are deleted

The system MUST NOT contain `DtoFlag` or `DtoIdentification`.

#### Scenario: Unused DTOs are gone

- **WHEN** the backend sources are searched for `DtoFlag` and `DtoIdentification`
- **THEN** there are no matches
