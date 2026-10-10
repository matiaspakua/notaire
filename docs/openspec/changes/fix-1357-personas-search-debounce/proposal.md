# Proposal — fix-1357-personas-search-debounce

## Why

Issue #1357 (RF-39 Buscar clientes, RNF-03 tiempo de respuesta): the personas search sent one `GET /people/search` per keystroke (17 requests for a 17-character surname, measured in Playwright), and the table flashed between keystrokes because each new criteria set started with no data. The search lived inline in the page.

## What changes

- `useSearchPersonas(params)` in `hooks/usePersonas.ts`: criteria debounced 300ms (`PERSONAS_SEARCH_DEBOUNCE_MS`) with the existing `useDebouncedValue`, trimmed, keyed by `personasKeys.search(params)` (primitive values only), `placeholderData: keepPreviousData`.
- The personas page uses the hook; the fetching indicator shows while a search refreshes.

Already fixed on main before this change: the `personas` array is no longer part of the key (#1340 pagination), and `useDebouncedValue` exists.

## Out of scope

Server-side unified search (single `q` param); other search pages.
