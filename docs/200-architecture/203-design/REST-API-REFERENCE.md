# REST API Reference — Notaire Backend

> Closes issue #287 (Swagger/OpenAPI documentation for all endpoints)

**Base URL:** `http://localhost:8080/api/v1`  
**Swagger UI:** `http://localhost:8080/swagger-ui.html`  
**OpenAPI spec:** `http://localhost:8080/v3/api-docs`

---

## Authentication

### POST /users/login

Authenticate a user.

**Request body:**
```json
{
  "nombre": "admin",
  "contrasenia": "admin"
}
```

**Response 200:**
```json
{
  "idUsuario": 1,
  "nombre": "admin",
  "tipo": "ADMIN",
  "valido": true,
  "idPersona": null
}
```

---

## Personas (Clientes)

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/people` | List all persons | CU17 |
| GET | `/people/{id}` | Get person by ID | CU17 |
| GET | `/people/search` | Search persons (`?nombre=&idTipoIdentificacion=`) | CU41 |
| POST | `/people` | Create person | CU18 |
| PUT | `/people/{id}` | Update person | CU21 |
| DELETE | `/people/{id}` | Delete person | — |

**Person object:**
```json
{
  "idPersona": 1,
  "nombre": "Juan",
  "apellido": "García",
  "dni": "20123456",
  "cuil": "20201234567",
  "email": "juan@example.com",
  "telefono": "1155667788",
  "domicilio": "Av. Corrientes 1234",
  "esCliente": true
}
```

---

## Gestiones

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/managements` | List all gestiones | CU02 |
| GET | `/managements/{id}` | Get gestión by ID | CU13, CU14 |
| GET | `/managements/client/{idCliente}` | Get gestiones by client | CU19 |
| POST | `/managements` | Create gestión | CU02 |
| PUT | `/managements/{id}` | Update gestión | CU16 |
| DELETE | `/managements/{id}` | Delete gestión | — |

---

## Presupuestos

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/budgets` | List all presupuestos | CU01 |
| GET | `/budgets/{id}` | Get presupuesto by ID | CU01 |
| GET | `/budgets/person/{idPersona}` | Presupuestos by person | CU45 |
| POST | `/budgets` | Create presupuesto | CU01 |
| PUT | `/budgets/{id}` | Update presupuesto | CU60 |
| DELETE | `/budgets/{id}` | Delete presupuesto | — |

### Items (Presupuesto lines)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/items` | List all items |
| GET | `/items/{id}` | Get item by ID |
| GET | `/items/budget/{idBudget}` | Items by presupuesto |
| POST | `/items` | Create item |
| PUT | `/items/{id}` | Update item |
| DELETE | `/items/{id}` | Delete item |

---

## Pagos

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/payments` | List all pagos | CU15 |
| GET | `/payments/{id}` | Get pago by ID | CU15 |
| GET | `/payments/budget/{idBudget}` | Pagos by presupuesto | CU47 |
| POST | `/payments` | Create pago | CU15 |
| PUT | `/payments/{id}` | Update pago | — |
| DELETE | `/payments/{id}` | Delete pago | — |

**Payment object:**
```json
{
  "idPago": 1,
  "monto": 5000.00,
  "fecha": "2025-04-15",
  "metodoPago": "Efectivo",
  "presupuesto": { "idPresupuesto": 3 }
}
```

---

## Escrituras

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/deeds` | List all escrituras | CU05 |
| GET | `/deeds/{id}` | Get escritura by ID | CU05 |
| POST | `/deeds` | Create escritura | CU05 |
| PUT | `/deeds/{id}` | Update escritura | CU06 |
| DELETE | `/deeds/{id}` | Delete escritura | — |

### Testimony

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/testimonies` | List all testimonios | CU07 |
| GET | `/testimonies/{id}` | Get testimonio | CU07 |
| POST | `/testimonies` | Create testimonio | CU07 |
| GET | `/testimony-movements` | List movimientos | CU10, CU11, CU12 |

### Property / Copy

| Method | Path | Description |
|--------|------|-------------|
| GET | `/properties` | List inmuebles |
| GET | `/copies` | List copias |

---

## Trámites

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/procedures` | List all tramites | CU03 |
| GET | `/procedures/{id}` | Get tramite by ID | — |
| POST | `/procedures` | Create tramite | CU03 |
| PUT | `/procedures/{id}` | Update tramite | — |
| DELETE | `/procedures/{id}` | Delete tramite | — |

---

## ManagementHistory

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/history` | List all historial | CU13 |
| GET | `/history/management/{idManagement}` | ManagementHistory by gestión | CU13 |

---

## Folios

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/folios` | List all folios | CU28 |
| POST | `/folios` | Create folio | CU28 |
| PUT | `/folios/{id}` | Update folio | CU40 |
| DELETE | `/folios/{id}` | Delete folio | CU68 |

### Tipo de Folio

| Method | Path | Description |
|--------|------|-------------|
| GET | `/folio-types` | List tipos de folio |

---

## Usuarios

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/users` | List all usuarios | CU23 |
| GET | `/users/{id}` | Get usuario by ID | — |
| POST | `/users` | Create usuario | CU20 |
| PUT | `/users/{id}` | Update usuario | CU21 |
| DELETE | `/users/{id}` | Delete usuario | — |

---

## Administration Catalogs

### Conceptos

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/concepts` | List all conceptos | CU29 |
| GET | `/concepts/{id}` | Get concepto | CU29 |
| POST | `/concepts` | Create concepto | CU29 |
| PUT | `/concepts/{id}` | Update concepto | CU66 |
| DELETE | `/concepts/{id}` | Delete concepto | — |

### Tipos de Trámite

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/procedure-types` | List all tipos | CU26 |
| GET | `/procedure-types/{id}` | Get tipo | — |
| POST | `/procedure-types` | Create tipo | CU26 |
| PUT | `/procedure-types/{id}` | Update tipo | CU57, CU64 |
| DELETE | `/procedure-types/{id}` | Delete tipo | — |

### Tipos de Documento

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/document-types` | List all tipos | CU27 |
| POST | `/document-types` | Create tipo | CU27 |
| PUT | `/document-types/{id}` | Update tipo | CU65 |
| DELETE | `/document-types/{id}` | Delete tipo | — |

### Documentos Presentados

| Method | Path | Description |
|--------|------|-------------|
| GET | `/submitted-documents` | List all |
| POST | `/submitted-documents` | Create |
| PUT | `/submitted-documents/{id}` | Update |
| DELETE | `/submitted-documents/{id}` | Delete |

### Estados de Gestión

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/management-statuses` | List all estados | CU67 |
| POST | `/management-statuses` | Create estado | CU67 |
| PUT | `/management-statuses/{id}` | Update estado | CU67 |
| DELETE | `/management-statuses/{id}` | Delete estado | CU67 |

### Plantillas de Presupuesto (Composite PK)

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/budget-templates` | List all plantillas | CU39 |
| GET | `/budget-templates/procedure-types/{id}` | By tipo de trámite | CU49 |
| POST | `/budget-templates` | Create plantilla | CU39 |
| PUT | `/budget-templates/procedure-types/{tId}/concept/{cId}` | Update | CU55 |
| DELETE | `/budget-templates/procedure-types/{tId}/concept/{cId}` | Delete | — |

---

## Auditoría

| Method | Path | Description |
|--------|------|-------------|
| GET | `/registro-auditoria` | List all audit logs (read-only) |
| GET | `/registro-auditoria/user/{idUsuario}` | Audit logs by user |

---

## Reportes (PDF generation)

All endpoints return `application/pdf` (errors: `ErrorResponse` JSON) and are consumed via `downloadPdf()` in `frontend/src/hooks/useReports.ts` (not the `apiGet` JSON wrapper), except `documentos-por-vencer`, which is API-only (see `contracts/api-reachability-allowlist.yaml`). Since #567 the PDFs are generated in-house from the current schema.

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/reports/budget/{idBudget}` | Download presupuesto PDF (items, total and pending balance) | CU01, CU39 |
| GET | `/reports/budget-properties/{idBudget}` | Download presupuesto PDF with the properties of its trámites | CU01 |
| GET | `/reports/procedure-document-list` | Documents required by a trámite type, from its template (`?procedureTypeName=`) | CU03 |
| GET | `/reports/management-history/{idManagement}` | Gestión data and state history report | CU13 |
| GET | `/reports/documents-due/{idSubmittedDocument}` | Expiry, payment and gestión of one submitted document | CU42 |
| GET | `/reports/document-debt` | Document debt of a gestión number (`?numberManagement=`) | CU09 |
| GET | `/reports/index-book` | Libro Índice report (`?year=`) | CU24 |
| GET | `/reports/monthly-sworn-statement` | Monthly declaración jurada (`?year=&month=`) | CU25 |
| GET | `/reports/tax-sworn-statement` | Rentas declaración jurada (`?year=&month=`) | CU50 |

---

## Suplencias

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/substitutions` | List suplencias | CU22 |
| POST | `/substitutions` | Create suplencia | CU22 |
| PUT | `/substitutions/{id}` | Update suplencia | — |
| DELETE | `/substitutions/{id}` | Delete suplencia | — |

---

## Error Responses

All error responses return a structured JSON body:

```json
{
  "error": "Descripción del error en español"
}
```

| HTTP Status | Meaning |
|-------------|---------|
| 200 OK | Success |
| 400 Bad Request | Invalid input |
| 404 Not Found | Entity does not exist |
| 409 Conflict | Duplicate entity or optimistic lock conflict |
| 500 Internal Server Error | Unexpected server error |

See [BACKEND-ERROR-HANDLING-STRATEGY.md](./BACKEND-ERROR-HANDLING-STRATEGY.md) for implementation details.
