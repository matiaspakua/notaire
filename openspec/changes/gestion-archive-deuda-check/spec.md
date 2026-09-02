# Specification: Gestión Archive with Deuda Verification (Issue #169)

> **Corrected under issue #914**: the original version of this spec required
> archiving to be **blocked** when `saldoPendiente > 0`. That contradicted the
> canonical Use Case (`docs/100-business/102-use-cases/CU16 – Archivar
> Gestión.md`), which requires archiving to succeed with a warning, not be
> rejected. The implementation based on this spec (commit 52776cc9) broke the
> pre-existing `GestionArchiveIntegrationTest` and was reverted. The
> requirements below reflect the corrected, CU16-compliant behavior.

## Purpose

Warn the user about pending payment obligations when archiving a gestión, without blocking the archive. The system SHALL verify the outstanding balance and persist whether the gestión was archived with pending debt, but SHALL NOT reject the archive request on that basis.

## Requirements

### Requirement: Archive succeeds and flags deuda when it exists
The system SHALL archive a gestión regardless of outstanding balance (deuda), persisting `deudaPendienteAlArchivar = true` when deuda > 0.

#### Scenario: Archive succeeds with deuda flagged
- **GIVEN** a gestión with presupuestos totaling $50,000
- **AND** payments totaling $30,000 have been registered
- **AND** saldo pendiente is $20,000
- **WHEN** the user attempts to archive the gestión
- **THEN** the archive succeeds (HTTP 200)
- **AND** the response reports `saldoPendiente: 20000.00` and `deudaPendienteAlArchivar: true`

#### Scenario: Archive succeeds when no deuda
- **GIVEN** a gestión with presupuestos totaling $50,000
- **AND** payments totaling $50,000 have been registered
- **AND** saldo pendiente is $0
- **WHEN** the user attempts to archive the gestión
- **THEN** the archive succeeds
- **AND** the gestión estado changes to "Archivada" with `deudaPendienteAlArchivar: false`

#### Scenario: Archive succeeds with partial deuda
- **GIVEN** a gestión with presupuestos totaling $100,000
- **AND** payments totaling $60,000 have been registered
- **AND** saldo pendiente is $40,000
- **WHEN** the user attempts to archive the gestión
- **THEN** the archive succeeds and reports `saldoPendiente: 40000.00`, `deudaPendienteAlArchivar: true`

### Requirement: Clear deuda calculation
The system SHALL calculate deuda as: Total Presupuesto - Total Pagado = Deuda.

#### Scenario: Deuda calculated correctly
- **GIVEN** multiple presupuestos for a gestión (e.g., 3 presupuestos: $20k, $30k, $10k = $60k total)
- **AND** multiple payments registered (e.g., 2 payments: $25k, $15k = $40k total)
- **WHEN** attempting to archive
- **THEN** deuda is calculated as $60k - $40k = $20k
- **AND** error message shows exactly "$20,000.00"
