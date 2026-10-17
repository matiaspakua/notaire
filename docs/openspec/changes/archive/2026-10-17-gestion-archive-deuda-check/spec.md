# Specification: Management archive with pending-debt verification (Issue #169)

> **Corrected under issue #914**: the original version of this spec required
> archiving to be **blocked** when the pending balance was greater than zero.
> That contradicted the canonical Use Case (`docs/100-business/102-use-cases/CU16 – Archivar Gestión.md`), which requires archiving to succeed with a warning, not be
> rejected. The implementation based on this spec (commit 52776cc9) broke the
> pre-existing `GestionArchiveIntegrationTest` and was reverted. The
> requirements below reflect the corrected, CU16-compliant behavior.

## Purpose

Warn the user about pending payment obligations when archiving a management, without blocking the archive. The system SHALL verify the outstanding balance and persist whether the management was archived with pending debt, but SHALL NOT reject the archive request on that basis.

## Requirements

### Requirement: Archive succeeds and flags pending debt when it exists
The system SHALL archive a management regardless of outstanding balance, persisting `pendingDebtAtArchiving = true` when the pending balance is greater than zero.

#### Scenario: Archive succeeds with pending debt flagged
- **GIVEN** a management with budgets totaling $50,000
- **AND** payments totaling $30,000 have been registered
- **AND** the pending balance is $20,000
- **WHEN** the user attempts to archive the management
- **THEN** the archive succeeds (HTTP 200)
- **AND** the response reports `pendingBalance` 20000.00 and `pendingDebtAtArchiving` true

#### Scenario: Archive succeeds when there is no pending debt
- **GIVEN** a management with budgets totaling $50,000
- **AND** payments totaling $50,000 have been registered
- **AND** the pending balance is $0
- **WHEN** the user attempts to archive the management
- **THEN** the archive succeeds
- **AND** the management status changes to "Archivada" with `pendingDebtAtArchiving: false`

#### Scenario: Archive succeeds with a partial pending balance
- **GIVEN** a management with budgets totaling $100,000
- **AND** payments totaling $60,000 have been registered
- **AND** the pending balance is $40,000
- **WHEN** the user attempts to archive the management
- **THEN** the archive succeeds and reports `pendingBalance` 40000.00 and `pendingDebtAtArchiving` true

### Requirement: Pending balance is budgets minus payments
The system SHALL calculate the pending balance as the total of the budgets minus the payments already registered.

#### Scenario: Pending balance calculated correctly
- **GIVEN** multiple budgets for a management (e.g., 3 budgets: $20k, $30k, $10k = $60k total)
- **AND** multiple payments registered (e.g., 2 payments: $25k, $15k = $40k total)
- **WHEN** attempting to archive
- **THEN** the pending balance is calculated as $60k - $40k = $20k
- **AND** the warning shows exactly "$20,000.00"
