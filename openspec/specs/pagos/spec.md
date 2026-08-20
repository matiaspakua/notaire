# pagos Specification

## Purpose

Governs how a `Pago` (payment applied to a `Presupuesto`) is processed, edited and
retrieved, per CU15 — Procesar pago.

## Requirements

### Requirement: Payment records the método de pago used to settle it
The system SHALL persist the payment method (`metodoPago`) supplied when a payment
is processed or edited, and SHALL return it on every subsequent retrieval of that
payment. The field is optional free text; the system SHALL NOT reject a payment for
omitting it.

#### Scenario: Processing a payment with a payment method
- **WHEN** a payment is processed with a `metodoPago` value (e.g. "Efectivo")
- **THEN** the payment is created and the response includes the same `metodoPago` value

#### Scenario: Retrieving a payment reflects the stored payment method
- **WHEN** a previously processed payment with a stored `metodoPago` is fetched by ID
  or listed by presupuesto
- **THEN** the returned payment includes the stored `metodoPago` value

#### Scenario: Editing a payment's payment method
- **WHEN** an existing payment is updated with a new `metodoPago` value
- **THEN** the payment's `metodoPago` is updated to the new value and subsequent
  retrievals return it

#### Scenario: Processing a payment without a payment method
- **WHEN** a payment is processed without a `metodoPago` value
- **THEN** the payment is created successfully and its `metodoPago` is absent
  (not defaulted to an invented value)

#### Scenario: Editing the payment method of a non-existent payment
- **WHEN** a payment update (including a `metodoPago` value) is submitted for an ID
  that does not exist
- **THEN** the system rejects the update with a not-found error and persists nothing
