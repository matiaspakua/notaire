# API Error Handling Strategy

This document defines the error response format, HTTP status code mapping, and
error-handling patterns for the Notaire REST API. It is the single canonical
source for this topic — see the note at the bottom for the prior duplicate.

## Error response format (`GlobalExceptionHandler` / `ErrorResponse`)

Controllers that throw `NotaireException` (or its subclasses
`ResourceNotFoundException`, `BusinessValidationException`) or trigger Bean
Validation (`@Valid`/`@Validated`) get a response built by
`com.licensis.notaire.config.GlobalExceptionHandler` from the
`com.licensis.notaire.exception.ErrorResponse` DTO:

```json
{
  "timestamp": "2026-06-10T14:00:00.123456",
  "status": 404,
  "error": "Not Found",
  "message": "GestionDeEscritura with id 42 not found",
  "path": "/api/v1/gestiones/42"
}
```

| Field | Type | Description |
|-------|------|--------------|
| `timestamp` | string | `LocalDateTime.now().toString()` — **not** offset/`Z`-suffixed ISO-8601, no timezone |
| `status` | int | HTTP status code (mirrors the response code) |
| `error` | string | The HTTP reason phrase (e.g. `"Not Found"`, `"Bad Request"`) — **not** a machine-readable code like `RESOURCE_NOT_FOUND`; there is no error-code taxonomy today |
| `message` | string | Human-readable description of the problem |
| `path` | string | Request URI that caused the error |
| `traceId`, `details` | string, map | Declared on the DTO but never populated by any handler today — reserved for future use |

## HTTP status code mapping

| Scenario | Code | Thrown by |
|----------|------|-----------|
| Resource not found | 404 | `ResourceNotFoundException` |
| Business rule violation | 400 | `BusinessValidationException` |
| Bean Validation failure (`@Valid` body, `@Validated` params) | 400 | `MethodArgumentNotValidException` / `ConstraintViolationException` |
| Any other `NotaireException` subclass | per `ex.getStatusCode()` | `NotaireException` |
| Unexpected server error | 500 | any other `Exception` |

## Known gap: most controllers don't go through `GlobalExceptionHandler` yet

As of this writing, only a handful of controllers actually throw
`NotaireException`/`ResourceNotFoundException`/`BusinessValidationException`.
The remaining majority (tracked in issue **#579**) use ad-hoc
`try { ... } catch (Exception e) { ... }` blocks that bypass
`GlobalExceptionHandler` entirely and return inconsistent bodies — anything
from a raw `e.getMessage()` string to a bare `Map.of("error", "...")`, with no
`status`/`timestamp`/`path` envelope. **Do not assume every endpoint returns
the `ErrorResponse` shape above** — check the specific controller, or treat
#579 as the tracking issue for closing this gap.

### Legacy ad-hoc pattern (still the majority — see #579)

```java
@PostMapping
public ResponseEntity<Object> create(@RequestBody Entity entity) {
    try {
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(entity));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
}
```

### Target pattern (new/refactored code should use this)

```java
@GetMapping("/{id}")
public ResponseEntity<Entity> getById(@PathVariable Integer id) {
    return ResponseEntity.ok(repo.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Entity with id " + id + " not found")));
}
```

`GlobalExceptionHandler` converts `NotaireException`/`ResourceNotFoundException`/
`BusinessValidationException` into the `ErrorResponse` envelope automatically —
controllers using this pattern don't need their own try/catch for these cases.

## Bean Validation error format

When `@Valid`/`@Validated` rejects input, `GlobalExceptionHandler` still
returns the same `ErrorResponse` shape, with `message` holding a
semicolon-joined `field: reason` list (see
`GlobalExceptionHandler.handleMethodArgumentNotValid` /
`handleConstraintViolationException`):

```json
{
  "timestamp": "2026-06-10T14:00:00.123456",
  "status": 400,
  "error": "Bad Request",
  "message": "nombre: must not be blank; fechaInicio: must not be null",
  "path": "/api/v1/gestiones"
}
```

## Login endpoint special case

The login endpoint (`POST /api/v1/usuarios/login`) returns HTTP 200 for both
a successful and a credential-failure attempt, using the `valido` field to
signal which one occurred. This intentional design prevents HTTP-level
information leakage (an attacker cannot distinguish "user not found" from
"wrong password" by status code alone). This is a deliberate exception to the
mapping above — do not "fix" it to return 401 without a reviewed API contract
change (see issues #685/#686, which document why an autonomous test addition
asserting 401 here would contradict the current, intentional contract).

Rate limiting is a separate case with its own distinct status: once
`LoginAttemptService` locks an account out, the endpoint returns **429** (Too
Many Requests) with a `message` field describing the lockout — not 200 and
not 423 (Locked), which issue #685 originally assumed before its own
verification against `UsuarioController.login()` corrected it. The frontend
reads this `message` field to show a lockout-specific error instead of a
generic one (issue #756).

## Error logging standards

| Severity | When to use |
|----------|-------------|
| `log.error(msg, e)` | Unexpected exceptions (500-level) |
| `log.warn(msg)` | Business rule violations (wrong password, inactive user, 404/409) |
| `log.debug(msg)` | Diagnostic detail (query results, param values) |

Never log passwords, tokens, or PII. Always include the entity ID and
operation in the message context.

## Frontend error handling

The React frontend handles error responses via React Query mutation
callbacks. `apiPost`/`apiPut`/`apiDelete` in `frontend/src/lib/api-client.ts`
throw `Error` objects on any non-2xx response; callers branch on the message,
not on structured fields, since many endpoints don't yet return the
`ErrorResponse` envelope (see the gap above):

```typescript
try {
  await mutation.mutateAsync(data);
  toast.success("Operación exitosa");
} catch (error) {
  const message = error instanceof Error ? error.message : "Error desconocido";
  toast.error(`Error: ${message}`);
}
```

## Optimistic locking

Entities with concurrent modification risk use `@Version`:

```java
@Version
@Column(name = "version")
private int version = 0;
```

**Key rule**: never pass the raw client request body directly to an update
method — always fetch the current entity first and apply changes onto it, so
the correct `@Version` is carried. See `PlantillaPresupuestoController.java`
for the reference implementation (fix for issue #340).

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
- Issue #579 — tracks migrating the remaining ad-hoc controllers onto `GlobalExceptionHandler`

---

_This document merges and supersedes the former
`docs/03-development/04-code-standards/ERROR-HANDLING-STRATEGY.md`, which
described only the legacy ad-hoc pattern above and used a different, narrower
response shape (`{"error": "..."}`). That file is archived at
`docs/archive/ERROR-HANDLING-STRATEGY-code-standards.md`. See issue #600._
