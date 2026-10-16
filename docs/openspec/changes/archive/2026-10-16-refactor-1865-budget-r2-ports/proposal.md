# Budget R2 shrink — ports/views (no entity on ports)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1865 |
| Use Case | Budget / budget templates CU39/CU60/CU71 (architectural refactor under epic #1693 / ADR-030) — no behaviour change |
| Branch | `cursor/1865-budget-r2-a603` |
| Gate 1 status | passed |

## Objetivo

ArchUnit R2 still flagged the budget application slice (~20 edges): entity-speaking `BudgetRepositoryPort` and entity-bound helpers (`BudgetService`, `BudgetTemplateCatalogService`, `BudgetTemplateService`, `BudgetCatalogItemsService`).

## What Changes

- Move entity-bound budget helpers to `adapter.out.persistence.budget` (use Spring Data locally).
- Delete entity-speaking `BudgetRepositoryPort` (+ persistence adapter); `BudgetOpsPort.existsById` for `ItemService`; `ProcedureOpsAdapter` uses `BudgetRepository`.
- Add inbound `LoadBudgetItemsFromTemplateUseCase` / `AddBudgetItemsFromCatalogUseCase` so `BudgetController` stays R5-clean.
- Shrink `hexagon-baseline.txt` (~20 R2 edges). Omit CHANGELOG while #1766 holds Unreleased.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Budget HTTP adapters call inbound ports only; entity/repository access stays in outbound budget adapters | #1865 / ADR-030 / epic #1693 | New |

## Capabilities

### New Capabilities
- `budget-r2-ports`: Budget application layer free of entity/repository R2 edges; controllers use inbound ports for CU39/CU71

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | move helpers; delete entity port; inbound ports; baseline |
| `frontend` | no | — |

### Surface area

- Entities / endpoints / Flyway / config / deps: none (same budget REST)

### Architecture review

ADR-030 slice log only.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| ADR-030 | slice log for budget R2 |
| CHANGELOG.md | n/a — #1766 |

## Out of Scope

Document/procedure R2 (#1866/#1867); item hexagon deep rewrite; CHANGELOG Unreleased.
