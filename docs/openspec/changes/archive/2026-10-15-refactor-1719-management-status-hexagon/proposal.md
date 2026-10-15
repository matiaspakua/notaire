# Management status as a full hexagon (slice 5 of epic 1693)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1719 |
| Use Case | CU30 / CU35 / CU67 — management status catalog (architectural refactor; CU76 characterization) |
| Branch | `refactor/1719_management_status_hexagon` |
| Gate 1 status | passed |

## Why

`ManagementStatusController` injects `ManagementStatusRepository` and `DeedManagementRepository`, uses the legacy entity's DTO conversion, carries `@Transactional`, and holds locked-when-in-use / delete-guard / search rules. This is the **fourth** such catalog after identification types, folio types and document types (#1705). ADR-030 requires each catalog to move behind domain-typed ports before extracting the shared lock rule.

## What Changes

- Pure domain `ManagementStatus` with create/restore/changeTo and domain exceptions without HTTP status.
- Inbound ports (query, manage), outbound ports (catalog, usage) and use cases holding the rules.
- Persistence adapter over the legacy entity (stays for deed management and history consumers).
- Controller calls inbound ports only; exception advice maps domain exceptions to unchanged bodies.
- ArchUnit baseline shrinks for `ManagementStatusController`.
- Shared lock/in-use extract: **deferred** to a follow-up issue after this green merge (keep shape compatible; do not diverge). Extracting in the same PR risks a large reusable refactor without a dedicated characterization suite across four catalogs.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Catalog reachable only through domain-typed ports; rules runnable without Spring/DB | #1719 | New |
| Status referenced by deed managements cannot be edited or deleted | existing controller | Made explicit in use cases |

## Capabilities

### New Capabilities

- `management-status-hexagon`: management status catalog behind ports and adapters.

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | domain/application/adapter for management status + tests |
| `frontend` | no | — |
| CI/CD | no | — |

### Surface area

- Entities: none (legacy JPA stays)
- Endpoints: none (behaviour unchanged aside from any deliberate stale-version correction called out in the PR)
- Database / Flyway: none
- CHANGELOG: **not touched** until #1716 clears

### Architecture review

Follows ADR-030; no new ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/202-ADR/ADR-030-hexagonal-architecture-full-adoption.md` | slice 5 log |
| `CHANGELOG.md` | **omit** while #1716 holds the lane |

## Out of Scope

- Extracting the shared catalog lock rule into a library (follow-up after merge).
- Migrating deed management / history off the legacy entity.
- Frontend hexagon.
