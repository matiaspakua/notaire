# pagos-parciales-cuotas Specification

## Purpose

Allows recording partial payments against a budget without requiring a
single payment to cover the outstanding balance, and distinguishes whether
a budget is `NoPayments`, `PARTIAL`, or `PAID` when its payment status is
read (CU15, CU47).

## Requirements

### Requirement: Record a payment that does not cover the outstanding balance
The system SHALL accept recording a payment on a budget whose `amount` is
less than the outstanding balance, without rejecting it for not covering
the total.

#### Scenario: A payment that covers the budget total
- **WHEN** a single payment is recorded whose `amount` equals the budget
  total
- **THEN** the system accepts the payment and the outstanding balance is
  zero

#### Scenario: A partial payment that does not cover the total
- **WHEN** a payment is recorded whose `amount` is less than the budget's
  outstanding balance
- **THEN** the system accepts the payment without requiring it to cover
  the rest of the balance

#### Scenario: A sequence of partial payments that sum to the total
- **WHEN** several partial payments are recorded on the same budget and
  their amounts sum to its total
- **THEN** the system accepts each payment individually and the outstanding
  balance reaches zero after the last one

### Requirement: Determine a budget's payment status
The system SHALL determine a budget's payment status as `NoPayments` when
it has no payment recorded, `PARTIAL` when it has payments recorded and its
outstanding balance is greater than zero, and `PAID` when its outstanding
balance is zero. `GET /api/v1/payments/budget/{id}/status` serialises those
enum names.

#### Scenario: Budget with no payments recorded
- **WHEN** the payment status of a budget with no payment recorded is read
- **THEN** the system reports `NoPayments`

#### Scenario: Partially paid budget
- **WHEN** the payment status of a budget that has at least one payment
  recorded and an outstanding balance greater than zero is read
- **THEN** the system reports `PARTIAL`

#### Scenario: Fully paid budget
- **WHEN** the payment status of a budget whose outstanding balance is zero
  is read
- **THEN** the system reports `PAID`

### Requirement: Read a budget's payment status (CU47)
The system SHALL allow reading a budget's payment status independently of
the list of its payments, so a `PARTIAL` budget is distinguished from a
`PAID` one.

#### Scenario: Read the status of a partially paid budget
- **WHEN** the payment status of a partially paid budget is read
- **THEN** the system responds with `PARTIAL`

#### Scenario: Read the status of a fully paid budget
- **WHEN** the payment status of a fully paid budget is read
- **THEN** the system responds with `PAID`

#### Scenario: Read the status of a budget that does not exist
- **WHEN** the payment status of a budget whose id does not exist is read
- **THEN** the system responds with a not-found error
