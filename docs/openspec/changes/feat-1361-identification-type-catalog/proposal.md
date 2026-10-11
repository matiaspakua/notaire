# Identification type admin catalog (#1361, slice 2)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1361 |
| Use Case | RF-53 – Administrar tablas base; CU78 – Security, Privacy and Compliance |
| Branch | `cursor/1361-identification-type-catalog-69d3` |
| Gate 1 status | draft |

## Objetivo

Slice 1 (#1484) put a required identification-type select on the person form and removed the GET allowlist entry. Admins still cannot CRUD the catalog in the UI: POST/PUT/DELETE `/api/v1/identification-types` remain #1250 bucket C, so new types (or corrections) require API calls. This slice adds the ADMIN-only catalog page so RF-53 base tables are manageable in-app and reachability can drop the three UI-backlog entries.

## What Changes

- Frontend: admin page `/dashboard/administration/identification-types` (list, create, edit, delete) using `FormContainer` → `FormSection` → `FormField` → `FormActions`, patterned on `management-statuses`.
- Frontend: extend `useIdentificationTypes` with create/update/delete mutations (`identificationTypeKeys`); invalidate the list used by the person form.
- Frontend: card on `/dashboard/administration` index; i18n keys under `administracion.tiposIdentificacion` / `nav.admin.identificationTypes` (es catalog + en).
- Delete failures surface the backend 409 reason via `useDeleteError` (#1345). No new `/in-use` pre-check unless Backend adds one later.
- `contracts/api-reachability-allowlist.yaml`: remove `POST`, `PUT`, and `DELETE` `/api/v1/identification-types` (and `/{id}`) UI-backlog lines; keep `GET .../{id}` as bucket A.
- Vitest hook/page tests; Playwright `TS-0127-identification-type-catalog.spec.ts` (CRUD + non-admin redirect).
- CHANGELOG entry when the product PR ships (after heavy-CI/#1518 serialize clears). Not in this Gate 1 prep commit.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Only ADMIN may open the identification-type catalog UI | CU78; existing `/dashboard/administration` layout guard (#1052) | Made explicit for this catalog |
| Admins can create, edit and delete identification types used by people | RF-53; #1361 expected behavior 2 | New (UI) |
| Delete of a type still referenced by people fails with a clear reason | #1345 delete-in-use pattern; API already 409 on FK conflict | Made explicit in UI |
| Non-admin has no admin index card path and cannot stay on the catalog URL | CU78 / #1052 | Made explicit |

## Capabilities

### New Capabilities

- `ui-identification-type-catalog`: ADMIN CRUD UI for identification types (DNI, CUIT, passport, …).

### Modified Capabilities

- (none under `docs/openspec/specs/`)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | Existing `IdentificationTypeController` is sufficient; no Flyway |
| `frontend` | yes | new page, hook mutations, admin index link, i18n, Vitest |
| `frontend-swing` | no | — |
| `contracts` | yes | reachability allowlist: drop 3 bucket-C entries |
| `testing/e2e` | yes | TS-0127 + E2E mapping row |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | Existing frontend / Playwright jobs |

### Surface area

- Entities: none (catalog already exists)
- Endpoints: UI consumes `GET/POST /identification-types`, `PUT/DELETE /identification-types/{id}` (already in OpenAPI). Not BREAKING.
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

Follows existing admin catalog pages and design-system form patterns. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | Add TS-0127 row (CU/RF + issue) |
| `CHANGELOG.md` | Unreleased entry for the admin catalog (product PR only; Agent 3 holds lock via #1518) |
| `contracts/api-reachability-allowlist.yaml` | Remove three UI-backlog lines |

## Out of Scope

- Person-form select / DNI default / list label (slice 1, merged #1484).
- `characters`-based `maxLength` enforcement on the person number field (deferred in slice 1).
- Backfilling legacy persons without a type (Owner decision: not in this issue).
- Adding `GET /identification-types/{id}/in-use` (optional Backend #8 follow-up; UI uses 409 toast today).
- Pure Englishize refactors outside touched strings (Agent 3 lane).
