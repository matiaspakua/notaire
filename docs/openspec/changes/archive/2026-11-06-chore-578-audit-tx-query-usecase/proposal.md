# Move audit read transactions out of AuditRecordController

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #578 |
| Use Case | CU76 |
| Branch | `chore/578_audit-tx-query-usecase` |
| Gate 1 status | passed |

## Objetivo

AuditRecordController is the last web adapter with `@Transactional`; it also injects the outbound ops port directly.

## Why

Issue #578: transaction boundaries belong in the application layer, not HTTP controllers. Hex (ADR-030) requires inbound ports for orchestration.

## What Changes

Add `QueryAuditRecordsUseCase` + `AuditRecordQueryUseCaseService` with `@Transactional(readOnly = true)`. Controller delegates to the inbound port and drops `@Transactional`. Update unit tests. Prove no `@Transactional` remains under `adapter.in.web`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Audit read APIs run in a read-only application-layer transaction. | Issue #578; CU23/CU76 | Made explicit |

## Capabilities

### New Capabilities

- `audit-tx-query-usecase`: Audit queries go through inbound port with TX in the use case.

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | inbound port, use case service, controller, unit tests |
| `frontend` | yes | — |
| `frontend-swing` | no | — |
| `infra` / observability | yes | — |
| CI/CD (`.github/workflows`) | yes | — |

### Surface area

- Entities: none
- Endpoints: none (behavior unchanged)
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

Follows hexagonal inbound/outbound ports (ADR-030); no new ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased note for #578 audit TX move |

## Out of Scope

["Closing #578 if other business-logic-in-controller leftovers remain", "Rewriting AuditRecordService persistence internals"]
