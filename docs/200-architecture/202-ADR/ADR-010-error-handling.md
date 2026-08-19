# ADR-010: Error Handling

## Status
Accepted

## Context
A consistent error handling strategy is crucial for both frontend developers (debugging) and end-users (clear error messages).

## Decision
We implement a global exception handling mechanism using Spring's
`GlobalExceptionHandler` (`@ControllerAdvice`).

### Key implementation details:
1.  **Uniform error response** for exceptions that reach `GlobalExceptionHandler`:
    ```json
    {
      "timestamp": "2026-06-10T14:00:00.123456",
      "status": 400,
      "error": "Bad Request",
      "message": "Validation failed",
      "path": "/api/v1/person"
    }
    ```
    `ErrorResponse` also declares `traceId` and `details` (`Map<String, Object>`)
    fields, reserved for future use — no handler populates them today.
2.  **Custom exception hierarchy** (`com.licensis.notaire.exception`):
    *   `NotaireException` (base)
    *   `BusinessValidationException` (400)
    *   `ResourceNotFoundException` (404)
3.  **Logging**: 5xx errors and business exceptions are logged with appropriate severity.

## Consequences
-   **Pros**: Improved API usability, easier frontend error handling, consistent logs.
-   **Cons**: A minority of controllers throw these exceptions and go through
    `GlobalExceptionHandler`; the rest still use ad-hoc `try/catch` blocks
    returning inconsistent, non-enveloped bodies (tracked in issue #579). No
    machine-readable error-code taxonomy exists (`error` is the HTTP reason
    phrase, not a code); I18n via `messages.properties` was not implemented.

See [`BACKEND-ERROR-HANDLING-STRATEGY.md`](../203-design/BACKEND-ERROR-HANDLING-STRATEGY.md)
for the current, ground-truth error-handling reference, including the legacy
ad-hoc pattern, HTTP status mapping, and the login-endpoint/rate-limiting
special cases.
