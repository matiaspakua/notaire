# Fix Persona form swallowing non-409 backend validation messages

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #945 |
| Use Case | CU17 – Dar Alta Persona; CU61 – Buscar persona o cliente (form validation surface) |
| Branch | `cursor/fix-945-persona-validation-toast-69d3` (create at implement time) |
| Gate 1 status | draft ready (internal stockpile); implement after queue ahead of this pack |

## Objetivo

On the Personas dashboard, `handleSaveError` historically showed only the generic
`t("errorSave")` toast for non-409 failures, discarding backend validation text
(e.g. blank identification). Partial code already calls `extractApiError` for
non-409 on current `main`, but Acceptance Criteria still require the
`Dedup-EDGE` Playwright case in `TS-0015-personas-clientes-workflow.spec.ts` to
pass. This change closes that remaining gap so users see the specific server
message (or a matching form-level error) when create/update fails with 400.

## What Changes

- Confirm (and fix if regressed) that Persona save errors use
  `extractApiError(err) ?? t("errorSave")` for all non-409 `ApiError` cases;
  keep the localized 409 duplicate-document path with optional “view existing”.
- Optionally align Personas with `presentMutationError` from #1054 **only if**
  that preserves the curated 409 UX (`preferFallback` / dedicated handler).
- Make `Dedup-EDGE` (empty DNI → validation feedback, dialog stays open) pass
  reliably; adjust toast/form matching or client/server message surfacing as
  needed without inventing a second validation stack.
- Update CU17 / E2E mapping notes if the proven path changes; CHANGELOG for
  user-visible validation feedback.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| When persona create/update fails with a parseable backend validation message (e.g. 400), the UI must show that message instead of a generic save error | CU17 – Dar Alta Persona (alt: datos inválidos) | Made explicit |
| Generic `errorSave` is allowed only when no message is extractable from the `ApiError` | #945 AC; #1054 pattern | Made explicit |
| HTTP 409 duplicate-document keeps the localized `duplicateDocument` toast (and view-existing action when id is known), not raw English API text | CU17 / `persona-validacion-duplicados` | Unchanged — preserve |
| On validation failure the create/edit dialog stays open | CU17 form UX; Dedup-EDGE | Made explicit |

## Capabilities

### New Capabilities

- `persona-validation-error-toast`: Persona mutation error presentation for
  non-409 backend validation (toast and/or form-level message) with Dedup-EDGE
  E2E proof.

### Modified Capabilities

- (none — `persona-validacion-duplicados` already covers 409; this adds the
  complementary non-409 validation-feedback requirement)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | Existing bean-validation / `ErrorResponse` bodies; no contract change |
| `frontend` | yes | `personas/page.tsx` error path; possibly `mutation-error` wiring; Playwright TS-0015 |
| `frontend-swing` | no | Removed / out of scope |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | Existing Playwright job covers this |

### Surface area

- Entities: none
- Endpoints: consumes `POST/PUT /api/v1/people` error bodies; no OpenAPI change
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none new

### Architecture review

Follows #945/#1054 `ApiError` + `extractApiError` / `presentMutationError`
patterns. No ADR. Does not broaden CRUD toast migration (already #1054).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU17 – Dar Alta persona.md` | Note alt flow: 400 validation message shown in toast/form (not generic save error) |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | Confirm TS-0015 Dedup-EDGE ↔ CU17/#945 |
| `CHANGELOG.md` | User-visible: Persona form shows backend validation messages on save failure |

## Out of Scope

- **#1054** — shared mutation-error migration for other CRUD pages (shipped)
- Broad #615 matrix beyond Dedup-EDGE / Persona validation feedback
- Changing backend validation messages or i18n of API English strings
- Client-only DNI required UI unless needed to make Dedup-EDGE honest against
  the same user-visible contract (prefer surfacing server message)
