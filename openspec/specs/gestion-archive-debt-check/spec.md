# gestion-archive-debt-check Specification

## Purpose

Calculates the aggregate pending balance of a gestión's presupuestos before it
is archived, and warns the user and records whether the gestión was archived
with outstanding debt, per CU-16 / RF-22 / RF-37.

## Requirements

### Requirement: Calculate aggregate pending balance of a gestión
The system SHALL calculate the aggregate pending balance ("saldo pendiente") of
a gestión by summing the pending balance of every presupuesto linked to the
gestión's trámites.

#### Scenario: Gestión with a single trámite and presupuesto
- **WHEN** the aggregate balance of a gestión with one trámite linked to one
  presupuesto with a positive pending balance is requested
- **THEN** the system returns that presupuesto's pending balance as the
  gestión's aggregate balance

#### Scenario: Gestión with multiple trámites and presupuestos
- **WHEN** the aggregate balance of a gestión with several trámites, each
  linked to its own presupuesto, is requested
- **THEN** the system returns the sum of the pending balances of every linked
  presupuesto

#### Scenario: Gestión with no pending balance
- **WHEN** the aggregate balance of a gestión whose linked presupuestos are
  fully paid is requested
- **THEN** the system returns zero

### Requirement: Warn about pending debt when archiving a gestión
The system SHALL warn the user of any pending debt before confirming the
archiving of a gestión, without blocking the archiving action.

#### Scenario: Archiving a gestión with pending debt
- **WHEN** a user requests to archive a gestión whose aggregate pending
  balance is greater than zero
- **THEN** the system presents a debt warning before the archiving is
  confirmed

#### Scenario: Confirming archiving despite pending debt
- **WHEN** a user confirms archiving a gestión after being warned of pending
  debt
- **THEN** the system archives the gestión

#### Scenario: Archiving a gestión with no pending debt
- **WHEN** a user requests to archive a gestión whose aggregate pending
  balance is zero
- **THEN** the system archives the gestión without presenting a debt warning

### Requirement: Record whether a gestión was archived with pending debt
The system SHALL persist, as part of the archiving record, whether the
gestión had a pending debt at the moment it was archived.

#### Scenario: Archiving record reflects pending debt
- **WHEN** a gestión with a pending aggregate balance greater than zero is
  archived
- **THEN** the archiving record for that gestión indicates it was archived
  with pending debt

#### Scenario: Archiving record reflects no pending debt
- **WHEN** a gestión with an aggregate pending balance of zero is archived
- **THEN** the archiving record for that gestión indicates it was archived
  without pending debt
