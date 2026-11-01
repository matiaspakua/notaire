# payment-exception-propagation Specification

## Purpose
TBD - created by archiving change chore-579-payment-propagate-exceptions. Update Purpose after archive.
## Requirements
### Requirement: Payment unexpected failures use GlobalExceptionHandler

`PaymentController` MUST NOT catch generic `Exception` to return an empty 500. Unexpected
failures MUST surface as the standard `ErrorResponse` from `GlobalExceptionHandler`.

#### Scenario: List payments failure returns ErrorResponse
- **GIVEN** `QueryPaymentsUseCase.findAll` throws a `RuntimeException`
- **WHEN** `GET /api/v1/payments` is invoked
- **THEN** the response is HTTP 500 with `ErrorResponse.message` equal to
  `An unexpected error occurred` and does not echo the exception text

