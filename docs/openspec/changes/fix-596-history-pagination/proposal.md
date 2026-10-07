# Paginate GET /api/v1/historial (slice of #596)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #596 |
| Use Case | CU13 – Gestionar historial de gestion |
| Branch | `fix/596_history_pagination` |
| Gate 1 status | draft |

## Objetivo

The history list returns the whole `history` table, which gains a row on every state change. Page it like the people, budget, deed, procedure and management lists, and document the paging parameters correctly in the OpenAPI artifact.

## What Changes

- `HistoryController#getAll` takes a `Pageable` (default 20, sort `idHistory`) through springdoc `@ParameterObject` and returns `Page<DtoHistorySummary>`.
- `openapi.yaml` regenerated: optional `page`/`size`/`sort` query parameters and `PageDtoHistorySummary`.
- `accepted-breaking-changes.txt`: the response-type change, with the reason no client breaks.
- Bruno `history/03`, existing unit/integration tests follow the page shape; CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| List endpoints on growing tables answer bounded pages | `.claude/rules/refactoring.md`, #596 | Applied to history |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `management-history`: Recording and reading the state-change history of deed managements.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `HistoryController`, OpenAPI, Bruno |
| `frontend` | no | — (no screen reads this list) |
| Docs / scripts / CI | yes | CHANGELOG, accepted-breaking-changes |

### Surface area

- Endpoints: `GET /api/v1/historial` (response is now a page — breaking; new optional query parameters)
- Entities / Flyway / Configuration: none

### Architecture review

Same pattern as `PersonController`/`BudgetController`; no ADR. `@ParameterObject` documents `Pageable` as its real optional query parameters instead of a required `pageable` object.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | BREAKING Changed entry |
| `backend-api/openapi/openapi.yaml` | regenerated |
| `backend-api/openapi/accepted-breaking-changes.txt` | accepted entry |
