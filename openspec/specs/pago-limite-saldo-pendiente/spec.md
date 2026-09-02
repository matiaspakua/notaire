# pago-limite-saldo-pendiente Specification

## Purpose
Prevents a payment from being registered for more than the outstanding
balance (saldo pendiente) of the presupuesto it applies to, so that a
mistyped or excessive amount cannot be accepted silently at the moment of
collection (CU15).
## Requirements
### Requirement: Reject a payment that exceeds the saldo pendiente
The system SHALL reject a payment whose `monto` is greater than the saldo
pendiente of the presupuesto it applies to, at the moment the payment is
registered.

#### Scenario: Payment within the saldo pendiente is accepted
- **WHEN** a payment is registered for a presupuesto with `monto` less than
  its current saldo pendiente
- **THEN** the payment is persisted and returned with HTTP 201

#### Scenario: Payment exactly matching the saldo pendiente is accepted
- **WHEN** a payment is registered for a presupuesto with `monto` equal to
  its current saldo pendiente
- **THEN** the payment is persisted and returned with HTTP 201

#### Scenario: Payment exceeding the saldo pendiente is rejected
- **WHEN** a payment is registered for a presupuesto with `monto` greater
  than its current saldo pendiente
- **THEN** the payment is NOT persisted and the request fails with HTTP 409
  Conflict

#### Scenario: Saldo pendiente calculation already accounts for prior payments
- **WHEN** a presupuesto already has one or more payments registered against
  it, reducing its saldo pendiente below the presupuesto's total
- **THEN** a new payment is validated against the reduced saldo pendiente,
  not against the presupuesto's original total

### Requirement: Payment form surfaces the rejection reason
The payment form SHALL show a specific, actionable message when a payment
is rejected for exceeding the saldo pendiente, distinct from a generic save
error.

#### Scenario: Operator sees a specific message when a payment is rejected for exceeding saldo
- **WHEN** an operator submits a payment amount that the system rejects for
  exceeding the saldo pendiente
- **THEN** the payment form shows a message stating that the amount exceeds
  the outstanding balance, instead of a generic "could not save" message

