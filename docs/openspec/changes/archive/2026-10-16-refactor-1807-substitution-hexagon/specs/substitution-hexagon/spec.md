## ADDED Requirements

### Requirement: Substitution controller is ports-only
The substitution HTTP adapter SHALL call inbound Query/Manage ports only; entity and PersonRepository access SHALL live behind SubstitutionOpsPort.

#### Scenario: query via inbound port
- **WHEN** a client lists or fetches substitutions
- **THEN** the controller delegates to QuerySubstitutionsUseCase and returns mapped views with nested person refs

#### Scenario: writes via inbound port
- **WHEN** a client creates, updates, or deletes a substitution
- **THEN** the controller delegates to ManageSubstitutionsUseCase; missing persons still answer 400

#### Scenario: ArchUnit R1 shrinks
- **WHEN** HexagonArchitectureTest runs
- **THEN** SubstitutionController has no R1 baseline edges to Substitution, Person, or PersonRepository
