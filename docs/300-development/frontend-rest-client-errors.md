# Frontend REST client — errors and retry (#293, Wave 8 #1961)

Permanent guide for how the Next.js client handles HTTP failures. Complements
[`FRONTEND-DESIGN-SYSTEM.md`](../200-architecture/203-design/FRONTEND-DESIGN-SYSTEM.md)
(list `ErrorState` / form validation) and CU-77 quality infrastructure.

## Layers

| Layer | Responsibility | Location |
|-------|----------------|----------|
| Transport | Fetch, status codes, `ApiError` | `frontend/src/lib/api-client` (hex adapters) |
| Session | 401 → login redirect | `handleSessionExpiryFromUnknown` via `query-client` caches |
| Query defaults | One automatic retry; no refetch on focus | `frontend/src/lib/query-client.ts` (`retry: 1`) |
| List UX | Surface failure + user retry | `DataTable` `error` + `onRetry` → `ErrorState` |
| Mutations | Toast + optional field errors | `presentMutationError` / `useDeleteError` |
| Required fields | Client-side before POST | `requiredFieldErrors` + `FormField` `error` |

## Retry strategy

1. **Queries:** React Query retries once (`retry: 1`) with its default backoff. Do not raise this globally — noisy endpoints should set `retry: false` locally.
2. **User retry:** After the automatic attempt fails, list pages pass `onRetry={() => void refetch()}` so the operator can try again without a full page reload.
3. **Mutations:** No automatic retry (unsafe for creates). Show `presentMutationError` and leave the dialog open when field errors are mapped.

## Timeouts and offline

- Browser/`fetch` timeouts follow the deployed reverse proxy; there is no client-side circuit breaker today.
- Offline: failed queries surface `common.loadError`; the user retries when connectivity returns. Request queuing is out of scope.

## User-visible messages

- Prefer translated module `loadError` / `errorSave` keys; Wave 8 adds `common.loadError` as the shared list fallback.
- Never dump raw stack traces into the UI. `extractApiError` / `presentMutationError` map server bodies when present.

## Tests

- Unit: `required-field-errors.test.ts`, page form tests (people, budgets, copies).
- E2E: Wave 7 axe + required-field flows; list retry covered where DataTable error branch is asserted in unit tests (`async-states.test.tsx`).
