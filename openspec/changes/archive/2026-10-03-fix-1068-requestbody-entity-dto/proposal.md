# Fix mass assignment: bind request DTOs instead of JPA entities

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1068 |
| Use Case | CU78 — Security, Privacy and Compliance |
| Branch | `cursor/fix-1068-requestbody-dto-69d3` |
| Gate 1 status | passed |

## Objetivo

Thirteen REST controllers accept JPA `@Entity` types as `@RequestBody`, so clients
can overwrite server-managed fields (id, version, status, audit, associations).
Bean Validation barely runs. This change replaces entity binding with validated
request DTOs/records and response DTOs, closing the mass-assignment gap under CU78.

## What Changes

- Introduce nested request records (client-writable fields only) with `@Valid`
  constraints on create/update for: Property, IdentificationType, Management
  (DeedManagement CRUD), Deed, ProcedureTemplate, Person, Item, Substitution,
  Copy, History, Budget, BudgetTemplate.
- Map requests onto entities in the controller/service layer; path id and
  existing `@Version` always win over any client-supplied id/version.
- Return response DTOs/records from those endpoints (no raw entity as the
  documented OpenAPI write/read contract for those operations).
- **BREAKING (intentional security):** `POST /api/v1/audit-log` removed —
  audit rows are written only by `AuditoriaAspect` (complements #1060). Clients
  that forged audit rows via entity POST must stop.
- Update Bruno request bodies and OpenAPI annotations for the affected routes.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| API write endpoints MUST NOT bind JPA entities as request bodies | CU78 / `.claude/rules/refactoring.md` | Made explicit |
| Clients MUST NOT set server-managed fields (id, version, audit timestamps, forged associations beyond FK ids) | CU78 | New (normative in delta spec) |
| Mutating audit-log entries via public API MUST NOT be allowed | CU78 / CU23 / #1060 | Made explicit |
| Write payloads MUST be validated with Bean Validation (`@Valid`) | CU78 | Made explicit |

## Capabilities

### New Capabilities

- `api-request-dto-binding`: Controllers accept only validated request DTOs; responses use DTOs; mass assignment of server-managed fields is rejected by construction.

### Modified Capabilities

None at the permanent-spec requirement level beyond this new capability.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Controllers, mappers, tests; AuditRecord POST removed |
| `frontend` | no | JSON shapes keep existing client-writable field names |
| `frontend-swing` | no | Removed; out of scope |
| `notaire-shared` | no | Prefer nested records in controllers (Folio/User pattern) |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: Property, IdentificationType, DeedManagement, Deed, ProcedureTemplate,
  Person, AuditRecord (create removed), Item, Substitution, Copy, History, Budget,
  BudgetTemplate — no schema change
- Endpoints: create/update (and AuditRecord POST) under `/api/v1/{inmueble,tipo-identificacion,gestiones,escrituras,plantilla-tramite,people,audit-log,items,suplencia,copia,historial,presupuestos,plantilla-presupuestos}`
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none
- **BREAKING:** AuditRecord POST removed; other endpoints keep client-writable
  field names but ignore unknown/server fields

### Architecture review

Follows existing adapter/in/web pattern (nested `XxxRequest` / `XxxResponse`
records as in `UserController` / `FolioController`), Spring Data `repository`
services, and "no entity objects in API layer". No new ADR — incremental
hardening of the existing hexagonal web adapter.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| OpenAPI annotations on affected controllers | Request/response schemas reflect DTOs; AuditRecord POST gone |
| Bruno collections under `testing/` / `integration-test/` | Bodies match request DTOs; drop audit-log create if present |
| `CHANGELOG.md` | Security note: request DTOs + audit POST removed |
| Use Case CU78 (if listed in wiki) | Point to mass-assignment closure if a permanent CU doc exists |

## Out of Scope

- Full replacement of legacy `Dto*` classes in `notaire-shared` (still used by
  Swing-era bridges elsewhere).
- Closing #1060 beyond removing AuditRecord POST (RBAC / OpenAPI status codes
  remain that issue's remaining AC if any).
- Refactoring read-only list endpoints that still serialize entities where the
  frontend already consumes them — create/get-by-id/update contracts move to
  response DTOs; bulk list migration may retain compatible shapes via the same
  response records where low-risk.
