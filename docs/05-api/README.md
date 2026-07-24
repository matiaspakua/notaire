# API Documentation - Notaire Project

Referencia completa de la API REST del proyecto Notaire.

## 📋 Contents

### [01. API Overview](01-overview/)
**Visión general de la API**

- Base URL: `http://localhost:8080/api/v1`
- Authentication method
- Response format
- Error handling
- Rate limiting

### [02. Endpoints](02-endpoints/)
**Referencia de todos los endpoints**

- **Presupuestos**: CRUD operations
- **Personas**: CRUD operations
- **Escrituras**: CRUD operations
- **Gestiones**: CRUD operations
- **Usuarios**: CRUD operations
- **Reportes**: Report generation

### [03. Schemas](03-schemas/)
**DTOs y estructuras de datos**

- Request DTOs
- Response DTOs
- Error responses

## 🚀 Quick Start

### Get Swagger UI
```
http://localhost:8080/swagger-ui.html
```

> Swagger UI and `/v3/api-docs` are only reachable when `ENVIRONMENT` (mapped to
> `app.environment`) is **not** `production` — see `SecurityAndCorsConfig` (issue #671).

### Example Request
```bash
curl -X GET "http://localhost:8080/api/v1/presupuestos" \
  -H "Content-Type: application/json"
```

### Example Response
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "numero": "PRES-2024-001",
      "estado": "ACTIVO"
    }
  ],
  "error": null
}
```

## 🔐 Authentication

(Details in 01-overview/)

## 📊 API Status

| Version | Status | Base URL |
|---------|--------|----------|
| **v1** | Stable | /api/v1 |
| **v2** | Planning | - |

## 📖 Navigation

- **[← Back to Docs](../)** - Volver a índice principal
- **[Development](../03-development/)** - Cómo desarrollar endpoints
- **[Architecture](../02-architecture/)** - Decisiones de API

