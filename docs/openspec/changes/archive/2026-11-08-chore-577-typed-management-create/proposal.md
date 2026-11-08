# Type ManagementController#create (#577)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #577 |
| Use Case | CU02 |
| Branch | `refactor/577_typed-management-create` |
| Gate 1 status | passed |

## Objetivo

`ManagementController#create` declares `ResponseEntity<Object>` because a `ManagementResponse` 201 shares the method with empty 400, empty 404, and empty 200 bodies. `ServerError` already throws `IllegalStateException` to `GlobalExceptionHandler` (500 `ErrorResponse`). Logout left the baseline in #2081. This tip types `create` only.

## What Changes

- `create` returns `ResponseEntity<ManagementResponse>`.
- Shrink `controller-loose-returns.txt` by the `ManagementController#create` line only.
- No new exception handler: the non-success arms are empty bodies or the existing 500 throw. Advice would invent a JSON error the clients do not receive.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Plain create stays 201 `ManagementResponse` (id, number, heading, dates, notary, status, pending-debt flag, version) | CU02 | Made explicit |
| Bad request, missing referenced data, and the unused Updated arm stay empty 400, 404, and 200 | existing `SaveResult` mapping | Made explicit |
| Persistence failure stays 500 `ErrorResponse` via `IllegalStateException` | #579 | Made explicit |

## Capabilities

### New Capabilities

- None

### Modified Capabilities

- `controller-typed-returns`: `ManagementController#create` leaves the loose-return baseline

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Create return type, one MockMvc test, baseline |
| `frontend` | no | HTTP contract unchanged |
| `frontend-swing` | no | none |
| `infra` / observability | no | none |
| CI/CD (`.github/workflows`) | no | none |

### Surface area

- Entities: none
- Endpoints: `POST /api/v1/managements` — HTTP contract unchanged
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Write set

- `backend-api/src/main/java/com/licensis/notaire/adapter/in/web/management/ManagementController.java`
- `backend-api/src/test/java/com/licensis/notaire/unit/ManagementCreateControllerTest.java`
- `backend-api/src/test/resources/architecture/controller-loose-returns.txt`
- `docs/300-development/303-testing/INVENTORY.md`
- `CHANGELOG.md`
- `docs/openspec/**`

### Risk

Risk: none. The JSON success body, the empty error bodies, and the 500 `ErrorResponse` stay as they are. No stored-data, authz, or merge-path change.

### Review budget

Target ≤400 counted lines (`aisdlc size`). OpenSpec, `CHANGELOG.md`, and `controller-loose-returns.txt` are excluded. One thin MockMvc class, not an extension of `ManagementArchiveControllerTest`.

### Architecture review

Follows the typed-return pattern. Create does not use advice: archive, login, and logout each map a second JSON shape, and create's non-success arms do not. No new ADR. The controller still calls the inbound port only.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased line, Refs #577 |
| `docs/openspec/specs/controller-typed-returns/spec.md` | Create requirement |
| `docs/300-development/303-testing/INVENTORY.md` | Regenerated after the new unit test |

## Out of Scope

- Closing #577 (multi-PR epic; this tip uses Refs)
- `ManagementController#createCompleteCase` and `#updateCompleteCase` (success is `DtoManagementSummary`, not `ManagementResponse`)
- `ManagementController#getWorkflowTrace` and `#transition`
- `AuditRecordController#getAll` and `AuxiliaryProtocolController#startDeed`
