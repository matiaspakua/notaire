# Paginate GET /api/v1/people (slice of #596)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #596 |
| Use Case | CU18 – Dar Alta Cliente; CU54 – Modificar Persona; CU61 – Buscar persona |
| Branch | `fix/596_people_pagination` |
| Gate 1 status | draft |

## Objetivo

The people list returns the whole table; page it like the budget, deed, procedure and management lists.

## What Changes

- `PersonController#getAllPeople` takes a `Pageable` (default 20, sort `idPerson`) and returns `Page<PersonResponse>`; `PersonService#findAll(Pageable)`.
- Frontend `usePersonas` uses `apiGetPaged` (same as `usePresupuestos`).
- Bruno `people/02-list`, Playwright `TS-0051`, existing integration tests updated to the page shape; OpenAPI regenerated; CHANGELOG.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| List endpoints answer bounded pages | `.claude/rules/refactoring.md`, #596 | Applied to people |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `people-management`: Listing and maintaining people (clients, notaries).

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `PersonController`, `PersonService` |
| `frontend` | yes | `usePersonas` |
| `notaire-shared` | no | — |
| Docs / scripts / CI | yes | OpenAPI, Bruno, Playwright, CHANGELOG |

### Surface area

- Endpoints: `GET /api/v1/people` (response is now a page — breaking)
- Entities / Flyway / Configuration: none

### Architecture review

Same pattern as `ProcedureController`/`DeedController`/`BudgetController`; no ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | BREAKING Changed entry |
| `backend-api/openapi/openapi.yaml` | regenerated |
