# Item R2 shrink — ports/views (no entity on ports)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1904 |
| Use Case | CU / item (epic #1693 / ADR-030) |
| Branch | `cursor/fix-1904-item-r2-69d3` |
| Gate 1 status | passed |

## Objetivo

ArchUnit R2 still flags the item application slice (~2 edges): entity-speaking `ItemRepositoryPort` and entity-bound `ItemService` under `application.usecase`.

## What Changes

Move `ItemService` to `adapter.out.persistence.item` with Spring Data `ItemRepository` locally; delete entity-speaking `ItemRepositoryPort` and `ItemPersistenceAdapter`; wire budget catalog/template helpers to `ItemRepository`; keep view-safe `ItemOpsPort`; shrink hexagon-baseline (~2 R2 edges).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Item HTTP/inbound path uses OpsPort (DTO views only); entity and Spring Data access stays in outbound persistence adapters | #1904 / ADR-030 / epic #1693 | New |

## Capabilities

### New Capabilities
- `item-r2-ports`: Item application layer free of entity/repository R2 edges

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | move helper; delete entity port; baseline |
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
| `docs/200-architecture/202-ADR/ADR-030-hexagonal-architecture-full-adoption.md` | item R2 slice log |
| `CHANGELOG.md` | n/a — omit Unreleased while Release Please holds it |

## Out of Scope

Remaining R2 leftovers (user/property); R6; CHANGELOG Unreleased; behaviour changes
