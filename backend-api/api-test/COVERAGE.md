# API Test Coverage

Bruno YAML suite — run with `bru run . -r --env Developmen` from this directory
(backend must be up at `localhost:8080`).

**Current status:** `72 requests · 124/124 tests passing` across 12 resources.

## Backend defects found and fixed via this suite

| # | Endpoint | Defect | Fix |
|---|----------|--------|-----|
| 1 | `POST /tipo-de-documento` | NPE unboxing null `Boolean vence` | `DtoTipoDeDocumento.isVence()` null-safe |
| 2 | `POST /tipo-identificacion` | `null value in column "caracteres"` (entity didn't map the NOT-NULL column) | added `caracteres` field to `TipoIdentificacion` |
| 3 | `PUT /tipo-tramite/{id}` | NPE on null `asociaInmuebles` / `version` | `TipoDeTramite.setAtributos` null-safe (booleans + preserve version) |
| 4 | `PUT /tipo-de-documento/{id}` | NPE on null `habilitado` / `version` | `TipoDeDocumento.setAtributos` null-safe |
| 5 | `GET /usuarios/persona/{id}` | `NonUniqueResult` 500 when a persona has >1 usuario | `findFirstByFkIdPersonaIdPersona` |
| 6 | `PUT /usuarios/{id}` | omitting `contrasenia` → NOT-NULL 500 | preserve stored password when omitted |

(Earlier, the same campaign fixed `PUT /conceptos`, `GET /folio`,
`DELETE /personas` — merged in PR #416.)

The recurring root cause is **unboxing a nullable wrapper (`Boolean`/`Integer`)
from a DTO into a primitive entity field** on the update path; create defaulted
the value but update did not. Hardened in `setAtributos`.

## Covered resources (full lifecycle)

`create → list → get-by-id → [filters] → update → verify → delete → verify-404`

| Resource | CRUD | Filters / extras |
|----------|------|------------------|
| conceptos | ✅ | — |
| tipo-tramite | ✅ | — |
| estado-gestion | ✅ | — |
| tipo-folio | ✅ | — |
| tipo-documento | ✅ | — |
| tipo-identificacion | ✅ | — |
| personas | ✅ | `?q=` search |
| usuarios | ✅ | login (+/- creds), `persona/{id}` |
| presupuestos | ✅ | `persona/{id}`, `buscar` |
| folios | ✅ (no PUT) | regression for #416 serialization |
| auth | n/a | login + negative |
| auditoria | read-only | list |

## TODO — resources not yet covered (and known issues)

These were removed as malformed stubs; they need lifecycle authoring. Several
`POST`s currently 500 on **missing-required-field** payloads (should arguably be
`400` validation) — flagged for a follow-up:

| Resource | Endpoints | Notes |
|----------|-----------|-------|
| gestiones | CRUD + `cliente/{id}`, `numero/{n}`, `{id}/estado-actual` | create needs `numero, fechaInicio, encabezado, fkIdPersonaEscribano, fkIdEstadoGestion` |
| escrituras | CRUD + `buscar`, `escribanos-disponibles` | create needs `numero, fechaEscrituracion, cuerpo, estado` |
| suplencia | CRUD | create needs `fechaInicio, fkIdSuplantado, fkIdSuplente` |
| inmueble | CRUD | create 500 to investigate |
| copia | CRUD | needs `fkIdTestimonio` (→ testimonio → escritura chain) |
| items | CRUD + `presupuesto/{id}` | needs presupuesto + concepto |
| pagos | CRUD + `fecha`, `presupuesto/{id}`, `saldo` | needs presupuesto |
| plantilla-presupuestos | CRUD (composite key) | covered in UI; add API lifecycle |
| plantilla-tramite, tramites, historial, testimonio, movimiento-testimonio, documento-presentado, reportes | list/CRUD | |

**Recommendation:** add input validation (`@Valid` + `400`) so missing-field
`POST`s return `400` instead of `500`, then author the lifecycles above.
