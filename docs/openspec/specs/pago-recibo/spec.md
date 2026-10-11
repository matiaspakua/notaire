# pago-recibo Specification

## Purpose

Allows issuing, as a PDF, the payment receipt that CU15 requires handing
to the client when a collection management is closed.

## Requirements

### Requirement: Issue a receipt for an existing payment
The system SHALL generate a receipt PDF for a payment that is already
stored, detailing the client who pays, the payment date, the concept or
concepts paid, and the amount paid.

#### Scenario: Receipt for a simple payment
- **WHEN** the receipt is requested for an existing `{idPayment}` whose budget
  has a client and associated items
- **THEN** the system responds `200 OK` with `Content-Type: application/pdf`
  whose content includes the client name, the payment date, the name
  of at least one budget item, and the payment amount

#### Scenario: Receipt for a partial payment or an installment
- **WHEN** the receipt is requested for a payment that is a partial
  installment of a budget with an outstanding balance
- **THEN** the receipt shows that installment's amount (the payment `amount`),
  not the budget total. The download name stays `recibo_pago_{idPayment}.pdf`

#### Scenario: Receipt for a payment that does not exist
- **WHEN** the receipt is requested for an `{idPayment}` that does not exist
- **THEN** the system responds `404 Not Found`
