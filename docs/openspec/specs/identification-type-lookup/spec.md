# identification-type-lookup Specification

## Purpose

Resolve identification-type names and ids without a god class. Source: #900; owner CU76.

## Requirements

### Requirement: Name by id

`IdentificationTypeLookup.nameOf` MUST return the name of the identification type whose id equals the given id, and null when there is none or the id is null.

#### Scenario: An id matches exactly

- **WHEN** the catalog holds ids 1 and 11 and `nameOf(1)` is called
- **THEN** it returns the name of id 1, not of id 11

#### Scenario: The id is unknown or null

- **WHEN** `nameOf(99)` or `nameOf(null)` is called
- **THEN** it returns null

### Requirement: Id by name

`IdentificationTypeLookup.idOf` MUST return the id of the identification type whose name equals the given name ignoring case, and 0 when there is none or the name is null.

#### Scenario: A name matches exactly

- **WHEN** the catalog holds `DNI` and `DNI Extranjero` and `idOf("dni")` is called
- **THEN** it returns the id of `DNI`

#### Scenario: The name is unknown or null

- **WHEN** `idOf("unknown")` or `idOf(null)` is called
- **THEN** it returns 0
