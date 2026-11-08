# submitted-document-exception-propagation Specification

## Purpose
TBD - created by archiving change chore-579-submitted-document-propagate-exceptions. Update Purpose after archive.
## Requirements
### Requirement: Unexpected submitted-document failures use GlobalExceptionHandler

Create, update, and delete on submitted documents SHALL NOT catch generic `Exception` to return empty error bodies; unexpected failures SHALL reach `GlobalExceptionHandler` as HTTP 500 `ErrorResponse`.

#### Scenario: Unexpected create failure

- **GIVEN** the manage use case throws an unexpected `RuntimeException` on create
- **WHEN** `POST` submitted-documents is called with a valid body
- **THEN** the response is HTTP 500 with the standard ErrorResponse message

