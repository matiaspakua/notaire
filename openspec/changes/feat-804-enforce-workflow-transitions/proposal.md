# Enforce WorkflowDefinition on all real Gestión status writes

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #804 |
| Use Case | CU02 — Iniciar Gestión; CU53 — Modificar Gestión; CU16 — Archivar Gestión; CU83 — Definir Workflow (engine) |
| Branch | `cursor/feat-804-enforce-workflow-transitions-69d3` |
| Gate 1 status | passed |

## Objetivo

#833 already exposes `POST /gestiones/{id}/transition` and the UI filters
destinations from `workflow-trace`, but plain `PUT` / complete-case update can
still set any `managementStatusId`, bypassing the workflow graph. Close those
bypasses so every real status mutation is legal per `WorkflowDefinition`.

## What Changes

- **BREAKING:** Reject status mutations via `PUT /gestiones/{id}` and
  `PUT /gestiones/{id}/complete-case` when the status id changes; clients MUST
  use `POST /gestiones/{id}/transition` (frontend already does).
- On create (`POST /gestiones`, `POST /gestiones/complete-case`), allow initial
  status assignment without a prior edge; when a workflow exists, validate the
  chosen status is a workflow node (prefer the INITIAL/start node).
- Formalize legal-next contract: document `GET /gestiones/{id}/workflow-trace`
  as the destinations source (nodes + transitions from current status). Optional
  thin `GET /gestiones/{id}/valid-destinations` only if needed for OpenAPI clarity.
- Keep #806 History (bitácora) writes on create and on successful `/transition`
  / archive; do not strip bitácora coverage.
- Always call the shared transition validator / use case — no duplicated graph
  checks.
- Update OpenAPI, CHANGELOG (**BREAKING**), Bruno if needed, Playwright
  TS-0011 / TS-0029 confirmation.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| After create, status changes MUST go through a validated workflow transition | CU83, CU53 | New (enforcement on orphan write paths) |
| Illegal status writes via generic PUT/complete-case MUST be rejected | CU83, #804 | New |
| Initial create MAY set a status without a prior edge; when workflow exists it MUST be a node (prefer start) | CU02, CU83 | New |
| Legal next destinations are derived from the gestión's WorkflowDefinition | CU83, #833 | Made explicit (workflow-trace contract) |
| Archive and `/transition` remain the supported mutation paths | CU16, CU83 | Made explicit |
| History (bitácora) on status create/change remains (#806) | CU13 | Unchanged — preserve |

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `gestion-workflow-transicion`: Extend enforcement so generic PUT and
  complete-case status mutations cannot bypass the workflow graph; document
  legal-next via workflow-trace; clarify create-time initial status rules.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Guard status writes in `ManagementController`; shared validator use; integration tests; OpenAPI |
| `frontend` | yes | Confirm filtered selector + TS-0011/TS-0029; no new UI required if already using `/transition` |
| `frontend-swing` | no | Removed; out of scope |
| `notaire-shared` | maybe | DTO only if thin valid-destinations endpoint is added |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: none (behavior only)
- Endpoints: `PUT /api/v1/gestiones/{id}`, `PUT /api/v1/gestiones/{id}/complete-case`,
  `POST /api/v1/gestiones`, `POST /api/v1/gestiones/complete-case`,
  `GET /api/v1/gestiones/{id}/workflow-trace` (contract docs),
  existing `POST .../transition` unchanged as the mutation path
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none
- **BREAKING:** yes — clients that PUT arbitrary `managementStatusId` /
  `statusManagementId` on update receive 400 and must call `/transition`

### Architecture review

Follows hexagonal Ports & Adapters: reuse
`WorkflowTransitionValidatorPort` / `TransitionManagementUseCase` and
`WorkflowLookupPort`. No ADR required (enforces existing CU83 engine on orphan
paths). No schema change.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU53 - Modificar Gestión.md` | Status changes require `/transition`; PUT rejects status mutations |
| `docs/100-business/102-use-cases/CU02 – Iniciar Gestión.md` | Initial status must be a workflow node when workflow exists |
| `docs/100-business/102-use-cases/CU83 – Definir Workflow de Estados y Transiciones.md` | Enforcement on real gestiones writes; legal-next via workflow-trace |
| `docs/200-architecture/203-design/REST-API-ENDPOINT_REGISTRY.md` | Note BREAKING PUT status behavior; workflow-trace as destinations source |
| `CHANGELOG.md` | **BREAKING** entry under `[Unreleased]` |
| `backend-api/api-test/COVERAGE.md` | Note PUT status rejection / Bruno updates if any |

## Out of Scope

- Rebuilding the workflow builder UI or `WorkflowValidationService` graph editor.
- Renaming Spanish path segments (`/historial`, `/estado-actual`, `/archivar`) —
  rename ADR if any.
- Changing archive debt rules (#774 / related) beyond existing
  `ManagementTransitionService` usage.
- Backfilling illegal historical status values already persisted.
