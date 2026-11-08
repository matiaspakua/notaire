# workflow-exception-propagation Specification

## Purpose
TBD - created by archiving change chore-579-workflow-propagate-exceptions. Update Purpose after archive.
## Requirements
### Requirement: Unexpected workflow failures use GlobalExceptionHandler

Create and update on workflow-definition, workflow-node, and workflow-transition SHALL NOT catch generic `Exception` to return `ErrorResponses`; unexpected failures SHALL reach `GlobalExceptionHandler` as HTTP 500 `ErrorResponse`.

#### Scenario: Unexpected workflow-definition create failure

- **GIVEN** the manage use case throws an unexpected `RuntimeException` on create
- **WHEN** `POST /api/v1/workflow-definition` is called with a valid body
- **THEN** the response is HTTP 500 with the standard ErrorResponse message

