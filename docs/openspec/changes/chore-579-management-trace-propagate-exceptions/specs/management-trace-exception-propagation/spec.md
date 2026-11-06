## ADDED Requirements

### Requirement: Unexpected management workflow-trace failures use GlobalExceptionHandler

`GET /api/v1/managements/{id}/workflow-trace` SHALL NOT catch generic `Exception` to return an empty 500 body; unexpected failures SHALL reach `GlobalExceptionHandler` as HTTP 500 `ErrorResponse`.

#### Scenario: Unexpected workflow-trace failure

- **GIVEN** `QueryWorkflowTraceUseCase.buildTrace` throws an unexpected `RuntimeException`
- **WHEN** `GET /api/v1/managements/{id}/workflow-trace` is called
- **THEN** the response is HTTP 500 with the standard ErrorResponse message
