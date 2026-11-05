# document-type-exception-propagation Specification

## Purpose
TBD - created by archiving change chore-579-document-type-propagate-exceptions. Update Purpose after archive.
## Requirements
### Requirement: DocumentType unexpected failures use GlobalExceptionHandler

`DocumentTypeController` MUST NOT catch generic `Exception` on create to return
`ErrorResponses` bodies for unexpected failures. Unexpected failures MUST surface
as the standard `ErrorResponse` from `GlobalExceptionHandler` (HTTP 500, safe message).

#### Scenario: Create document-type unexpected failure returns ErrorResponse
- **GIVEN** `ManageDocumentTypesUseCase.create` throws a `RuntimeException`
- **WHEN** `POST /api/v1/document-types` is invoked with a valid body
- **THEN** the response is HTTP 500 with `ErrorResponse.message` equal to
  `An unexpected error occurred` and does not echo the exception text

