# Traceability — fix-1357-personas-search-debounce

| Requirement | Code | Tests |
|---|---|---|
| RF-39 search is debounced, one request per pause | `frontend/src/hooks/usePersonas.ts` (`useSearchPersonas`, `PERSONAS_SEARCH_DEBOUNCE_MS`) | `frontend/src/tests/unit/personas-search.test.tsx`; `testing/e2e/tests/TS-0015-personas-clientes-workflow.spec.ts` (#1357) |
| Query key holds primitive params only | `personasKeys.search` | `personas-search.test.tsx` |
| Previous results stay while loading (RNF-03) | `placeholderData: keepPreviousData`; `app/dashboard/personas/page.tsx` | `personas-search.test.tsx` |
