# Frontend budgets hexagon

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1827 |
| Use Case | Architectural refactor under epic #1693 (ADR-030); budgets admin/search — no behaviour change |
| Branch | `cursor/1827-fe-budgets-a603` |
| Gate 1 status | passed |

## Objetivo

After FE people (#1823): budget hooks and the budgets dashboard still import `api-client`, holding four eslint suppressions.

## What Changes

- Add `frontend/src/core/budget` domain + ports + use cases.
- Add `adapters/http/budget` HTTP gateway (only api-client import for budgets).
- Composition wiring; update `useBudgets` / `useBudgetSearch` and budgets page off api-client.
- Shrink eslint-suppressions; pin architecture-boundaries test.
- Omit CHANGELOG while #1766 holds Unreleased.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Budget hooks and budgets page call composition/use cases only; api-client stays in the HTTP adapter | #1827 / ADR-030 / epic #1693 | New |

## Capabilities

### New Capabilities
- `fe-budgets-hexagon`: Frontend budgets list/search/mutations behind core ports and HTTP adapter

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | core/budget, adapters/http/budget, composition, hooks, budgets page, suppressions |
| `backend-api` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD | no | — |

### Surface area

- Entities: none
- Endpoints: none
- Database: none
- Configuration: none
- Dependencies: none

### Architecture review

ADR-030 slice log only.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/202-ADR/ADR-030-hexagonal-architecture-full-adoption.md` | slice log for FE budgets |
| `CHANGELOG.md` | n/a — omit while #1766 holds Unreleased |

## Out of Scope

FE deeds/payments/protocol/testimonies waves; CHANGELOG Unreleased (#1766).
