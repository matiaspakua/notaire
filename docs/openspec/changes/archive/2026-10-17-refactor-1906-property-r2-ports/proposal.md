# Property R2 shrink — ports/views (no entity on ports)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1906 |
| Use Case | CU / property (epic #1693 / ADR-030) |
| Branch | `cursor/fix-1906-property-r2-69d3` |
| Gate 1 status | passed |

## Objetivo

ArchUnit R2 still flags the property application slice (~1 edge): entity-speaking `PropertyRepositoryPort`. Domain `PropertyCatalogPort` already covers the HTTP path; the only mid-strangler consumer is `ProcedureOpsAdapter` (already under `adapter.out.persistence`).

## What Changes

Inject Spring Data `PropertyRepository` into `ProcedureOpsAdapter` locally; delete entity-speaking `PropertyRepositoryPort` and `PropertyPersistenceAdapter`; keep `PropertyCatalogPort`; shrink hexagon-baseline (R2 1→0).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Property HTTP path uses PropertyCatalogPort (domain types only); entity Spring Data access for procedure ops stays in outbound adapters | #1906 / ADR-030 / epic #1693 | New |

## Capabilities

### New Capabilities
- `property-r2-ports`: Property application layer free of entity/repository R2 edges (R2 cleared)

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | delete entity port/adapter; wire Spring Data in ProcedureOpsAdapter; baseline |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

Follows the existing architecture; no new ADR (ADR-030 slice log only).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/202-ADR/ADR-030-hexagonal-architecture-full-adoption.md` | property R2 slice log |
| `CHANGELOG.md` | n/a — omit Unreleased while Release Please holds it |

## Out of Scope

R6 leftovers; CHANGELOG Unreleased; behaviour changes
