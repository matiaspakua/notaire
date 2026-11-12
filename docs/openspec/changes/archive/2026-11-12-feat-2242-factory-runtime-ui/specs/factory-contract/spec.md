# factory-contract Specification (delta)

## ADDED Requirements

### Requirement: Phase D runtime adapts Project and Run without a second SoR

The kit OpenAPI / CLI contract SHALL remain the system of record for Projects,
Runs, and RunRequests. Phase D runtime HTML/serve adapters MUST NOT introduce a
second OpenAPI tree, a parallel project/run store, or competing write semantics
for paths owned by Phase A.4 (`/factory/projects`, `/factory/runs`). Descriptive
notes MAY document that the runtime UI adapts those existing operations.

#### Scenario: no second OpenAPI tree in Phase D

- **WHEN** `aisdlc contract check` runs after Phase D lands
- **THEN** the factory OpenAPI remains a single tree and Phase D does not add
  competing `/factory/*` schemas for Project/Run ownership
