# pagos Specification

## Purpose

Governs how a `Payment` (a payment applied to a `Budget`) is processed,
edited and retrieved, per CU15 — Procesar pago.

## Requirements

### Requirement: Payment records the payment method used to settle it

The system SHALL persist the payment method (`paymentMethod`) supplied when
a payment is processed or edited via `POST /api/v1/payments` (JSON body) or
`PUT /api/v1/payments/{id}`, and SHALL return it on every subsequent
retrieval of that payment. The field is optional free text; the system SHALL
NOT reject a payment for omitting it. The alternate create surface
`POST /api/v1/payments/params` SHALL NOT exist.

#### Scenario: Processing a payment with a payment method

- **WHEN** a payment is processed with a `paymentMethod` value (for example
  "Efectivo") via `POST /api/v1/payments`
- **THEN** the payment is created and the response includes the same
  `paymentMethod` value

#### Scenario: Retrieving a payment reflects the stored payment method

- **WHEN** a previously processed payment with a stored `paymentMethod` is
  fetched by id or listed by `Budget`
- **THEN** the returned payment includes the stored `paymentMethod` value

#### Scenario: Editing a payment's payment method

- **WHEN** an existing payment is updated with a new `paymentMethod` value
- **THEN** the payment's `paymentMethod` is updated to the new value and
  subsequent retrievals return it

#### Scenario: Processing a payment without a payment method

- **WHEN** a payment is processed without a `paymentMethod` value via
  `POST /api/v1/payments`
- **THEN** the payment is created successfully and its `paymentMethod` is
  absent (not defaulted to an invented value)

#### Scenario: Editing the payment method of a payment that does not exist

- **WHEN** a payment update (including a `paymentMethod` value) is submitted
  for an id that does not exist
- **THEN** the system rejects the update with a not-found error and persists
  nothing

### Requirement: Payment create returns Location

Successful `POST /api/v1/payments` SHALL return `201 Created` with a
`Location` header for `/api/v1/payments/{id}`.

#### Scenario: JSON payment create includes Location header

- **WHEN** a client successfully creates a payment via `POST /api/v1/payments`
- **THEN** the response is `201 Created` and `Location` identifies the new
  payment resource
