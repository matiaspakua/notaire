# API Test Coverage

Bruno YAML suite — run with `bru run . -r --env Developmen` from this directory
(backend must be up at `localhost:8080`).

**Current status:** 149 requests / 266 tests passing across the full suite
(verified 2026-09-05, issue #952). `00-auth` was renamed from `auth/` so it
sorts first (login/rate-limit fixtures other suites depend on).

## Backend defects found and fixed via this suite

| # | Endpoint | Defect | Fix |
|---|----------|--------|-----|
| 1 | `POST /tipo-de-documento` | NPE unboxing null `Boolean vence` | `DtoTipoDeDocumento.isVence()` null-safe |
| 2 | `POST /tipo-identificacion` | `null value in column "caracteres"` (entity didn't map the NOT-NULL column) | added `caracteres` field to `TipoIdentificacion` |
| 3 | `PUT /tipo-tramite/{id}` | NPE on null `asociaInmuebles` / `version` | `TipoDeTramite.setAtributos` null-safe (booleans + preserve version) |
| 4 | `PUT /tipo-de-documento/{id}` | NPE on null `habilitado` / `version` | `TipoDeDocumento.setAtributos` null-safe |
| 5 | `GET /usuarios/persona/{id}` | `NonUniqueResult` 500 when a persona has >1 usuario | `findFirstByFkIdPersonaIdPersona` |
| 6 | `PUT /usuarios/{id}` | omitting `contrasenia` → NOT-NULL 500 | preserve stored password when omitted |
| 7 | `POST/PUT /items` | `fkIdPresupuesto` silently dropped (`@JsonIgnore` blocked the field on write, not just read) | `@JsonProperty(access = WRITE_ONLY)` |
| 8 | `DELETE /historial/{id}`, `/items/{id}`, `/pagos/{id}`, `/tramites/{id}` | delete silently no-op'd for rows loaded fresh from the DB (Spring Data's default `isNew()` misreads a primitive `@Version` of 0 as "new") | implement `Persistable<Integer>` with an explicit `isNew()` |
| 9 | `DELETE /historial/{id}` | delete silently cancelled by Hibernate's cascade on the stale `EstadoDeGestion.historialList` collection | unlink the entity from that collection before `repository.delete()` |

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
| suplencias | ✅ | fixture setup (`create-suplente`, `create-suplantado`) |
| plantilla-presupuesto | ✅ | `get-by-tipo-tramite` |
| plantilla-tramite | read-only | list, `get-by-tipo-tramite` |
| escrituras | search only | `buscar` (CU62) — no create/update/delete |
| inmueble | ✅ | — |
| historial | ✅ | `gestion/{id}` |
| items | ✅ | `presupuesto/{id}`, budget-FK happy path |
| pagos | ✅ | `presupuesto/{id}`, `saldo`, `estado`, over-limit 409 |
| tramites | ✅ | — |

## TODO — resources not yet covered

Tracked as issue #953 (16 controllers with zero Bruno coverage): `CarpetaTramite`,
`Copia`, `Cuaderno`, `DocumentoPresentado`, `Gestion`, `MinutaInscripcion`,
`MovimientoTestimonio`, `PlantillaCostoDocumento`, `ProtocoloAuxiliar`, `Reporte`,
`Rol`, `Testimonio`, `WorkflowDefinition`, `WorkflowNode`, `WorkflowTransition`,
`WorkflowValidation`.
