# property-exception-propagation Specification

## Purpose
TBD - created by archiving change chore-579-property-propagate-exceptions. Update Purpose after archive.
## Requirements
### Requirement: Property unexpected failures use GlobalExceptionHandler

`PropertyController` MUST NOT catch generic `Exception` on create to return
`ErrorResponses` bodies for unexpected failures. Unexpected failures MUST surface
as the standard `ErrorResponse` from `GlobalExceptionHandler` (HTTP 500, safe message).

#### Scenario: Create property unexpected failure returns ErrorResponse
- **GIVEN** `ManagePropertiesUseCase.create` throws a `RuntimeException`
- **WHEN** `POST /api/v1/properties` is invoked with a valid body
- **THEN** the response is HTTP 500 with `ErrorResponse.message` equal to
  `An unexpected error occurred` and does not echo the exception text

