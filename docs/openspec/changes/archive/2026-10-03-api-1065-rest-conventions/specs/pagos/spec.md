# pagos Delta Specification

## MODIFIED Requirements

### Requirement: Payment records the método de pago used to settle it

The system SHALL persist the payment method (`metodoPago` / `paymentMethod`)
supplied when a payment is processed or edited via `POST /api/v1/pagos` (JSON
body) or `PUT /api/v1/pagos/{id}`, and SHALL return it on every subsequent
retrieval of that payment. The field is optional free text; the system SHALL NOT
reject a payment for omitting it. The alternate create surface
`POST /api/v1/pagos/params` SHALL NOT exist.

#### Scenario: Processing a payment with a payment method

- **WHEN** a payment is processed with a `metodoPago` value (e.g. "Efectivo")
  via `POST /api/v1/pagos`
- **THEN** the payment is created and the response includes the same
  `metodoPago` value

#### Scenario: Retrieving a payment reflects the stored payment method

- **WHEN** a previously processed payment with a stored `metodoPago` is fetched
  by ID or listed by presupuesto
- **THEN** the returned payment includes the stored `metodoPago` value

#### Scenario: Editing a payment's payment method

- **WHEN** an existing payment is updated with a new `metodoPago` value
- **THEN** the payment's `metodoPago` is updated to the new value and subsequent
  retrievals return it

#### Scenario: Processing a payment without a payment method

- **WHEN** a payment is processed without a `metodoPago` value via
  `POST /api/v1/pagos`
- **THEN** the payment is created successfully and its `metodoPago` is absent
  (not defaulted to an invented value)

#### Scenario: Editing the payment method of a non-existent payment

- **WHEN** a payment update (including a `metodoPago` value) is submitted for an
  ID that does not exist
- **THEN** the system rejects the update with a not-found error and persists
  nothing

## ADDED Requirements

### Requirement: Payment create returns Location

Successful `POST /api/v1/pagos` SHALL return `201 Created` with a `Location`
header for `/api/v1/pagos/{id}`.

#### Scenario: JSON payment create includes Location header

- **WHEN** a client successfully creates a payment via `POST /api/v1/pagos`
- **THEN** the response is `201 Created` and `Location` identifies the new
  payment resource
