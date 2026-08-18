# REST API Reference — Notaire Backend

> Closes issue #287 (Swagger/OpenAPI documentation for all endpoints)

**Base URL:** `http://localhost:8080/api/v1`  
**Swagger UI:** `http://localhost:8080/swagger-ui.html`  
**OpenAPI spec:** `http://localhost:8080/v3/api-docs`

---

## Authentication

### POST /usuarios/login

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
| GET | `/personas` | List all persons | CU17 |
| GET | `/personas/{id}` | Get person by ID | CU17 |
| GET | `/personas/buscar` | Search persons (`?nombre=&idTipoIdentificacion=`) | CU41 |
| POST | `/personas` | Create person | CU18 |
| PUT | `/personas/{id}` | Update person | CU21 |
| DELETE | `/personas/{id}` | Delete person | — |

**Persona object:**
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
| GET | `/gestiones` | List all gestiones | CU02 |
| GET | `/gestiones/{id}` | Get gestión by ID | CU13, CU14 |
| GET | `/gestiones/cliente/{idCliente}` | Get gestiones by client | CU19 |
| POST | `/gestiones` | Create gestión | CU02 |
| PUT | `/gestiones/{id}` | Update gestión | CU16 |
| DELETE | `/gestiones/{id}` | Delete gestión | — |

---

## Presupuestos

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/presupuestos` | List all presupuestos | CU01 |
| GET | `/presupuestos/{id}` | Get presupuesto by ID | CU01 |
| GET | `/presupuestos/persona/{idPersona}` | Presupuestos by person | CU45 |
| POST | `/presupuestos` | Create presupuesto | CU01 |
| PUT | `/presupuestos/{id}` | Update presupuesto | CU60 |
| DELETE | `/presupuestos/{id}` | Delete presupuesto | — |

### Items (Presupuesto lines)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/items` | List all items |
| GET | `/items/{id}` | Get item by ID |
| GET | `/items/presupuesto/{idPresupuesto}` | Items by presupuesto |
| POST | `/items` | Create item |
| PUT | `/items/{id}` | Update item |
| DELETE | `/items/{id}` | Delete item |

---

## Pagos

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/pagos` | List all pagos | CU15 |
| GET | `/pagos/{id}` | Get pago by ID | CU15 |
| GET | `/pagos/presupuesto/{idPresupuesto}` | Pagos by presupuesto | CU47 |
| POST | `/pagos` | Create pago | CU15 |
| PUT | `/pagos/{id}` | Update pago | — |
| DELETE | `/pagos/{id}` | Delete pago | — |

**Pago object:**
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
| GET | `/escrituras` | List all escrituras | CU05 |
| GET | `/escrituras/{id}` | Get escritura by ID | CU05 |
| POST | `/escrituras` | Create escritura | CU05 |
| PUT | `/escrituras/{id}` | Update escritura | CU06 |
| DELETE | `/escrituras/{id}` | Delete escritura | — |

### Testimonio

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/testimonio` | List all testimonios | CU07 |
| GET | `/testimonio/{id}` | Get testimonio | CU07 |
| POST | `/testimonio` | Create testimonio | CU07 |
| GET | `/movimiento-testimonio` | List movimientos | CU10, CU11, CU12 |

### Inmueble / Copia

| Method | Path | Description |
|--------|------|-------------|
| GET | `/inmueble` | List inmuebles |
| GET | `/copia` | List copias |

---

## Trámites

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/tramites` | List all tramites | CU03 |
| GET | `/tramites/{id}` | Get tramite by ID | — |
| POST | `/tramites` | Create tramite | CU03 |
| PUT | `/tramites/{id}` | Update tramite | — |
| DELETE | `/tramites/{id}` | Delete tramite | — |

---

## Historial

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/historial` | List all historial | CU13 |
| GET | `/historial/gestion/{idGestion}` | Historial by gestión | CU13 |

---

## Folios

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/folio` | List all folios | CU28 |
| POST | `/folio` | Create folio | CU28 |
| PUT | `/folio/{id}` | Update folio | CU40 |
| DELETE | `/folio/{id}` | Delete folio | CU68 |

### Tipo de Folio

| Method | Path | Description |
|--------|------|-------------|
| GET | `/tipo-folio` | List tipos de folio |

---

## Usuarios

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/usuarios` | List all usuarios | CU23 |
| GET | `/usuarios/{id}` | Get usuario by ID | — |
| POST | `/usuarios` | Create usuario | CU20 |
| PUT | `/usuarios/{id}` | Update usuario | CU21 |
| DELETE | `/usuarios/{id}` | Delete usuario | — |

---

## Administration Catalogs

### Conceptos

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/conceptos` | List all conceptos | CU29 |
| GET | `/conceptos/{id}` | Get concepto | CU29 |
| POST | `/conceptos` | Create concepto | CU29 |
| PUT | `/conceptos/{id}` | Update concepto | CU66 |
| DELETE | `/conceptos/{id}` | Delete concepto | — |

### Tipos de Trámite

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/tipo-tramite` | List all tipos | CU26 |
| GET | `/tipo-tramite/{id}` | Get tipo | — |
| POST | `/tipo-tramite` | Create tipo | CU26 |
| PUT | `/tipo-tramite/{id}` | Update tipo | CU57, CU64 |
| DELETE | `/tipo-tramite/{id}` | Delete tipo | — |

### Tipos de Documento

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/tipo-de-documento` | List all tipos | CU27 |
| POST | `/tipo-de-documento` | Create tipo | CU27 |
| PUT | `/tipo-de-documento/{id}` | Update tipo | CU65 |
| DELETE | `/tipo-de-documento/{id}` | Delete tipo | — |

### Documentos Presentados

| Method | Path | Description |
|--------|------|-------------|
| GET | `/documento-presentado` | List all |
| POST | `/documento-presentado` | Create |
| PUT | `/documento-presentado/{id}` | Update |
| DELETE | `/documento-presentado/{id}` | Delete |

### Estados de Gestión

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/estado-gestion` | List all estados | CU67 |
| POST | `/estado-gestion` | Create estado | CU67 |
| PUT | `/estado-gestion/{id}` | Update estado | CU67 |
| DELETE | `/estado-gestion/{id}` | Delete estado | CU67 |

### Plantillas de Presupuesto (Composite PK)

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/plantilla-presupuestos` | List all plantillas | CU39 |
| GET | `/plantilla-presupuestos/tipo-tramite/{id}` | By tipo de trámite | CU49 |
| POST | `/plantilla-presupuestos` | Create plantilla | CU39 |
| PUT | `/plantilla-presupuestos/tipo-tramite/{tId}/concepto/{cId}` | Update | CU55 |
| DELETE | `/plantilla-presupuestos/tipo-tramite/{tId}/concepto/{cId}` | Delete | — |

---

## Auditoría

| Method | Path | Description |
|--------|------|-------------|
| GET | `/registro-auditoria` | List all audit logs (read-only) |
| GET | `/registro-auditoria/usuario/{idUsuario}` | Audit logs by user |

---

## Reportes (PDF generation)

All endpoints return `application/pdf` and are consumed via `downloadPdf()` in `frontend/src/hooks/useReportes.ts` (not the `apiGet` JSON wrapper).

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/reportes/presupuesto/{idPresupuesto}` | Download presupuesto PDF | CU01, CU39 |
| GET | `/reportes/presupuesto-inmuebles/{idPresupuesto}` | Download presupuesto PDF with inmuebles detail | CU08 |
| GET | `/reportes/lista-documentos-tramite` | List of documents required for a trámite type (`?nombreTipoTramite=`) | CU02 |
| GET | `/reportes/historial-gestion/{idGestion}` | Gestión history report | CU13 |
| GET | `/reportes/documentos-por-vencer/{idDocumentoPresentado}` | Documents nearing expiration | — |
| GET | `/reportes/consultar-deuda-documentos` | Outstanding document debt report (`?numeroGestion=`) | CU16 |
| GET | `/reportes/libro-indice` | Libro Índice report (`?anio=`) | CU24 |
| GET | `/reportes/declaracion-jurada-mensual` | Monthly declaración jurada (`?anio=&mes=`) | CU25 |
| GET | `/reportes/declaracion-jurada-rentas` | Rentas declaración jurada (`?anio=&mes=`) | CU50 |

---

## Suplencias

| Method | Path | Description | Use Cases |
|--------|------|-------------|-----------|
| GET | `/suplencia` | List suplencias | CU22 |
| POST | `/suplencia` | Create suplencia | CU22 |
| PUT | `/suplencia/{id}` | Update suplencia | — |
| DELETE | `/suplencia/{id}` | Delete suplencia | — |

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

See [ERROR-HANDLING-STRATEGY.md](./ERROR-HANDLING-STRATEGY.md) for implementation details.
