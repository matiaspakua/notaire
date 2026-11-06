### Requirement: User unexpected failures use GlobalExceptionHandler

Unexpected failures from User create/update/delete MUST surface
as the standard `ErrorResponse` from `GlobalExceptionHandler` (HTTP 500, safe message).

#### Scenario: Unexpected create failure
- **WHEN** `ManageUsersUseCase.create` throws an unexpected `RuntimeException`
- **THEN** the API responds 500 with `ErrorResponse` (`message` = An unexpected error occurred)
