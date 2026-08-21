<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md.
     Business rules belong here in normative form (SHALL/MUST); the permanent
     Use Case documentation remains their source of truth - cite it, do not
     duplicate it. -->

## ADDED Requirements

### Requirement: Validate workflow transition before archiving a gestión
The system SHALL validate, before archiving a gestión, that "Archivada" is
a reachable destination from the gestión's current estado according to
the `WorkflowDefinition` of its tipo de trámite (`gestion-workflow-transicion`),
in addition to the existing pending-debt warning, per CU83 and CU16.

#### Scenario: Archiving succeeds when the transition to Archivada is valid
- **WHEN** a user archives a gestión whose current estado has a
  `WorkflowTransition` to "Archivada" in the `WorkflowDefinition` of its
  tipo de trámite
- **THEN** the system archives the gestión (subject to the existing
  pending-debt warning)

#### Scenario: Archiving is rejected when the transition to Archivada is invalid
- **WHEN** a user attempts to archive a gestión whose current estado has
  no `WorkflowTransition` to "Archivada" in the `WorkflowDefinition` of
  its tipo de trámite
- **THEN** the system rejects the archiving and responds with an error
  indicating that the gestión cannot be archived from its current estado
