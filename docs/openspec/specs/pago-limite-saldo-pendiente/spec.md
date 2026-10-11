# pago-limite-saldo-pendiente Specification

## Purpose

Prevents a payment from being registered for more than the outstanding
balance (`pendingBalance`) of the `Budget` it applies to, so that a
mistyped or excessive amount cannot be accepted silently at the moment of
collection (CU15).

## Requirements

### Requirement: Reject a payment that exceeds the outstanding balance

The system SHALL reject a payment whose `amount` is greater than the
`pendingBalance` of the `Budget` it applies to, at the moment the payment
is registered.

#### Scenario: Payment within the outstanding balance is accepted

- **WHEN** a payment is registered for a `Budget` with `amount` less than
  its current `pendingBalance`
- **THEN** the payment is persisted and returned with HTTP 201

#### Scenario: Payment exactly matching the outstanding balance is accepted

- **WHEN** a payment is registered for a `Budget` with `amount` equal to
  its current `pendingBalance`
- **THEN** the payment is persisted and returned with HTTP 201

#### Scenario: Payment exceeding the outstanding balance is rejected

- **WHEN** a payment is registered for a `Budget` with `amount` greater
  than its current `pendingBalance`
- **THEN** the payment is NOT persisted and the request fails with HTTP 409
  Conflict

#### Scenario: The outstanding balance already accounts for prior payments

- **WHEN** a `Budget` already has one or more payments registered against
  it, reducing its `pendingBalance` below the budget total
- **THEN** a new payment is validated against the reduced `pendingBalance`,
  not against the budget's original total

### Requirement: Payment form surfaces the rejection reason

The payment form SHALL show a specific, actionable message when a payment
is rejected for exceeding the outstanding balance, distinct from a generic
save error. The rejection message key stays `saldoExcedido`.

#### Scenario: Operator sees a specific message when a payment is rejected

- **WHEN** an operator submits a payment `amount` that the system rejects
  for exceeding the `pendingBalance`
- **THEN** the payment form shows the `saldoExcedido` message, stating that
  the amount exceeds the outstanding balance, instead of a generic "could
  not save" message
