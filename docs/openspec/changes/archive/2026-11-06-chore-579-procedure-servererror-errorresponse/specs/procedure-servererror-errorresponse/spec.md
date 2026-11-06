## ADDED Requirements

### Requirement: Procedure ServerError returns ErrorResponse

When `ManageProceduresUseCase` create returns `SaveResult.ServerError`, the API SHALL respond HTTP 500 with the standard `ErrorResponse` JSON body (not an empty body).

#### Scenario: Create ServerError returns ErrorResponse

- **GIVEN** create returns `SaveResult.ServerError`
- **WHEN** `POST /api/v1/procedures` is called with a valid body
- **THEN** the response is HTTP 500 with a standard `ErrorResponse` body
