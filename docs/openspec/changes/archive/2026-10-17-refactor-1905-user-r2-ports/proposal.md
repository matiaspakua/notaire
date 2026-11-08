# User R2 shrink — ports/views (no entity on ports)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1905 |
| Use Case | CU / user (epic #1693 / ADR-030) |
| Branch | `cursor/fix-1905-user-r2-69d3` |
| Gate 1 status | passed |

## Objetivo

ArchUnit R2 still flags the user application slice (~2 edges): entity-speaking `UserRepositoryPort` and `RoleRepositoryPort`. Domain catalog ports already cover the HTTP path; the entity ports have no remaining consumers.

## What Changes

Delete unused entity-speaking `UserRepositoryPort` / `RoleRepositoryPort` and their `UserPersistenceAdapter` / `RolePersistenceAdapter`; keep domain `UserCatalogPort` / `RoleCatalogPort`; shrink hexagon-baseline (~2 R2 edges).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| User/role HTTP path uses catalog ports (domain types only); no entity-speaking application ports remain in the user slice | #1905 / ADR-030 / epic #1693 | New |

## Capabilities

### New Capabilities
- `user-r2-ports`: User application layer free of entity/repository R2 edges

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | delete unused entity ports/adapters; baseline |
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
| `docs/200-architecture/202-ADR/ADR-030-hexagonal-architecture-full-adoption.md` | user R2 slice log |
| `CHANGELOG.md` | n/a — omit Unreleased while Release Please holds it |

## Out of Scope

Property R2 (#1906); R6; CHANGELOG Unreleased; behaviour changes
