# Fix API error toasts swallowed on CRUD pages

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1054 |
| Use Case | CU15 – Procesar pago; CU20 – Dar alta usuario / CU21 – Modificar Usuario (exemplars); surface also CU26–CU30 tablas base |
| Branch | `cursor/fix-1054-api-error-toasts-69d3` (create at implement time) |
| Gate 1 status | draft ready (internal); implement after #1137 / CI capacity |

## Objetivo

Fifteen dashboard pages toast generic save/delete strings and never surface the
backend business message carried on `ApiError`. `extractApiError` only maps HTTP
400/409, and no page wires `FormField error=` / `aria-invalid` when the API
returns field detail. Users never see rules such as “in use”, duplicates, or
overpayment. This change generalises the #945 Persona fix into one shared
mutation-error path for all listed CRUD surfaces.

## What Changes

- Single shared mutation error handler that maps `ApiError` (all relevant
  statuses) into a toast message and optional per-field errors.
- Expand `extractApiError` (or successor helper) beyond 400/409 so business and
  conflict bodies are shown; keep 401 on the #1053 session-expiry path.
- Confirm `apiDelete` throws `ApiError` for every non-OK status (partially done
  in #1053) and cover remaining statuses in unit tests.
- Migrate the 15 pages that still use bare `toast.error(t("error…"))` /
  catch-without-`err` to the shared handler; set `FormField error=` and
  `aria-invalid` when field detail is available.
- Focused Playwright coverage aligned with #615 (representative CRUD error
  display, not a 15-suite matrix).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| When a mutation fails, the user must see the server’s business validation message when present | CU15 – Procesar pago (alt flows: pago inválido / saldo); CU20/CU21 usuario alta/modificación | Made explicit |
| Field-level validation from the API must appear next to the offending control when mappable | Cross-cutting CRUD UX; FormField design system | New (document in CU15 / CU20 alt notes) |
| Generic fallback copy is allowed only when the API body has no parseable message | Existing toast pattern (#945) | Made explicit |
| Session-expiry 401 must not be presented as a generic mutation toast | CU84 / #1053 | Unchanged — out of this change’s handler for authenticated 401 |

## Capabilities

### New Capabilities

- `api-error-toasts`: Shared client mapping of `ApiError` into user-visible toasts
  and inline field errors across CRUD mutation pages.

### Modified Capabilities

- (none under `openspec/specs/` today cover client mutation error presentation;
  #945 was Persona-local without a capability delta)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | `GlobalExceptionHandler` / `ErrorResponse` already return `message`; no contract change required for Gate 1 |
| `frontend` | yes | `api-client`, `utils` / new error helper, 15 dashboard pages, unit + Playwright |
| `frontend-swing` | no | Removed / out of scope |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | Existing frontend / Playwright jobs cover this |

### Surface area

- Entities: none
- Endpoints: consumes existing REST error bodies (`ErrorResponse.message` /
  bean-validation joined `field: msg`); no OpenAPI change
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none new

### Architecture review

Follows existing `ApiError` + design-system `FormField` patterns from #945 /
\#1053. No ADR. Does not implement #1051 (HttpOnly JWT) or broaden #615 beyond
the error-display slice needed for #1054 AC.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU15 – Procesar pago.md` | Note alt flow: API business error shown in toast / field when pago rejected |
| `docs/100-business/102-use-cases/CU20 – Dar alta usuario.md` | Note alt flow: API conflict/validation message surfaced (not generic toast) |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | Map new TS-nnnn API-error-display E2E to CU15/CU20, #1054, #615 |
| `CHANGELOG.md` | User-visible: CRUD screens show backend validation messages |

## Out of Scope

- **#1053** — session 401 redirect (shipped); do not regress
- **#1051** — JWT HttpOnly cookie / CSP
- **#1052** — admin route guards (in-flight #1137)
- Broad #615 matrix for every form in the app — only focused error-display E2E
  for this AC; leave residual #615 open if needed
- Changing `GlobalExceptionHandler` JSON shape (optional follow-up if structured
  `fieldErrors` map is later desired)
- Backend i18n of exception messages
