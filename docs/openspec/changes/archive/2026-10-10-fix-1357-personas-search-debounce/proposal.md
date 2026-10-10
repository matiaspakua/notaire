# Personas search is debounced (#1357)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1357 |
| Use Case | CU61 – Buscar persona o cliente; RF-39 – Buscar clientes; RNF-03 – Tiempo de respuesta |
| Branch | `fix/1357_personas_search_debounce` |
| Gate 1 status | draft |

## Objetivo

The personas search sent one `GET /people/search` per keystroke (Playwright measured 17 requests for a 17-character surname) and the table flashed between keystrokes, because each new criteria set started with no data. The search lived inline in the page.

## What Changes

- `useSearchPersonas(params)` in `hooks/usePersonas.ts`: criteria debounced 300ms (`PERSONAS_SEARCH_DEBOUNCE_MS`) with the existing `useDebouncedValue`, trimmed, keyed by `personasKeys.search(params)` (primitive values only), `placeholderData: keepPreviousData`.
- `/dashboard/personas` uses the hook; the table shows its fetching state while a search refreshes.
- Vitest `personas-search.test.tsx`; Playwright TS-0015 `#1357`; CHANGELOG entry.

Already fixed on `main` before this change: the `personas` array is no longer part of the key (#1340) and `useDebouncedValue` exists.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A search request is sent once the user pauses typing (~300ms) | #1357, RNF-03 | New |
| Previous results stay visible until new ones arrive | #1357, RF-39 | New |

## Capabilities

### New Capabilities

- `personas-search`: debounced personas search with a primitive cache key.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | usePersonas (`useSearchPersonas`), personas page |
| `testing` | yes | Playwright TS-0015 |
| `backend-api` | no | GET /people/search unchanged |

### Surface area

- Route: /dashboard/personas
- API: GET /api/v1/people/search (unchanged contract)

### Architecture review

No architecture change. Server-side unified search (single `q` param) and the other search pages are out of scope.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
