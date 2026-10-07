<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md.
     Business rules belong here in normative form (SHALL/MUST); the permanent
     Use Case documentation remains their source of truth - cite it, do not
     duplicate it. -->

## Purpose

Lets the operator choose the presupuesto being paid from a list instead of
typing its ID from memory, and shows its saldo pendiente before the payment
is confirmed, so a payment is never registered against the wrong
presupuesto or without knowing what is actually owed (CU15, pasos 2-5 y 11).

## ADDED Requirements

### Requirement: Payment form offers a presupuesto picker
The payment form SHALL let the operator select the presupuesto to pay from
a list of existing presupuestos, identified by presupuesto number and
associated client, instead of requiring a numeric ID to be typed.

#### Scenario: Operator selects a presupuesto from the picker
- **WHEN** the operator opens the picker on the payment form
- **THEN** it lists the existing presupuestos, each identified by its
  presupuesto number and the client it belongs to

#### Scenario: Operator picks a presupuesto and it becomes the payment target
- **WHEN** the operator selects one presupuesto from the picker
- **THEN** that presupuesto is set as the target of the payment being
  registered

#### Scenario: No presupuestos available
- **WHEN** the operator opens the picker and there are no presupuestos to
  choose from
- **THEN** the picker shows an empty state instead of an empty or broken
  dropdown

### Requirement: Payment form shows the saldo pendiente of the selected presupuesto
The payment form SHALL display the current saldo pendiente of the
presupuesto selected in the picker before the payment amount is confirmed.

#### Scenario: Saldo pendiente is shown after selecting a presupuesto
- **WHEN** the operator selects a presupuesto in the picker
- **THEN** the form displays that presupuesto's current saldo pendiente
  before the payment is submitted

#### Scenario: Saldo pendiente updates when the selection changes
- **WHEN** the operator changes the picker selection from one presupuesto
  to another
- **THEN** the displayed saldo pendiente updates to reflect the newly
  selected presupuesto, not the previous one

#### Scenario: Saldo pendiente fails to load
- **WHEN** the selected presupuesto's saldo pendiente cannot be retrieved
  (e.g. network or server error)
- **THEN** the form shows that the saldo pendiente is unavailable instead of
  a stale or blank value, and does not block selecting a different
  presupuesto
