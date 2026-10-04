# deed-management-entity-null-safety Specification

## Purpose
Make `DeedManagement` (and related `Person` DTO helpers) null-safe for unset
relationships, and prove the behavior with comprehensive unit tests so
gestión entity mapping never fails with `NullPointerException`.

## Requirements

### Requirement: DeedManagement DTO mapping is null-safe for unset associations
`DeedManagement.getDto()`, `getDtoNotary()`, and `setAtributos()` SHALL tolerate
null notary, null management status, and null identification type without
throwing `NullPointerException`.

#### Scenario: getDto with null management status does not throw
- **WHEN** `getDto()` is called on a `DeedManagement` whose
  `fkIdManagementStatus` is null
- **THEN** no `NullPointerException` is thrown and the returned DTO has a null
  (or unset) status

#### Scenario: getDtoNotary with null notary returns null
- **WHEN** `getDtoNotary()` is called on a `DeedManagement` whose
  `fkIdNotaryPerson` is null
- **THEN** the method returns null and does not throw

#### Scenario: getDtoNotary with notary missing identification type does not throw
- **WHEN** `getDtoNotary()` is called and the notary person has a null
  `fkIdIdentificationType`
- **THEN** no `NullPointerException` is thrown and the DTO omits or nulls the
  identification type

#### Scenario: setAtributos with null status does not throw
- **WHEN** `setAtributos()` is called with a DTO whose status is null
- **THEN** no `NullPointerException` is thrown and the entity's prior status
  (if any) is left unchanged

#### Scenario: getDto with status and notary present maps both
- **WHEN** `getDto()` is called on a gestión with non-null status and notary
  (notary has identification type)
- **THEN** the DTO carries both status and personNotary fields populated

### Requirement: Constructor list initialization is covered by unit tests
Unit tests SHALL document and assert the current list-initialization behavior
of `DeedManagement` constructors.

#### Scenario: Default constructor initializes empty procedure and history lists
- **WHEN** a `DeedManagement` is created with the default constructor
- **THEN** `getProcedureList()` and `getHistoryList()` are non-null empty lists

#### Scenario: Id constructor initializes empty procedure and history lists
- **WHEN** a `DeedManagement` is created with the id constructor
- **THEN** `getProcedureList()` and `getHistoryList()` are non-null empty lists

### Requirement: Person.getDto is null-safe for gestión-related associations
`Person.getDto()` SHALL tolerate a null identification type and a null
`DeedManagementList` without throwing `NullPointerException`.

#### Scenario: Person getDto with null identification type does not throw
- **WHEN** `getDto()` is called on a `Person` whose `fkIdIdentificationType`
  is null
- **THEN** no `NullPointerException` is thrown

#### Scenario: Person getDto with null DeedManagementList does not throw
- **WHEN** `getDto()` is called on a notary `Person` whose
  `DeedManagementList` is null
- **THEN** no `NullPointerException` is thrown and gestión lists on the DTO
  remain empty/absent

### Requirement: DeedManagement entity unit suite is green
The `DeedManagementEntityTest` suite SHALL pass completely after the
null-safety fixes, with no compilation errors in `backend-api`.

#### Scenario: All DeedManagementEntityTest methods pass
- **WHEN** `mvn test -pl backend-api -Dtest=DeedManagementEntityTest` is run
- **THEN** every test method passes and the module compiles
