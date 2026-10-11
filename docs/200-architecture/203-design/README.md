# API Documentation - Notaire Project

Complete reference for the Notaire REST API.

## 📋 Contents

- **[REST-API-REFERENCE.md](REST-API-REFERENCE.md)** — Endpoint-by-endpoint reference with request/response examples and Use Case traceability.
- **[REST-API-ENDPOINT_REGISTRY.md](REST-API-ENDPOINT_REGISTRY.md)** — Full inventory of all 189 REST endpoints, classified by frontend usage.
- **[BACKEND-ERROR-HANDLING-STRATEGY.md](BACKEND-ERROR-HANDLING-STRATEGY.md)** — Exception handling and error response conventions.
- **[FRONTEND-DESIGN-SYSTEM.md](FRONTEND-DESIGN-SYSTEM.md)** — Theme tokens, form patterns, quick reference, form checklist, and UI conventions.
- **[FRONTEND-WORKFLOW-TRACKER.md](FRONTEND-WORKFLOW-TRACKER.md)** — Dashboard animated management-workflow visualization (backend endpoint, DTOs, frontend components, tests).
- **[FRONTEND-OPENAPI-TYPES.md](FRONTEND-OPENAPI-TYPES.md)** — OpenAPI→TypeScript generation, drift check, and hook migration policy (#1260).
- **[CICLO-DE-VIDA.md](CICLO-DE-VIDA.md)** — Canonical lifecycles: gestión, deed, testimony, documents, budget, payment, folders. Mermaid only.
- **[GESTION.md](GESTION.md)** — Case process: use cases, step dependencies, one sequence per step.

## 🚀 Quick Start

### Swagger UI
```
http://localhost:8080/swagger-ui.html
```

> Swagger UI and `/v3/api-docs` are only reachable when `ENVIRONMENT` (mapped from
> `app.environment`) is **not** `production` — see `SecurityAndCorsConfig` (issue #671).

### Example Request
```bash
curl -X GET "http://localhost:8080/api/v1/budgets" \
  -H "Authorization: Bearer <token>"
```

### Example Response

Endpoints return the entity/DTO directly (or a Spring `Page<T>` for paginated
list endpoints) — there is no `{success, data, error}` envelope.

```json
{
  "idBudget": 1,
  "number": 2024001,
  "date": "2024-03-15",
  "status": "BORRADOR",
  "heading": "..."
}
```

### Error Response

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Budget not found",
  "path": "/api/v1/budgets/1"
}
```

See [REST-API-REFERENCE.md](REST-API-REFERENCE.md#error-responses) for the full status code table.

## 🔐 Authentication

`POST /api/v1/users/login` authenticates a user and returns a JWT. Subsequent
requests carry `Authorization: Bearer <token>`, validated by `JwtAuthenticationFilter`.
Details in [REST-API-REFERENCE.md](REST-API-REFERENCE.md#authentication).

## 📊 API Status

| Version | Status | Base URL |
|---------|--------|----------|
| **v1** | Stable | /api/v1 |

## 📖 Navigation

- **[← Back to Docs](../../)** — Documentation index
- **[Development](../../300-development/)** — How to build endpoints
- **[SAD](../201-SAD/sad.md)** — Architecture decisions
