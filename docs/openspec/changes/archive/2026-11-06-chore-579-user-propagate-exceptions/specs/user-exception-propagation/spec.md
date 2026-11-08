## ADDED Requirements

### Requirement: Unexpected user failures use GlobalExceptionHandler

Create, update, and delete on users SHALL NOT catch generic `Exception` to return an empty 500/409 body; unexpected failures SHALL reach `GlobalExceptionHandler` as HTTP 500 `ErrorResponse`. Login MAY keep its anti-enumeration catch.

#### Scenario: Unexpected user create failure

- **GIVEN** the manage use case throws an unexpected `RuntimeException` on create
- **WHEN** `POST /api/v1/users` is called with a valid body
- **THEN** the response is HTTP 500 with the standard ErrorResponse message
