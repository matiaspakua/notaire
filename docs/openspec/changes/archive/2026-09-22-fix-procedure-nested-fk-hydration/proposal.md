# Fix Procedure nested FK hydration on create/update

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #981 |
| Use Case | CU82 — Generar Minuta de Inscripción |
| Branch | `fix/981_procedure_nested_fk_hydration` |
| Gate 1 status | pending |

## Objetivo

`POST /api/v1/tramites` (and `PUT /api/v1/tramites/{id}`) accept the raw JPA
`Procedure` entity directly as the request body. When a client sends a
nested reference object for an association (e.g.
`{"fkIdDeed": {"idDeed": 96}}`), Jackson deserializes it into a brand-new
transient `Deed` carrying only `idDeed` — the JPA layer never re-fetches the
real, previously-persisted row before cascading the save, so the response
(and subsequent `GET`) reflects blank/default association state (e.g.
`status: "Sin Firmar"`, `number: 0`) instead of the real linked `Deed`. This
silently corrupts every FK association passed this way
(`fkIdDeed`, `fkIdProperty`, `fkIdManagement`, `fkIdBudget`).

This blocks `POST /api/v1/minutas-inscripcion` (CU82), which depends on
`RegistrationDraftService` finding "a trámite with a property associated to
the deed" via the just-created `Procedure` — the corrupted association
means that lookup can come back empty, failing
`TS-0082-minuta-inscripcion-feature.spec.ts`'s golden path and two
related edge cases.

## What Changes

- Replace `ProcedureController.create`/`update`'s raw-entity request body
  with a `ProcedureRequest` record carrying plain FK ids
  (`idProcedureType`, `idProperty`, `idDeed`, `idManagement`, `idBudget`,
  `notes`), matching the pattern already used by
  `RegistrationDraftController.GenerateRequest`.
- The controller loads each referenced association from its repository
  before building/saving the `Procedure`, so the persisted and returned
  state always reflects the real row, never client-supplied placeholder
  data.
- `idProcedureType` is required (400 if missing or not found); `idProperty`,
  `idDeed`, `idManagement`, `idBudget` are optional, each resolved only if
  provided, 404 if a provided id does not exist.
- **BREAKING** for any API client still sending the old nested-object
  request shape (`{"fkIdProcedureType": {"idProcedureType": 1}}`) — the
  contract becomes flat ids (`{"idProcedureType": 1}`). No production UI
  caller exists yet for this endpoint (confirmed: no `frontend/src` call
  site), so the only callers are test fixtures, updated in this same
  change: Bruno (`backend-api/api-test/tramites/01-create.yml`,
  `04-update.yml`), Playwright E2E (`api-helpers.ts`'s `seedProcedure`,
  `TS-0082-minuta-inscripcion-feature.spec.ts`), and backend unit/
  integration tests.
- Response shape is unchanged (still the `Procedure` entity, matching
  every other endpoint's current convention in this controller) — only
  the request contract changes.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Creating or updating a Procedure must persist and return the real, currently-persisted state of each referenced Deed/Property/DeedManagement/Budget/ProcedureType — never a client-supplied placeholder | CU82 (implicit precondition: minuta generation needs the real Deed status) | Made explicit |
| `idProcedureType` is mandatory when creating a Procedure; a Procedure without a type is invalid | Existing DB constraint (`fk_id_procedure_type` `NOT NULL`), not previously enforced at the API request-validation level | Made explicit |

## Capabilities

### New Capabilities
- `procedure-fk-hydration`: request/response contract guaranteeing Procedure create/update always reflects real, persisted association state for its Deed/Property/DeedManagement/Budget/ProcedureType references

### Modified Capabilities
None — `minuta-inscripcion` (CU82) is a consumer of this fix, not itself changed; no requirement in that capability's own spec changes.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `ProcedureController` request contract; new `ProcedureRequest` record |
| `frontend` | no | No production UI caller exists for this endpoint yet |
| `frontend-swing` | no | N/A (module removed) |
| `notaire-shared` | no | No shared DTO changes — `ProcedureRequest` is a plain record local to the controller, not a cross-module DTO |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: `Procedure` (no field/column changes — only how the controller builds one from a request)
- Endpoints: `POST /api/v1/tramites`, `PUT /api/v1/tramites/{id}` — request body contract changes (see BREAKING note above)
- Database (Flyway `V{n}`): none — no schema change
- Configuration / `.env`: none
- Dependencies: none new

### Architecture review

Follows the existing pattern already used by `RegistrationDraftController.GenerateRequest`/`RegistrationDraftService` (plain-id request record, service/controller resolves real entities before persisting) rather than inventing a new convention. `Property`, `Budget`, `ProcedureType` already have hexagonal repository ports (`PropertyRepositoryPort`, `BudgetRepositoryPort`, `ProcedureTypeRepositoryPort`) used directly; `Deed`/`DeedManagement` do not have hex ports yet, so the fix uses their existing plain Spring Data repositories (`DeedRepository`, `DeedManagementRepository`) directly in the controller, consistent with how the codebase already mixes both patterns elsewhere (per CONSTITUTION.md P10 — adapt, don't replace; introducing two new hex ports is out of scope for this bug fix).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| Swagger/OpenAPI (auto-generated from `@Operation`/`@ApiResponses`) | Request body example implicitly updates via the new `ProcedureRequest` record; no manual doc edit needed |
| `docs/300-development/` API-UI traceability notes (if any reference the old nested body shape) | Checked during implementation; update if found |

## Out of Scope

- Introducing `DeedRepositoryPort`/`DeedManagementRepositoryPort` hexagonal ports — the existing plain repositories are reused as-is
- Converting the controller's response shape to a DTO — unchanged, matches existing convention for this controller
- Any other `*Controller` still accepting raw entities directly — this fix is scoped to `ProcedureController` only, the one blocking CU82
