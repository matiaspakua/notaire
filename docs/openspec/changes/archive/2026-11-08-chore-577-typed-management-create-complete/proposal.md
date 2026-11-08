# Type ManagementController#createCompleteCase (#577)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #577 |
| Use Case | CU02 |
| Branch | `refactor/577_typed-management-create-complete` |
| Gate 1 status | passed |

## Objetivo

`ManagementController#createCompleteCase` declares `ResponseEntity<Object>` because a `DtoManagementSummary` 201 shares the method with empty 400, empty 404, and empty 200 bodies. Transition left the baseline in #2085. This tip types `createCompleteCase` only.

## What Changes

- `createCompleteCase` returns `ResponseEntity<DtoManagementSummary>`.
- `CompleteCaseWebMapper#mapCreateResult` returns `ResponseEntity<DtoManagementSummary>` instead of living only in the controller switch.
- Shrink `controller-loose-returns.txt` by the `ManagementController#createCompleteCase` line only.
- No new exception handler. Non-success arms stay empty bodies the clients already receive.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Complete-case create stays 201 with `DtoManagementSummary` | CU02 | Made explicit |
| Bad request, missing referenced data, and the unused Updated arm stay empty 400, 404, and 200 | existing `CompleteCaseResult` mapping | Made explicit |

## Capabilities

### New Capabilities

- None

### Modified Capabilities

- `controller-typed-returns`: `ManagementController#createCompleteCase` leaves the loose-return baseline

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Create complete-case return type, web mapper, one MockMvc test, baseline |
| `frontend` | no | HTTP contract unchanged |
| `frontend-swing` | no | none |
| `infra` / observability | no | none |
| CI/CD (`.github/workflows`) | no | none |

### Surface area

- Entities: none
- Endpoints: `POST /api/v1/managements/complete-case` — HTTP contract unchanged
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Write set

- `backend-api/src/main/java/com/licensis/notaire/adapter/in/web/management/ManagementController.java`
- `backend-api/src/main/java/com/licensis/notaire/adapter/in/web/management/CompleteCaseWebMapper.java`
- `backend-api/src/main/java/com/licensis/notaire/config/ManagementCompositionConfig.java`
- `backend-api/src/test/java/com/licensis/notaire/unit/ManagementCreateCompleteCaseControllerTest.java`
- `backend-api/src/test/resources/architecture/controller-loose-returns.txt`
- `docs/300-development/303-testing/INVENTORY.md`
- `CHANGELOG.md`
- `docs/openspec/**`

### Risk

Risk: none. The JSON success body and the empty error bodies stay as they are. No stored-data, authz, or merge-path change.

### Review budget

Target ≤400 counted lines (`aisdlc size`). OpenSpec, `CHANGELOG.md`, and `controller-loose-returns.txt` are excluded. One thin MockMvc class.

### Architecture review

Follows the typed-return pattern. Create complete-case does not use a new advice: the non-success arms are empty bodies. No new ADR. The controller still calls the inbound port only; the mapper stays a web adapter.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased line, Refs #577 |
| `docs/openspec/specs/controller-typed-returns/spec.md` | Create complete-case requirement |
| `docs/300-development/303-testing/INVENTORY.md` | Regenerated after the new unit test |

## Out of Scope

- Closing #577 (multi-PR epic; this tip uses Refs)
- `ManagementController#updateCompleteCase` and `#getWorkflowTrace`
- `AuditRecordController#getAll` and `AuxiliaryProtocolController#startDeed`
