# item-servererror-errorresponse Specification

## Purpose
Item create/update ServerError arms return the standard ErrorResponse body via GlobalExceptionHandler.
## Requirements
### Requirement: Item ServerError returns ErrorResponse

When `ManageItemsUseCase` create returns `SaveResult.ServerError`, the API SHALL respond HTTP 500 with the standard `ErrorResponse` JSON body (not an empty body).

#### Scenario: Create ServerError returns ErrorResponse

- **GIVEN** create returns `SaveResult.ServerError`
- **WHEN** `POST /api/v1/items` is called with a valid body
- **THEN** the response is HTTP 500 with a standard `ErrorResponse` body

