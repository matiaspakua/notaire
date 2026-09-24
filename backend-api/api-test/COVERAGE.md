# API Test Coverage

Bruno YAML suite — run with `bru run . -r --env Development` from this directory
(backend must be up at `localhost:8080`).

**Current status:** 164 requests / 291 tests passing, twice in a row against the
same database with no leaked rows (verified 2026-09-24, issue #1035). `00-auth`
sorts first (login/rate-limit fixtures other folders depend on).

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
| 10 | `DELETE /{resource}/{id}` for 30 more entities (`Rol`, `TipoDeDocumento`, `TipoDeFolio`, `TipoDeTramite`, `TipoIdentificacion`, `EstadoDeGestion`, `WorkflowDefinition`/`Node`/`Transition`, `Persona`, `Usuario`, `Escritura`, `GestionDeEscritura`, `Presupuesto`, `Testimonio`, `Cuaderno`, `Folio`, `Inmueble`, `MinutaInscripcion`, `MovimientoTestimonio`, `DocumentoPresentado`, `RegistroAuditoria`, `Suplencia`, `Concepto`, `Copia`, plus 5 `@EmbeddedId` join entities) | same silent-delete-via-`isNew()` bug as defect #8, present across every remaining surrogate-key and composite-key entity | implement `Persistable<Integer>` (surrogate keys) or `Persistable<XxxPK>` with `@Transient boolean isNew` + `@PostLoad`/`@PrePersist` (composite keys) on all 30 entities (#957) |
| 11 | `DELETE /plantilla-presupuestos/tipo-tramite/{id}/concepto/{id}` | returned 200 but the row survived: the `getReference` proxy was not removed from the parents' cascade-ALL lists, so flush re-persisted it | load with `em.find` before unlinking and removing (#1036) |

(Earlier, the same campaign fixed `PUT /conceptos`, `GET /folio`,
`DELETE /personas` — merged in PR #416.)

The recurring root cause is **unboxing a nullable wrapper (`Boolean`/`Integer`)
from a DTO into a primitive entity field** on the update path; create defaulted
the value but update did not. Hardened in `setAtributos`.

## Covered resources (full lifecycle)

`[setup] → create → list → get-by-id → [filters] → update → verify → delete → verify-404 → [teardown]`

| Folder | Endpoint | CRUD | Filters / extras | Use Cases |
|--------|----------|------|------------------|-----------|
| 00-auth | `/usuarios/login` | n/a | login, invalid, rate-limit lockout | CU78 |
| audit-records | `/audit-log` | read-only | list | CU73, CU23 |
| budget-templates | `/plantilla-presupuestos` | ✅ | `tipo-tramite/{id}`; own procedure type + concept fixtures | CU26, CU29, CU39, CU55, CU49, CU37, CU57 |
| budgets | `/presupuestos` | ✅ | `persona/{id}`, `buscar?status=` | CU01, CU60, CU45 |
| concepts | `/conceptos` | ✅ | — | CU29, CU66, CU34, CU37 |
| deeds | `/escrituras` | search only | `buscar?number=` | CU62 |
| document-types | `/tipo-de-documento` | ✅ | — | CU27, CU65, CU32, CU38 |
| folio-types | `/tipo-folio` | ✅ | — | CU36, CU68, CU40, CU58 |
| folios | `/folio` | ✅ (no PUT) | own notary fixture | CU48, CU28, CU63, CU33 |
| history | `/historial` | ✅ | `gestion/{id}`; own notary + management fixtures | CU48, CU02, CU11, CU13, CU53 |
| identification-types | `/tipo-identificacion` | ✅ | — | CU17 |
| items | `/items` | ✅ | `presupuesto/{id}`, surcharge without reason rejected | CU01, CU71, CU45 |
| management-statuses | `/estado-gestion` | ✅ | — | CU30, CU67, CU35 |
| payments | `/pagos` | ✅ | `presupuesto/{id}`, `saldo`, `estado`, over-limit 409, receipt PDF | CU01, CU15, CU47, CU45 |
| people | `/people` | ✅ | `search?lastName=`, 409 duplicate | CU17, CU18, CU61, CU46, CU54, CU41 |
| procedure-templates | `/plantilla-tramite` | read-only | list, `tipo-tramite/{id}` | CU79, CU03 |
| procedure-types | `/tipo-tramite` | ✅ | — | CU26, CU64, CU31, CU57 |
| procedures | `/tramites` | ✅ | — | CU02, CU53 |
| properties | `/inmueble` | ✅ | — | CU69 |
| substitutions | `/suplencia` | ✅ | own substitute + replaced person fixtures | CU48, CU22, CU59 |
| users | `/usuarios` | ✅ | login (+/- credentials), case-insensitive login, JWT structure, `persona/{id}` | CU20, CU78, CU21 |

## TODO — resources not yet covered

Tracked as issue #953 (16 controllers with zero Bruno coverage): `CarpetaTramite`,
`Copia`, `Cuaderno`, `DocumentoPresentado`, `Gestion`, `MinutaInscripcion`,
`MovimientoTestimonio`, `PlantillaCostoDocumento`, `ProtocoloAuxiliar`, `Reporte`,
`Rol`, `Testimonio`, `WorkflowDefinition`, `WorkflowNode`, `WorkflowTransition`,
`WorkflowValidation`.
