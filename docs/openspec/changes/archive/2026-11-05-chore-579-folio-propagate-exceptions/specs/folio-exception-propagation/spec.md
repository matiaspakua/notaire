## ADDED Requirements

### Requirement: Folio unexpected failures use GlobalExceptionHandler
Unexpected failures in FolioController SHALL propagate to GlobalExceptionHandler so clients receive a standard ErrorResponse (HTTP 500) instead of an empty body.

#### Scenario: Unexpected create failure
- **WHEN** ManageFoliosUseCase.create throws RuntimeException
- **THEN** the response is 500 with ErrorResponse message "An unexpected error occurred"
