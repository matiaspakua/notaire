# ADR-014: Configurable workflow engine for managements

**Status:** Accepted
**Date:** 2026-06-09
**Deciders:** Matías Miguez
**Related:** ADR-001 (Microservices Architecture), ADR-004 (Database Migration)

## Context

The legacy monolith modeled the life cycle of a `DeedManagement` (and of a `Procedure`) as a fixed set of states coded in Java (`ManagementStatus`), with the valid transitions scattered through conditionals in the legacy JPA controllers. Adding or changing a state, or changing which transitions are valid for a procedure type, required a code change and a deploy.

The notarial system has many procedure types (`ProcedureType`), each with its own valid state flow. A state machine fixed in code does not scale to that variation without duplicating logic per type.

## Decision

Model the state flow as versioned data in the database, with three JPA entities in `com.licensis.notaire.business`:

- **`WorkflowDefinition`** — one flow definition, assignable to one or more `ProcedureType` values.
- **`WorkflowNode`** — one node in the flow, linked to an existing `ManagementStatus` and typed as `WorkflowNodeType`: `INITIAL`, `INTERMEDIATE`, or `FINAL`. It includes coordinates (`positionX`, `positionY`) so the frontend can draw the diagram.
- **`WorkflowTransition`** — a directed edge between two `WorkflowNode` values (`originNode` → `destinationNode`), with an optional `condition` (an expression evaluated to enable the transition) and a readable `description`.

Transition checks are exposed through `WorkflowValidationController` / `WorkflowValidationService`, and flow-instance traceability through `WorkflowTraceService`. Creating and editing flows is exposed through `WorkflowDefinitionController`, `WorkflowNodeController`, and `WorkflowTransitionController`.

Every entity has a `version` column for optimistic locking (`@Version`, applied by the application), consistent with the rest of the domain model.

### Why a data-driven workflow engine (not a fixed status enum, and not an external BPMN engine)

| Criterion | Data-driven engine (chosen) | Fixed status enum | External BPMN engine (Camunda/Flowable) |
|----------|----------------------------------|------------------------|------------------------------------------|
| Flexibility per procedure type | High — each `ProcedureType` references its own `WorkflowDefinition` | None — one flow for every type | High, but with a process model foreign to the domain |
| Added complexity | Medium — 3 new entities, no external engine | Minimal | High — a new runtime, a new language (BPMN XML), and a learning curve |
| Flow changes without a deploy | Yes — edit rows, not code | No | Yes |
| Fit with existing Spring Data JPA | Direct — the same conventions as the rest of the domain | Direct | Needs extra integration |

**Decision:** a small, in-house, data-driven workflow engine. A full BPMN engine would be more machinery than this domain needs for a bounded number of procedure types.

## Consequences

### Positive

- An administrator can define or adjust the flow of a procedure type without a code change, through the `Workflow*Controller` endpoints.
- The model maps directly to a state diagram (see `docs/200-architecture/204-diagrams/Diagrama de Estados/`), because each `WorkflowNode` maps 1:1 to a `ManagementStatus`.
- It reuses the established patterns (JPA, DTOs, repositories) and does not introduce a new backend paradigm.

### Negative

- Validating a `WorkflowTransition` `condition` depends on application conventions (there is no formal expression engine); its meaning has to be documented per use case.
- A badly configured `WorkflowDefinition` (for example, no reachable `FINAL` node) is not caught at design time; it depends on `WorkflowValidationService` at runtime.

## Related ADRs

- ADR-001: Microservices Architecture
- ADR-004: Database Migration (MySQL → PostgreSQL)
