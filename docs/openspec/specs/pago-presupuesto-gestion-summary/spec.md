# pago-presupuesto-gestion-summary Specification

## Purpose

Exposes the persisted relationship between a payment (`Payment`) and its
`Budget`, and provides consultable financial summaries — by budget and by
management — of what was budgeted, collected and still owed, per CU47.

## Requirements

### Requirement: A payment response includes its associated budget

The system SHALL include the identifier of the associated budget (`idBudget`)
in every API response representing a `Payment`, whether returned individually,
in a list, or after being created or edited.

#### Scenario: Retrieving a single payment includes its budget

- **WHEN** a payment that is associated with a budget is retrieved by ID
- **THEN** the response includes the `idBudget` of that budget

#### Scenario: Listing payments by budget includes the budget on each entry

- **WHEN** the payments associated with a budget are listed
- **THEN** each returned payment includes the `idBudget` of that budget

#### Scenario: Creating a payment returns the associated budget

- **WHEN** a payment is processed against a budget
- **THEN** the response for the created payment includes the `idBudget` of
  that budget

### Requirement: Budget financial summary for CU47

The system SHALL provide, for a given budget, a summary containing the
associated management number, the management header (`managementHeading`),
the budget number, the budget total, the pending balance (`pendingBalance`),
and the list of payments applied to it (each with payment number, `amount`,
date, and `notes`), per CU47. The path is
`GET /api/v1/budgets/{id}/summary`.

#### Scenario: Budget with no payments

- **WHEN** the financial summary of a budget with no payments applied is
  requested
- **THEN** the response shows a `pendingBalance` equal to the budget total
  and an empty list of payments

#### Scenario: Budget with one payment

- **WHEN** the financial summary of a budget with one payment applied is
  requested
- **THEN** the response shows the reduced `pendingBalance` and a list
  containing that payment's number, `amount`, date, and `notes`

#### Scenario: Budget with multiple payments

- **WHEN** the financial summary of a budget with several payments applied
  is requested
- **THEN** the response shows the `pendingBalance` net of all payments and a
  list containing every payment applied

#### Scenario: Requesting the summary of a budget that does not exist

- **WHEN** the financial summary of a budget id that does not exist is
  requested
- **THEN** the system responds with a not-found error

### Requirement: Management financial summary

The system SHALL provide, for a given management, an aggregate financial
summary containing the total amount budgeted (`totalBudgeted`), the total
amount collected (`totalCollected`), and the pending balance
(`pendingBalance`), summed across every budget linked to the management's
procedures. The path is `GET /api/v1/managements/{id}/financial-summary`.

#### Scenario: Management with a single procedure and budget

- **WHEN** the financial summary of a management with one procedure linked
  to one budget is requested
- **THEN** the response shows that budget's total as `totalBudgeted`, the
  sum of its payments as `totalCollected`, and its pending balance as
  `pendingBalance`

#### Scenario: Management with multiple procedures and budgets

- **WHEN** the financial summary of a management with several procedures,
  each linked to its own budget, is requested
- **THEN** the response shows the sum of every linked budget's total as
  `totalBudgeted`, the sum of every payment across them as
  `totalCollected`, and the sum of their pending balances as
  `pendingBalance`

#### Scenario: Management with no payments registered

- **WHEN** the financial summary of a management whose linked budgets have
  no payments applied is requested
- **THEN** the response shows a `totalCollected` of zero and a
  `pendingBalance` equal to `totalBudgeted`
