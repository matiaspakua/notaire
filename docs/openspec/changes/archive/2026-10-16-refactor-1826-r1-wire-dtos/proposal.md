# Shrink ArchUnit R1 wire DTOs (ItemResponse + *Ref)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1826 |
| Use Case | Architectural refactor under epic #1693 (ADR-030). Wire-layer mapping only — no behaviour change |
| Branch | `cursor/1826-r1-wire-dtos-a603` |
| Gate 1 status | passed |

## Objetivo

Clear remaining ArchUnit R1 edges on inbound-adapter wire types that still depend on `business.*` / `jakarta.persistence`.

## What Changes

- Remove `ItemResponse.from(Item)`; BudgetController template/catalog paths return `ItemView` via services.
- Remove dead `*Ref.from(entity)` factories (DeedRef, DocumentTypeRef, ManagementRef, NotaryRef, ProcedureTypeRef, PropertyRef).
- Clear `ErrorResponses` → `OptimisticLockException` via class-name check (no JPA type import).
- Shrink ArchUnit R1 baseline. Omit CHANGELOG while #1766 holds Unreleased.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Inbound wire DTOs map from views/IDs only — no JPA entity imports | #1826 / ADR-030 / epic #1693 | New |

## Capabilities

### New Capabilities
- `r1-wire-dtos`: ArchUnit R1 clearance for ItemResponse and support *Ref / ErrorResponses

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | ItemResponse; *Ref; ErrorResponses; BudgetTemplate/Catalog services return ItemView |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD | no | — |

### Surface area

- Entities: none
- Endpoints: none (JSON shapes unchanged)
- Database: none
- Configuration: none
- Dependencies: none

### Architecture review

ADR-030 slice log only.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/202-ADR/ADR-030-hexagonal-architecture-full-adoption.md` | slice log for R1 wire DTO shrink |
| `CHANGELOG.md` | n/a — omit while #1766 holds Unreleased |

## Out of Scope

FE budgets (#1827); R2 application-layer entity cleanup; CHANGELOG Unreleased (#1766).
