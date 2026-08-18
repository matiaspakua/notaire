# Custom Exception and Error Handling Strategy

> **DEPRECATED (2026-08-02)**: superseded by
> [`docs/200-architecture/203-design/BACKEND-ERROR-HANDLING-STRATEGY.md`](../../200-architecture/203-design/BACKEND-ERROR-HANDLING-STRATEGY.md),
> which merges this document's content with the actual `GlobalExceptionHandler`
> implementation and corrects the response shape below (it describes only the
> legacy ad-hoc `try/catch` pattern, not the target `ErrorResponse` envelope).
> Kept for historical reference — see issue #600.

> Closes issue #299

## Overview

This document describes the standard error handling patterns for the Notaire backend API. All exceptions must produce predictable HTTP responses with structured JSON error bodies.

## HTTP Status Code Mapping

| Scenario | HTTP Status | Exception type |
|----------|-------------|----------------|
| Entity not found | 404 Not Found | `NonexistentEntityException` |
| Duplicate / already exists | 409 Conflict | `PreexistingEntityException` |
| Optimistic lock conflict | 409 Conflict | `OptimisticLockException` |
| Invalid input | 400 Bad Request | `IllegalArgumentException` |
| Authorization failure | 403 Forbidden | — |
| General server error | 500 Internal Server Error | `Exception` |

## Structured Error Response Body

All error responses return a JSON object with at least an `error` key:

```json
{
  "error": "Human-readable description in Spanish"
}
```

Additional fields may be included for validation errors:

```json
{
  "error": "Validación fallida",
  "campos": {
    "nombre": "El campo nombre es obligatorio"
  }
}
```

## Standard Controller Pattern

Every REST controller must follow this error handling template:

```java
@PostMapping
public ResponseEntity<?> create(@RequestBody MyEntity entity) {
    try {
        getJpaController().create(entity);
        return ResponseEntity.ok().build();
    } catch (PreexistingEntityException e) {
        log.warn("Entidad ya existe", e);
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("error", "La entidad ya existe"));
    } catch (Exception e) {
        log.error("Error al crear entidad", e);
        return ResponseEntity.internalServerError()
            .body(Map.of("error", "Error interno al crear la entidad"));
    }
}

@PutMapping("/{id}")
public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody MyEntity entity) {
    try {
        // CRITICAL: fetch current entity first to carry correct @Version
        MyEntity current = getJpaController().find(id);
        if (current == null) {
            return ResponseEntity.notFound().build();
        }
        // Apply changes to fetched entity (not the raw request body)
        current.setNombre(entity.getNombre());
        getJpaController().edit(current);
        return ResponseEntity.ok().build();
    } catch (OptimisticLockException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("error", "Conflicto de concurrencia. Recargue y vuelva a intentarlo."));
    } catch (NonexistentEntityException e) {
        return ResponseEntity.notFound().build();
    } catch (Exception e) {
        // Unwrap cause chain for wrapped OptimisticLockException
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof OptimisticLockException) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Conflicto de concurrencia detectado."));
            }
            cause = cause.getCause();
        }
        log.error("Error al actualizar entidad", e);
        return ResponseEntity.internalServerError()
            .body(Map.of("error", "Error interno al actualizar la entidad"));
    }
}
```

## Optimistic Locking

All entities with concurrent modification risk should use `@Version`:

```java
@Version
@Column(name = "version")
private int version = 0;
```

**Key rule**: Never pass the raw client request body directly to `edit()`. Always fetch the current entity first. See `PlantillaPresupuestoController.java` for the reference implementation (fix for issue #340).

## Logging Standards

| Level | When to use |
|-------|-------------|
| `SEVERE` | Unexpected errors (500 responses) |
| `WARNING` | Expected business errors (409, 404) |
| `INFO` | Successful business operations |
| `FINE` | Debug information |

```java
private static final Logger LOG = Logger.getLogger(MyController.class.getName());
// ...
LOG.log(Level.SEVERE, "Error inesperado en MyController.create", e);
LOG.log(Level.WARNING, "Conflicto de concurrencia detectado", e);
```

## Frontend Error Handling

The React frontend handles error responses via React Query mutation callbacks:

```typescript
try {
  await mutation.mutateAsync(data);
  toast.success("Operación exitosa");
} catch (error) {
  // Backend returns { error: "..." } body
  const message = error instanceof Error ? error.message : "Error desconocido";
  toast.error(`Error: ${message}`);
}
```

The `apiPost`, `apiPut`, `apiDelete` functions in `src/lib/api-client.ts` throw `Error` objects whose message includes the HTTP status code:

```typescript
throw new Error(`POST /presupuestos → 409`);
```

## References

- `PlantillaPresupuestoController.java` — reference implementation for concurrency fix
- `NonexistentEntityException.java`, `PreexistingEntityException.java` — custom exception classes
- Issue #340 — PlantillaPresupuesto concurrency fix
