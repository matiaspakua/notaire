# REST API Reference — Notaire Backend

> Closes issue #287 (Swagger/OpenAPI documentation for all endpoints)

**Base URL:** `http://localhost:8080/api/v1`  
**Swagger UI:** `http://localhost:8080/swagger-ui.html`  
**OpenAPI spec:** `http://localhost:8080/v3/api-docs`

Paths below are relative to the base URL. The authoritative contract is `backend-api/openapi/openapi.yaml`.

---

## Authentication

### POST /users/login

Authenticate a user. The body is a `DtoUser`. A successful login also sets the HttpOnly session cookie.

**Request body:**
```json
{
  "name": "admin",
  "password": "admin"
}
```

**Response 200:**
```json
{
  "valid": true,
  "idUser": 1,
  "name": "admin",
  "status": true,
  "type": "Escribano",
  "person": {
    "idPerson": 1,
    "firstName": "Ana",
    "lastName": "Perez"
  }
}
```

The response also includes `token` and `version`. `type` keeps the stored administrator value `Escribano` (also `ADMIN` or `ADMINISTRADOR`). A failed login returns `200` with `valid: false`. Too many failures return `429`.

---

## People (clients)

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/people` | List people | CU17 |
| GET | `/people/{id}` | Get a person by ID | CU17 |
| GET | `/people/search` | Search (`firstName`, `lastName`, `identificationNumber`, `idIdentificationType`, `isClient`) | CU61 |
| POST | `/people` | Create a person | CU18 |
| PUT | `/people/{id}` | Update a person | CU21 |
| DELETE | `/people/{id}` | Delete a person | — |

**Person object:**
```json
{
  "id": 1,
  "firstName": "Juan",
  "lastName": "Garcia",
  "identificationNumber": "20123456",
  "taxId": "20201234567",
  "email": "juan@example.com",
  "phone": "1155667788",
  "address": "Av. Corrientes 1234",
  "isClient": true
}
```

---

## Managements

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/managements` | List managements | CU02 |
| GET | `/managements/{id}` | Get a management by ID | CU13, CU14 |
| GET | `/managements/client/{idPerson}` | Managements for a client | CU19 |
| POST | `/managements` | Create a management | CU02 |
| PUT | `/managements/{id}` | Update a management | CU16 |
| DELETE | `/managements/{id}` | Delete a management | — |

Related reads include `/managements/{id}/current-status`, `/managements/{id}/history`, `/managements/{id}/case-summary`, and `/managements/{id}/transition`.

---

## Budgets

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/budgets` | List budgets | CU01 |
| GET | `/budgets/{id}` | Get a budget by ID | CU01 |
| GET | `/budgets/person/{idPerson}` | Budgets for a person | CU45 |
| POST | `/budgets` | Create a budget | CU01 |
| PUT | `/budgets/{id}` | Update a budget | CU60 |
| DELETE | `/budgets/{id}` | Delete a budget | — |

Status values stay the stored codes `BORRADOR`, `PENDIENTE`, `APROBADO`, `RECHAZADO`, and `FACTURADO`.

### Items (budget lines)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/items` | List items |
| GET | `/items/{id}` | Get an item by ID |
| GET | `/items/budget/{idBudget}` | Items for a budget |
| POST | `/items` | Create an item |
| PUT | `/items/{id}` | Update an item |
| DELETE | `/items/{id}` | Delete an item |

Item types stay `NORMAL`, `DESCUENTO`, and `RECARGO`.

---

## Payments

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/payments` | List payments | CU15 |
| GET | `/payments/{id}` | Get a payment by ID | CU15 |
| GET | `/payments/budget/{idBudget}` | Payments for a budget | CU47 |
| POST | `/payments` | Create a payment | CU15 |
| PUT | `/payments/{id}` | Update a payment | — |
| DELETE | `/payments/{id}` | Delete a payment | — |

**Payment request:**
```json
{
  "idBudget": 3,
  "amount": 5000.00,
  "date": "2025-04-15T00:00:00Z",
  "paymentMethod": "Cash"
}
```

---

## Deeds

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/deeds` | List deeds | CU05 |
| GET | `/deeds/{id}` | Get a deed by ID | CU05 |
| POST | `/deeds` | Create a deed | CU05 |
| PUT | `/deeds/{id}` | Update a deed | CU06 |
| DELETE | `/deeds/{id}` | Delete a deed | — |

Deed statuses stay the stored labels `Sin Firmar`, `Firmada`, `Anulada`, `No Paso`, and `Inscripta`.

### Testimony

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/testimonies` | List testimonies | CU07 |
| GET | `/testimonies/{id}` | Get a testimony | CU07 |
| POST | `/testimonies` | Create a testimony | CU07 |
| GET | `/testimony-movements` | List testimony movements | CU10, CU11, CU12 |

### Property / Copy

| Method | Path | Description |
|--------|------|-------------|
| GET | `/properties` | List properties |
| GET | `/copies` | List copies |

---

## Procedures

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/procedures` | List procedures | CU03 |
| GET | `/procedures/{id}` | Get a procedure by ID | — |
| POST | `/procedures` | Create a procedure | CU03 |
| PUT | `/procedures/{id}` | Update a procedure | — |
| DELETE | `/procedures/{id}` | Delete a procedure | — |

---

## Management history

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/history` | List history records | CU13 |
| GET | `/history/management/{idManagement}` | History for a management | CU13 |

---

## Folios

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/folios` | List folios | CU28 |
| POST | `/folios` | Create a folio | CU28 |
| PUT | `/folios/{id}` | Update a folio | CU40 |
| DELETE | `/folios/{id}` | Delete a folio | CU68 |

Folio statuses stay the stored labels `Nuevo`, `Utilizado`, and `Errose`.

### Folio type

| Method | Path | Description |
|--------|------|-------------|
| GET | `/folio-types` | List folio types |

---

## Users

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/users` | List users | CU23 |
| GET | `/users/{id}` | Get a user by ID | — |
| POST | `/users` | Create a user | CU20 |
| PUT | `/users/{id}` | Update a user | CU21 |
| DELETE | `/users/{id}` | Delete a user | — |

---

## Administration catalogs

### Concepts

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/concepts` | List concepts | CU29 |
| GET | `/concepts/{id}` | Get a concept | CU29 |
| POST | `/concepts` | Create a concept | CU29 |
| PUT | `/concepts/{id}` | Update a concept | CU66 |
| DELETE | `/concepts/{id}` | Delete a concept | — |

### Procedure types

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/procedure-types` | List procedure types | CU26 |
| GET | `/procedure-types/{id}` | Get a procedure type | — |
| POST | `/procedure-types` | Create a procedure type | CU26 |
| PUT | `/procedure-types/{id}` | Update a procedure type | CU57, CU64 |
| DELETE | `/procedure-types/{id}` | Delete a procedure type | — |

### Document types

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/document-types` | List document types | CU27 |
| POST | `/document-types` | Create a document type | CU27 |
| PUT | `/document-types/{id}` | Update a document type | CU65 |
| DELETE | `/document-types/{id}` | Delete a document type | — |

### Submitted documents

| Method | Path | Description |
|--------|------|-------------|
| GET | `/submitted-documents` | List submitted documents |
| POST | `/submitted-documents` | Create a submitted document |
| PUT | `/submitted-documents/{id}` | Update a submitted document |
| DELETE | `/submitted-documents/{id}` | Delete a submitted document |

### Management statuses

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/management-statuses` | List management statuses | CU67 |
| POST | `/management-statuses` | Create a management status | CU67 |
| PUT | `/management-statuses/{id}` | Update a management status | CU67 |
| DELETE | `/management-statuses/{id}` | Delete a management status | CU67 |

### Budget templates (composite key)

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/budget-templates` | List budget templates | CU39 |
| GET | `/budget-templates/procedure-types/{idProcedureType}` | Templates for a procedure type | CU49 |
| POST | `/budget-templates` | Create a budget template | CU39 |
| PUT | `/budget-templates/procedure-types/{idProcedureType}/concept/{idConcept}` | Update a template | CU55 |
| DELETE | `/budget-templates/procedure-types/{idProcedureType}/concept/{idConcept}` | Delete a template | — |

---

## Audit

| Method | Path | Description |
|--------|------|-------------|
| GET | `/audit-log` | List audit records (read only) |
| GET | `/audit-log/user/{idUser}` | Audit records for a user |

---

## Reports (PDF)

These endpoints return `application/pdf` (errors are `ErrorResponse` JSON). The frontend downloads them with `downloadPdf()` in `frontend/src/hooks/useReports.ts`. Since #567 the PDFs are generated in-house. Download filenames stay the historical Spanish names.

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/reports/budget/{idBudget}` | Budget PDF (items, total, and pending balance) | CU01, CU39 |
| GET | `/reports/budget-properties/{idBudget}` | Budget PDF including the properties of its procedures | CU01 |
| GET | `/reports/procedure-document-list` | Documents required by a procedure type (`?procedureTypeName=`) | CU03 |
| GET | `/reports/management-history/{idManagement}` | Management data and status history | CU13 |
| GET | `/reports/documents-due/{idSubmittedDocument}` | Expiry, payment, and management of one submitted document | CU42 |
| GET | `/reports/document-debt` | Document debt for a management number (`?numberManagement=`) | CU09 |
| GET | `/reports/index-book` | Index book (`?year=`) | CU24 |
| GET | `/reports/monthly-sworn-statement` | Monthly sworn statement (`?year=&month=`) | CU25 |
| GET | `/reports/tax-sworn-statement` | Tax sworn statement (`?year=&month=`) | CU50 |

---

## Substitutions

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/substitutions` | List substitutions | CU22 |
| POST | `/substitutions` | Create a substitution | CU22 |
| PUT | `/substitutions/{id}` | Update a substitution | — |
| DELETE | `/substitutions/{id}` | Delete a substitution | — |

---

## Error responses

Error bodies use `ErrorResponse`:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "heading: is required",
  "path": "/api/v1/budgets",
  "timestamp": "2026-10-11T00:00:00Z"
}
```

| HTTP Status | Meaning |
|-------------|---------|
| 200 OK | Success |
| 400 Bad Request | Invalid input |
| 404 Not Found | The entity does not exist |
| 409 Conflict | Duplicate entity or optimistic lock conflict |
| 500 Internal Server Error | Unexpected server error |

See [BACKEND-ERROR-HANDLING-STRATEGY.md](./BACKEND-ERROR-HANDLING-STRATEGY.md) for implementation details.
