# Paged endpoints document optional page/size/sort (slice of #596)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #596 |
| Use Case | CU74 – Performance and Caching Strategy |
| Branch | `fix/596_pageable_parameter_object` |
| Gate 1 status | draft |

## Objetivo

Five paged list endpoints document one required `pageable` query object, which no client sends: Spring reads `page`, `size` and `sort`. Document the real optional parameters so the OpenAPI artifact matches the implementation.

## What Changes

- `PersonController`, `BudgetController`, `DeedController`, `ProcedureController`, `ManagementController`: `@ParameterObject` on the `Pageable` argument.
- `openapi.yaml` regenerated: optional `page`/`size`/`sort` with defaults; unused `Pageable` schema dropped.
- `PagedEndpointsOpenApiParametersIntegrationTest` covers all 7 paged endpoints.
- CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The contract documents the parameters the server reads | CONSTITUTION (OpenAPI coherence), #596 | Applied to paged endpoints |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `api-contract`: The OpenAPI contract describes the request parameters the implementation actually reads.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | 5 controllers, OpenAPI |
| `frontend` | no | — (already sends page/size/sort) |
| Docs / scripts / CI | yes | CHANGELOG |

### Surface area

- Endpoints: `GET /api/v1/{people,presupuestos,escrituras,tramites,gestiones}` (documentation only; runtime unchanged)
- Entities / Flyway / Configuration: none

### Architecture review

No architecture change; same `@ParameterObject` pattern as `HistoryController` (#1322).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
| `backend-api/openapi/openapi.yaml` | regenerated |
