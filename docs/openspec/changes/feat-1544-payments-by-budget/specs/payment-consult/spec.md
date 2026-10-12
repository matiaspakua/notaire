# payment-consult — delta

## Purpose

Consulting a budget's payments in the UI.

## MODIFIED Requirements

### Requirement: Consult payments by budget

The payments page SHALL let the user pick a budget and SHALL then show its total, paid amount, outstanding balance, payment status and only its payments.

#### Scenario: Consult

- **WHEN** a budget with 2 payments is picked
- **THEN** the summary shows status Parcialmente abonado and the table has 2 rows

#### Scenario: URL

- **WHEN** the page is reloaded with `?budget=`
- **THEN** the selection is kept; clearing returns to the full list

#### Scenario: Empty

- **WHEN** a budget without payments
- **THEN** 'Este presupuesto no tiene pagos' is shown

#### Scenario: Mobile

- **WHEN** the viewport is 390px
- **THEN** the summary is visible without horizontal overflow
