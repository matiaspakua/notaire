# pago-presupuesto-picker-saldo Specification

## Purpose

Lets the operator choose the `Budget` being paid from a list instead of
typing its id from memory, and shows its `pendingBalance` before the
payment is confirmed, so a payment is never registered against the wrong
budget or without knowing what is actually owed (CU15, steps 2-5 and 11).

## Requirements

### Requirement: Payment form offers a budget picker

The payment form SHALL let the operator select the `Budget` to pay from a
list of existing budgets, identified by budget number and associated client,
instead of requiring a numeric id to be typed.

#### Scenario: Operator selects a budget from the picker

- **WHEN** the operator opens the picker on the payment form
- **THEN** it lists the existing budgets, each identified by its budget
  number and the client it belongs to

#### Scenario: Operator picks a budget and it becomes the payment target

- **WHEN** the operator selects one `Budget` from the picker
- **THEN** that budget is set as the target of the payment being registered

#### Scenario: No budgets available

- **WHEN** the operator opens the picker and there are no budgets to choose
  from
- **THEN** the picker shows an empty state instead of an empty or broken
  dropdown

### Requirement: Payment form shows the outstanding balance of the selected budget

The payment form SHALL display the current `pendingBalance` of the `Budget`
selected in the picker before the payment amount is confirmed. The catalog
keys `saldo.pending`, `saldo.summary`, `saldo.error`, and `saldoExcedido`
stay.

#### Scenario: Outstanding balance is shown after selecting a budget

- **WHEN** the operator selects a `Budget` in the picker
- **THEN** the form displays that budget's current `pendingBalance` before
  the payment is submitted

#### Scenario: Outstanding balance updates when the selection changes

- **WHEN** the operator changes the picker selection from one `Budget` to
  another
- **THEN** the displayed `pendingBalance` updates to reflect the newly
  selected budget, not the previous one

#### Scenario: Outstanding balance fails to load

- **WHEN** the selected budget's `pendingBalance` cannot be retrieved (for
  example a network or server error)
- **THEN** the form shows the `saldo.error` state instead of a stale or
  blank value, and does not block selecting a different budget
