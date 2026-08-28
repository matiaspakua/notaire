<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md.
     Business rules belong here in normative form (SHALL/MUST); the permanent
     Use Case documentation remains their source of truth - cite it, do not
     duplicate it. -->

## Purpose

Validates and applies a state change of a gestión against the transitions
defined in the `WorkflowDefinition` of its tipo de trámite (CU83), instead
of accepting any estado value unconditionally through the generic `PUT`.

## ADDED Requirements

### Requirement: Transicionar estado de gestión contra el workflow definido
El sistema SHALL validar que un cambio de estado propuesto para una
gestión corresponda a una `WorkflowTransition` existente entre el
`WorkflowNode` del estado actual y el `WorkflowNode` del estado destino,
dentro del `WorkflowDefinition` asignado al `TipoDeTramite` de la gestión,
según CU83.

#### Scenario: Transición válida se aplica
- **WHEN** un usuario solicita cambiar el estado de una gestión a un
  estado para el cual existe una `WorkflowTransition` desde su estado
  actual, en el `WorkflowDefinition` de su tipo de trámite
- **THEN** el sistema aplica el cambio de estado a la gestión

#### Scenario: Transición inválida es rechazada
- **WHEN** un usuario solicita cambiar el estado de una gestión a un
  estado para el cual no existe ninguna `WorkflowTransition` desde su
  estado actual, en el `WorkflowDefinition` de su tipo de trámite
- **THEN** el sistema rechaza la operación y responde con un error que
  indica que la transición no es válida para el workflow del tipo de
  trámite

#### Scenario: Gestión sin workflow definido rechaza cualquier transición
- **WHEN** un usuario solicita cambiar el estado de una gestión cuyo tipo
  de trámite no tiene un `WorkflowDefinition` asignado
- **THEN** el sistema rechaza la operación y responde con un error que
  indica que el tipo de trámite no tiene un workflow configurado
