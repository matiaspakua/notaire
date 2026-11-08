# Type ManagementController#transition (#577)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #577 |
| Use Case | CU83 |
| Branch | `refactor/577_typed-management-transition` |
| Gate 1 status | passed |

## Objetivo

`ManagementController#transition` declares `ResponseEntity<?>` because `TransitionManagementWebMapper#mapToHttpResponse` is declared `Object`. The method always returns `ManagementSummaryDto`. Invalid and missing managements already throw to `GlobalExceptionHandler`. Create left the baseline in #2082. This tip types `transition` only.

## What Changes

- `transition` returns `ResponseEntity<ManagementSummaryDto>`.
- `mapToHttpResponse` returns `ManagementSummaryDto` instead of `Object`.
- Shrink `controller-loose-returns.txt` by the `ManagementController#transition` line only.
- No new exception handler. `BusinessValidationException` and `ResourceNotFoundException` already map to 400 and 404 `ErrorResponse`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A legal status change stays 200 with `id` and `currentStatus` | CU83 | Made explicit |
| A transition the workflow does not allow stays 400 `ErrorResponse` with `message` | CU83 | Made explicit |
| A missing management stays 404 `ErrorResponse` | existing not-found mapping | Made explicit |

## Capabilities

### New Capabilities

- None

### Modified Capabilities

- `controller-typed-returns`: `ManagementController#transition` leaves the loose-return baseline

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Transition return type, mapper return type, one MockMvc test, baseline |
| `frontend` | no | HTTP contract unchanged |
| `frontend-swing` | no | none |
| `infra` / observability | no | none |
| CI/CD (`.github/workflows`) | no | none |

### Surface area

- Entities: none
- Endpoints: `POST /api/v1/managements/{id}/transition` — HTTP contract unchanged
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Write set

- `backend-api/src/main/java/com/licensis/notaire/adapter/in/web/management/ManagementController.java`
- `backend-api/src/main/java/com/licensis/notaire/adapter/in/web/management/TransitionManagementWebMapper.java`
- `backend-api/src/test/java/com/licensis/notaire/unit/ManagementTransitionControllerTest.java`
- `backend-api/src/test/resources/architecture/controller-loose-returns.txt`
- `docs/300-development/303-testing/INVENTORY.md`
- `CHANGELOG.md`
- `docs/openspec/**`

### Risk

Risk: none. The JSON success body and the existing 400/404 `ErrorResponse` bodies stay as they are. No stored-data, authz, or merge-path change.

### Review budget

Target ≤400 counted lines (`aisdlc size`). OpenSpec, `CHANGELOG.md`, and `controller-loose-returns.txt` are excluded. One thin MockMvc class.

### Architecture review

Follows the typed-return pattern. Transition does not use a new advice: the non-success paths already throw, and `GlobalExceptionHandler` already maps them. No new ADR. The controller still calls the inbound port only. The mapper stays a web adapter and returns the DTO it already constructs.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased line, Refs #577 |
| `docs/openspec/specs/controller-typed-returns/spec.md` | Transition requirement |
| `docs/300-development/303-testing/INVENTORY.md` | Regenerated after the new unit test |

## Out of Scope

- Closing #577 (multi-PR epic; this tip uses Refs)
- `ManagementController#createCompleteCase`, `#updateCompleteCase`, and `#getWorkflowTrace`
- `AuditRecordController#getAll` and `AuxiliaryProtocolController#startDeed`
