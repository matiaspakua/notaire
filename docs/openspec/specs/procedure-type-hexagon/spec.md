# procedure-type-hexagon Specification

## Purpose
TBD - created by archiving change refactor-1791-procedure-type-hexagon. Update Purpose after archive.

## Requirements

### Requirement: ProcedureTypeController is ports-only

`ProcedureTypeController` MUST NOT import JPA entities or Spring Data repositories. CRUD, in-use checks, and workflow assignment MUST go through inbound ports backed by an outbound OpsPort.

#### Scenario: list search get via query port
- **WHEN** a client GETs procedure types
- **THEN** the controller calls `QueryProcedureTypesUseCase` only

#### Scenario: writes in-use and workflow via manage port
- **WHEN** a client mutates a procedure type or assigns a workflow
- **THEN** the controller calls `ManageProcedureTypesUseCase` only

#### Scenario: ArchUnit R1 shrinks
- **WHEN** ArchUnit runs against the hexagon baseline
- **THEN** the ProcedureTypeController R1 edges are absent
