# API Error Handling Strategy

This document defines the error response format, HTTP status code mapping, and error-handling patterns for the Notaire REST API.

## Error response format

All error responses use a consistent JSON structure:

```json
{
  "error": "RESOURCE_NOT_FOUND",
  "message": "GestionDeEscritura with id 42 not found",
  "timestamp": "2026-06-10T14:00:00Z",
  "path": "/api/v1/gestiones/42",
  "status": 404
}
```

| Field | Type | Description |
|-------|------|-------------|
| `error` | string | Machine-readable error code (UPPER_SNAKE_CASE) |
| `message` | string | Human-readable description of the problem |
| `timestamp` | ISO-8601 | When the error occurred |
| `path` | string | Request URI that caused the error |
| `status` | int | HTTP status code (mirrors the response code) |

## HTTP status code mapping

| Scenario | Code | Error code |
|----------|------|------------|
| Resource not found | 404 | `RESOURCE_NOT_FOUND` |
| Validation failed (missing/invalid field) | 400 | `VALIDATION_ERROR` |
| Constraint violation (FK, unique) | 409 | `CONSTRAINT_VIOLATION` |
| Unauthorized (future: token missing) | 401 | `UNAUTHORIZED` |
| Forbidden (future: insufficient role) | 403 | `FORBIDDEN` |
| Unexpected server error | 500 | `INTERNAL_ERROR` |
| Method not allowed | 405 | `METHOD_NOT_ALLOWED` |

## Current implementation patterns

Controllers follow a consistent try/catch pattern:

```java
@GetMapping("/{id}")
public ResponseEntity<?> getById(@PathVariable Integer id) {
    return repo.findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
}

@PostMapping
public ResponseEntity<?> create(@RequestBody Entity entity) {
    try {
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(entity));
    } catch (Exception e) {
        log.error("Failed to create entity", e);
        return ResponseEntity.internalServerError().build();
    }
}

@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable Integer id) {
    if (!repo.existsById(id)) return ResponseEntity.notFound().build();
    try {
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    } catch (Exception e) {
        log.error("Failed to delete id {}", id, e);
        return ResponseEntity.status(HttpStatus.CONFLICT).build(); // FK constraint
    }
}
```

## Validation error format

When `@Valid` / `@Validated` rejects input, the response must include field-level detail:

```json
{
  "error": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "timestamp": "2026-06-10T14:00:00Z",
  "path": "/api/v1/gestiones",
  "status": 400,
  "violations": [
    { "field": "nombre", "message": "must not be blank" },
    { "field": "fechaInicio", "message": "must not be null" }
  ]
}
```

To produce this, add a `@RestControllerAdvice` that catches `MethodArgumentNotValidException`:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<Map<String, String>> violations = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> Map.of("field", fe.getField(), "message", fe.getDefaultMessage()))
            .toList();
        return ResponseEntity.badRequest().body(Map.of(
            "error", "VALIDATION_ERROR",
            "message", "Request validation failed",
            "path", req.getRequestURI(),
            "violations", violations
        ));
    }
}
```

## Login endpoint special case

The login endpoint (`POST /api/v1/usuarios/login`) returns HTTP 200 in all cases, using the `valido` field to signal success or failure. This intentional design prevents HTTP-level information leakage (an attacker cannot distinguish "user not found" from "wrong password" by status code alone).

## Error logging standards

| Severity | When to use |
|----------|-------------|
| `log.error(msg, e)` | Unexpected exceptions (500-level) |
| `log.warn(msg)` | Business rule violations (wrong password, inactive user) |
| `log.debug(msg)` | Diagnostic detail (query results, param values) |

Never log passwords, tokens, or PII. Always include the entity ID and operation in the message context.

## Testing error paths

Every controller must have tests for:
- `404` when the entity does not exist
- `409` when a FK/unique constraint prevents deletion
- `500` when the repository throws an unexpected exception
- `400` when required fields are missing (once `@Valid` is applied)

See `AdditionalControllersTest` for the established test pattern.

## Related documentation

- `docs/04-operations/03-security/INPUT-VALIDATION-STRATEGY.md`
- `docs/04-operations/03-security/API-AUTHENTICATION-GUIDE.md`
- `.claude/rules/programming.md` — Error Handling section
