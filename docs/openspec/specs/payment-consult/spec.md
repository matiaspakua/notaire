# payment-consult Specification

## Purpose

Consulting a budget's payments in the UI. Source: #1544, #1557; archived change(s) `2026-10-13-feat-1544-payments-by-budget`, `2026-10-13-feat-1557-payments-date-filter`.

## Requirements
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

### Requirement: Payments by date range

The payments page SHALL filter payments between two days, both inclusive, kept in the URL, and SHALL reject an inverted range in the UI and with HTTP 400 in the API.

#### Scenario: Inclusive

- **WHEN** payments on 10, 15 and 20 Feb and a 10–15 Feb range
- **THEN** the 10 and 15 Feb payments are listed

#### Scenario: With budget

- **WHEN** a budget and a range
- **THEN** only that budget's payments in the range, no `/payments/date` call

#### Scenario: Inverted UI

- **WHEN** from after to
- **THEN** an inline error, no request, no rows

#### Scenario: Inverted API

- **WHEN** startDate after endDate
- **THEN** 400

#### Scenario: Mobile

- **WHEN** 390px
- **THEN** inputs usable, cards listed

