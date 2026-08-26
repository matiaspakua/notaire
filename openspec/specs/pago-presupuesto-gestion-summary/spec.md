# pago-presupuesto-gestion-summary Specification

## Purpose

Exposes the persisted relationship between a payment (`Pago`) and its
presupuesto, and provides consultable financial summaries — by presupuesto and
by gestión — of what was budgeted, collected and still owed, per CU47.

## Requirements

### Requirement: A payment response includes its associated presupuesto
The system SHALL include the identifier of the associated presupuesto in every
API response representing a `Pago`, whether returned individually, in a list,
or after being created or edited.

#### Scenario: Retrieving a single payment includes its presupuesto
- **WHEN** a payment that is associated with a presupuesto is retrieved by ID
- **THEN** the response includes the identifier of that presupuesto

#### Scenario: Listing payments by presupuesto includes the presupuesto on each entry
- **WHEN** the payments associated with a presupuesto are listed
- **THEN** each returned payment includes the identifier of that presupuesto

#### Scenario: Creating a payment returns the associated presupuesto
- **WHEN** a payment is processed against a presupuesto
- **THEN** the response for the created payment includes the identifier of
  that presupuesto

### Requirement: Presupuesto financial summary for CU47
The system SHALL provide, for a given presupuesto, a summary containing the
associated gestión number, the gestión header, the presupuesto number, the
presupuesto total, the pending balance, and the list of payments applied to
it (each with payment number, amount, date, and observations), per CU47.

#### Scenario: Presupuesto with no payments
- **WHEN** the financial summary of a presupuesto with no payments applied is
  requested
- **THEN** the response shows a pending balance equal to the presupuesto total
  and an empty list of payments

#### Scenario: Presupuesto with one payment
- **WHEN** the financial summary of a presupuesto with one payment applied is
  requested
- **THEN** the response shows the reduced pending balance and a list
  containing that payment's number, amount, date, and observations

#### Scenario: Presupuesto with multiple payments
- **WHEN** the financial summary of a presupuesto with several payments
  applied is requested
- **THEN** the response shows the pending balance net of all payments and a
  list containing every payment applied

#### Scenario: Requesting the summary of a non-existent presupuesto
- **WHEN** the financial summary of a presupuesto ID that does not exist is
  requested
- **THEN** the system responds with a not-found error

### Requirement: Gestión financial summary
The system SHALL provide, for a given gestión, an aggregate financial summary
containing the total amount budgeted, the total amount collected, and the
pending balance, summed across every presupuesto linked to the gestión's
trámites.

#### Scenario: Gestión with a single trámite and presupuesto
- **WHEN** the financial summary of a gestión with one trámite linked to one
  presupuesto is requested
- **THEN** the response shows that presupuesto's total as the total budgeted,
  the sum of its payments as the total collected, and its pending balance as
  the saldo

#### Scenario: Gestión with multiple trámites and presupuestos
- **WHEN** the financial summary of a gestión with several trámites, each
  linked to its own presupuesto, is requested
- **THEN** the response shows the sum of every linked presupuesto's total as
  the total budgeted, the sum of every payment across them as the total
  collected, and the sum of their pending balances as the saldo

#### Scenario: Gestión with no payments registered
- **WHEN** the financial summary of a gestión whose linked presupuestos have
  no payments applied is requested
- **THEN** the response shows a total collected of zero and a saldo equal to
  the total budgeted
