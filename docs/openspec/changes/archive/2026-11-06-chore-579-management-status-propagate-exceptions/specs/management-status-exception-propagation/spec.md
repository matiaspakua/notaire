## ADDED Requirements

### Requirement: Unexpected management-status failures use GlobalExceptionHandler

Create and update on `/api/v1/management-statuses` SHALL NOT catch generic `Exception` to return `ErrorResponses`; unexpected failures SHALL reach `GlobalExceptionHandler` as HTTP 500 `ErrorResponse`.

#### Scenario: Unexpected create failure

- **GIVEN** the manage use case throws an unexpected `RuntimeException` on create
- **WHEN** `POST /api/v1/management-statuses` is called with a valid body
- **THEN** the response is HTTP 500 with the standard ErrorResponse message
