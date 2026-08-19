# API Documentation - Notaire Project

Referencia completa de la API REST del proyecto Notaire.

## 📋 Contents

- **[REST-API-REFERENCE.md](REST-API-REFERENCE.md)** — Endpoint-by-endpoint reference with request/response examples and Use Case traceability.
- **[REST-API-ENDPOINT_REGISTRY.md](REST-API-ENDPOINT_REGISTRY.md)** — Full inventory of all 189 REST endpoints, classified by frontend usage.
- **[BACKEND-ERROR-HANDLING-STRATEGY.md](BACKEND-ERROR-HANDLING-STRATEGY.md)** — Exception handling and error response conventions.
- **[FRONTEND-DESIGN-SYSTEM.md](FRONTEND-DESIGN-SYSTEM.md)** — Theme tokens, form patterns, quick reference, form checklist, and UI conventions.
- **[FRONTEND-WORKFLOW-TRACKER.md](FRONTEND-WORKFLOW-TRACKER.md)** — Dashboard animated gestión-workflow visualization (backend endpoint, DTOs, frontend components, tests).

## 🚀 Quick Start

### Swagger UI
```
http://localhost:8080/swagger-ui.html
```

> Swagger UI and `/v3/api-docs` are only reachable when `ENVIRONMENT` (mapped from
> `app.environment`) is **not** `production` — see `SecurityAndCorsConfig` (issue #671).

### Example Request
```bash
curl -X GET "http://localhost:8080/api/v1/presupuestos" \
  -H "Authorization: Bearer <token>"
```

### Example Response

Endpoints return the entity/DTO directly (or a Spring `Page<T>` for paginated
list endpoints) — there is no `{success, data, error}` envelope.

```json
{
  "idPresupuesto": 1,
  "numero": 2024001,
  "fecha": "2024-03-15",
  "estado": "ACTIVO",
  "encabezado": "..."
}
```

### Error Response

```json
{
  "error": "Descripción del error en español"
}
```

See [REST-API-REFERENCE.md](REST-API-REFERENCE.md#error-responses) for the full status code table.

## 🔐 Authentication

`POST /api/v1/usuarios/login` authenticates a user and returns a JWT. Subsequent
requests carry `Authorization: Bearer <token>`, validated by `JwtAuthenticationFilter`.
Details in [REST-API-REFERENCE.md](REST-API-REFERENCE.md#authentication).

## 📊 API Status

| Version | Status | Base URL |
|---------|--------|----------|
| **v1** | Stable | /api/v1 |

## 📖 Navigation

- **[← Back to Docs](../../)** — Volver a índice principal
- **[Development](../../300-development/)** — Cómo desarrollar endpoints
- **[SAD](../201-SAD/sad.md)** — Decisiones de arquitectura
