# Generate TypeScript API types from committed OpenAPI

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #1260 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `cursor/feat-1260-openapi-ts-types-cf98` |
| Gate 1 status | draft |
| Parent | #1197 Phase 0.5 |

## Why

`backend-api/openapi/openapi.yaml` is CI-guarded, but `frontend/src/types/index.ts` was
hand-written. Field renames slip to Playwright/prod (#773 class). REPO-SPLIT-PLAN P0.5
requires generated types before later topology work.

## Objetivo

One command regenerates `frontend/src/types/api.generated.ts`; Frontend CI fails on drift;
dashboard / gestiones / documentos / presupuestos hooks consume generated schema aliases.

## What Changes

- Add `openapi-typescript` + npm scripts `openapi:types` / `openapi:types:check`
- Commit generated types; Frontend CI drift check
- `src/types/api.ts` aliases; re-export high-traffic types from `index.ts`
- Migrate gestiones, presupuestos, documentos hooks (+ dashboard via those hooks)
- Design note + frontend README + CHANGELOG

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Frontend API types come from committed OpenAPI | #1260, #1197 P0.5 | Made explicit |
| CI fails when generated types drift | #1260 AC | New |
| High-traffic hooks use generated types | #1260 AC | New |

## Capabilities

### New Capabilities

- `openapi-generated-types`: OpenAPI→TS generation with drift guard and partial hook migration.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | package.json, types, hooks, Vitest, Frontend CI step |
| `docs` | yes | OpenSpec + design note |
| `backend-api` | no | consumes existing `openapi.yaml` only |

## Documentation Impact

| Doc | Change |
|-----|--------|
| `docs/200-architecture/203-design/FRONTEND-OPENAPI-TYPES.md` | generation + drift workflow |
| `frontend/README.md` | `openapi:types` scripts |
| `CHANGELOG.md` | Unreleased |
