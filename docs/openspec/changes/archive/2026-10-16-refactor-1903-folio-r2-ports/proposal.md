# Folio R2 shrink — ports/views (no entity on ports)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1903 |
| Use Case | CU / folio (epic #1693 / ADR-030) |
| Branch | `cursor/fix-1903-folio-r2-69d3` |
| Gate 1 status | passed |

## Objetivo

ArchUnit R2 still flags the folio application slice (~2 edges): entity-speaking `FolioRepositoryPort` (`Folio`, `FolioType`). Domain catalog ports already exist; the only mid-strangler consumer is `AuxiliaryProtocolService` (already under `adapter.out.persistence`).

## What Changes

Inject Spring Data `FolioRepository` into `AuxiliaryProtocolService` locally; delete entity-speaking `FolioRepositoryPort` and `FolioPersistenceAdapter`; keep view-safe `FolioCatalogPort` / `FolioTypeCatalogPort`; shrink hexagon-baseline (~2 R2 edges).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Folio HTTP/inbound path uses catalog ports (domain types only); entity and Spring Data access for auxiliary-protocol helpers stays in outbound persistence adapters | #1903 / ADR-030 / epic #1693 | New |

## Capabilities

### New Capabilities
- `folio-r2-ports`: Folio application layer free of entity/repository R2 edges

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | delete entity port/adapter; wire Spring Data in notebook helper; baseline |
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
| `docs/200-architecture/202-ADR/ADR-030-hexagonal-architecture-full-adoption.md` | folio R2 slice log |
| `CHANGELOG.md` | n/a — omit Unreleased while Release Please holds it |

## Out of Scope

Remaining R2 leftovers (item/user/property); R6; CHANGELOG Unreleased; behaviour changes
