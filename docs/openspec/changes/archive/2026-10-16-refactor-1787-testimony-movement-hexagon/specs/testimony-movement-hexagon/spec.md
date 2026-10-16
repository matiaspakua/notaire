## ADDED Requirements

### Requirement: TestimonyMovementController is ports-only

`TestimonyMovementController` MUST NOT import JPA entities or Spring Data repositories. CRUD and circuit operations (enter-registration, register-inscription, withdraw, reenter) MUST go through inbound ports backed by an outbound OpsPort.

#### Scenario: list and get return DTOs via query port
- **WHEN** a client GETs `/api/v1/testimony-movements` or `/{id}`
- **THEN** the controller calls `QueryTestimonyMovementsUseCase` only

#### Scenario: writes and circuit via manage port
- **WHEN** a client POSTs/PUTs/DELETEs or invokes circuit endpoints
- **THEN** the controller calls `ManageTestimonyMovementsUseCase` only and maps sealed results to HTTP

#### Scenario: ArchUnit R1 shrinks
- **WHEN** ArchUnit runs against the hexagon baseline
- **THEN** the TestimonyMovementController → entity/repository R1 edges are absent