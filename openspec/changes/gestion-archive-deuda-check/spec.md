# Specification: Gestión Archive with Deuda Verification (Issue #169)

## Purpose

Prevent archiving a gestión that has pending payment obligations. When a user attempts to archive, the system SHALL verify there is no outstanding balance. If balance exists, the system SHALL reject the archive request with a clear warning showing the pending amount.

## Requirements

### Requirement: Archive blocked when deuda exists
The system SHALL prevent archiving a gestión that has an outstanding balance (deuda > 0).

#### Scenario: Archive rejected with deuda warning
- **GIVEN** a gestión with presupuestos totaling $50,000
- **AND** payments totaling $30,000 have been registered
- **AND** saldo pendiente is $20,000
- **WHEN** the user attempts to archive the gestión
- **THEN** the archive request fails with an error showing: "No se puede archivar: deuda pendiente de $20,000.00"
- **AND** the gestión remains in its current estado (not archived)

#### Scenario: Archive succeeds when no deuda
- **GIVEN** a gestión with presupuestos totaling $50,000
- **AND** payments totaling $50,000 have been registered
- **AND** saldo pendiente is $0
- **WHEN** the user attempts to archive the gestión
- **THEN** the archive succeeds
- **AND** the gestión estado changes to "Archivado"

#### Scenario: Archive with partial deuda
- **GIVEN** a gestión with presupuestos totaling $100,000
- **AND** payments totaling $60,000 have been registered
- **AND** saldo pendiente is $40,000
- **WHEN** the user attempts to archive the gestión
- **THEN** the archive request fails with: "No se puede archivar: deuda pendiente de $40,000.00"

### Requirement: Clear deuda calculation
The system SHALL calculate deuda as: Total Presupuesto - Total Pagado = Deuda.

#### Scenario: Deuda calculated correctly
- **GIVEN** multiple presupuestos for a gestión (e.g., 3 presupuestos: $20k, $30k, $10k = $60k total)
- **AND** multiple payments registered (e.g., 2 payments: $25k, $15k = $40k total)
- **WHEN** attempting to archive
- **THEN** deuda is calculated as $60k - $40k = $20k
- **AND** error message shows exactly "$20,000.00"
