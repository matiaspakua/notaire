## ADDED Requirements

### Requirement: Folio-type unexpected failures use GlobalExceptionHandler

`FolioTypeController` MUST NOT catch generic `Exception` to return `ErrorResponses` bodies for
unexpected failures. Unexpected failures MUST surface as the standard `ErrorResponse` from
`GlobalExceptionHandler` (HTTP 500, safe message). Domain `FolioTypeException` paths remain
handled by `FolioTypeExceptionHandler`.

#### Scenario: Create folio type unexpected failure returns ErrorResponse
- **GIVEN** `ManageFolioTypesUseCase.create` throws a `RuntimeException`
- **WHEN** `POST /api/v1/folio-types` is invoked with a valid body
- **THEN** the response is HTTP 500 with `ErrorResponse.message` equal to
  `An unexpected error occurred` and does not echo the exception text
