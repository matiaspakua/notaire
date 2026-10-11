# gestion-archive-debt-check Specification

## Purpose

Calculates the aggregate pending balance of a management's budgets before it
is archived, and warns the user and records whether the management was
archived with outstanding debt, per CU16 / RF-22 / RF-37.

## Requirements

### Requirement: Calculate the aggregate pending balance of a management

The system SHALL calculate the aggregate pending balance (`pendingBalance`)
of a management by summing the pending balance of every budget linked to the
management's procedures. The path is
`GET /api/v1/managements/{id}/outstanding-balance`.

#### Scenario: Management with a single procedure and budget

- **WHEN** the aggregate balance of a management with one procedure linked
  to one budget with a positive pending balance is requested
- **THEN** the system returns that budget's pending balance as the
  management's `pendingBalance`

#### Scenario: Management with multiple procedures and budgets

- **WHEN** the aggregate balance of a management with several procedures,
  each linked to its own budget, is requested
- **THEN** the system returns the sum of the pending balances of every
  linked budget

#### Scenario: Management with no pending balance

- **WHEN** the aggregate balance of a management whose linked budgets are
  fully paid is requested
- **THEN** the system returns zero

### Requirement: Warn about pending debt when archiving a management

The system SHALL warn the user of any pending debt before confirming the
archiving of a management, without blocking the archiving action. The
archive path is `POST /api/v1/managements/{id}/archive` and keeps the query
flag `confirmado`.

#### Scenario: Archiving a management with pending debt

- **WHEN** a user requests to archive a management whose aggregate
  `pendingBalance` is greater than zero
- **THEN** the system presents a debt warning before the archiving is
  confirmed

#### Scenario: Confirming archiving despite pending debt

- **WHEN** a user confirms archiving a management after being warned of
  pending debt (`confirmado`)
- **THEN** the system archives the management

#### Scenario: Archiving a management with no pending debt

- **WHEN** a user requests to archive a management whose aggregate
  `pendingBalance` is zero
- **THEN** the system archives the management without presenting a debt
  warning

### Requirement: Record whether a management was archived with pending debt

The system SHALL persist, as part of the archiving record, whether the
management had a pending debt at the moment it was archived
(`pendingDebtAtArchiving`).

#### Scenario: Archiving record reflects pending debt

- **WHEN** a management with a pending aggregate balance greater than zero
  is archived
- **THEN** the archiving record for that management sets
  `pendingDebtAtArchiving` to show it was archived with pending debt

#### Scenario: Archiving record reflects no pending debt

- **WHEN** a management with an aggregate pending balance of zero is archived
- **THEN** the archiving record for that management sets
  `pendingDebtAtArchiving` to show it was archived without pending debt

### Requirement: Validate the workflow transition before archiving a management

The system SHALL validate, before archiving a management, that "Archivada" is
a reachable destination from the management's current status according to
the `WorkflowDefinition` of its `ProcedureType`
(`gestion-workflow-transicion`), in addition to the existing pending-debt
warning, per CU83 and CU16.

#### Scenario: Archiving succeeds when the transition to Archivada is valid

- **WHEN** a user archives a management whose current status has a
  `WorkflowTransition` to "Archivada" in the `WorkflowDefinition` of its
  `ProcedureType`
- **THEN** the system archives the management (subject to the existing
  pending-debt warning)

#### Scenario: Archiving is rejected when the transition to Archivada is invalid

- **WHEN** a user attempts to archive a management whose current status has
  no `WorkflowTransition` to "Archivada" in the `WorkflowDefinition` of its
  `ProcedureType`
- **THEN** the system rejects the archiving and responds with an error
  stating that the management cannot be archived from its current status
