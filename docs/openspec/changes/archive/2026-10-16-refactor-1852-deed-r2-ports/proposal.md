# Deed R2 shrink — ports/views (no entity on ports)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1852 |
| Use Case | CU15 / CU86 – deed CRUD + numbering (architectural refactor under epic #1693 / ADR-030) — no behaviour change |
| Branch | `cursor/1852-deed-r2-a603` |
| Gate 1 status | passed |

## Objetivo

ArchUnit R2 still flags deed application types that speak `business.Person` / JPA repositories. Clear the deed R2 fan (smallest high-fan domain in the R2 wave).

## What Changes

- Inbound numbering ports take `Integer notaryId` (not `business.Person`).
- Move entity-bound `DeedService` / `DeedSigningService` from `application.usecase.deed` into `adapter.out.persistence.deed` (they only serve `DeedOpsAdapter`).
- Shrink `hexagon-baseline.txt` for cleared deed R2 edges.
- Omit CHANGELOG while #1766 holds Unreleased.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Deed inbound/outbound ports speak domain ids/views only; entity access stays in adapters | #1852 / ADR-030 / epic #1693 / CU15 / CU86 | New |

## Capabilities

### New Capabilities
- `deed-r2-ports`: Deed application layer free of entity/repository R2 edges

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | numbering ports; move DeedService/DeedSigningService; baseline shrink |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: none
- Endpoints: none (same deed REST)
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

Follows ADR-030; slice log only.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/202-ADR/ADR-030-hexagonal-architecture-full-adoption.md` | slice log for deed R2 |
| `CHANGELOG.md` | n/a — omit while #1766 holds Unreleased |

## Out of Scope

Report/management/workflow R2 children (#1849–#1851); full Deed domain aggregate extraction; CHANGELOG Unreleased (#1766).
