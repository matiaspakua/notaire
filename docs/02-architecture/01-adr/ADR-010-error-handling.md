# ADR-010: Error Handling

## Status
Accepted

## Context
A consistent error handling strategy is crucial for both frontend developers (debugging) and end-users (clear error messages).

## Decision
We will implement a global exception handling mechanism using Spring's **`@ControllerAdvice`**.

### Key implementation details:
1.  **Uniform Error Response**: Every error will return a standard JSON object:
    ```json
    {
      "timestamp": "2024-04-28T12:00:00Z",
      "status": 400,
      "error": "Bad Request",
      "message": "Validation failed",
      "path": "/api/v1/person",
      "code": "VAL_001",
      "details": { "field": "dni", "message": "Must be numeric" }
    }
    ```
2.  **Custom Exception Hierarchy**:
    *   `NotaireException` (Base)
    *   `BusinessValidationException` (400)
    *   `UnauthorizedException` (401)
    *   `ResourceNotFoundException` (404)
    *   `InternalTechnicalException` (500)
3.  **Logging**: All 5xx errors and Business exceptions must be logged with appropriate severity.
4.  **I18n**: Support for internationalized error messages via `messages.properties`.

## Consequences
-   **Pros**: Improved API usability, easier frontend error handling, consistent logs.
-   **Cons**: Requires careful definition of error codes to be useful.
