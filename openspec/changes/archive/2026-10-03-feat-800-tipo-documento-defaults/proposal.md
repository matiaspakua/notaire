# Expose DocumentType enabled and returned on admin form

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #800 |
| Use Case | CU27 — Ingresar nuevo tipo de documento; CU32 — Modificar tipo de documento; CU04 — Registrar documentación cliente; CU72 — Gestión de Documentos Presentados |
| Branch | `cursor/feat-800-tipo-documento-defaults-69d3` |
| Gate 1 status | passed |

## Objetivo

Issue #837 shipped `expires` / `dueDays` / `deliveredBy` on the document-type
admin form and SubmittedDocument inheritance, but the residual `#800` fields
`enabled` and `returned` remain omitted from the create/edit UI and from the
API DTO round-trip (`DtoDocumentType` has no `returned`; create forces
`enabled=true`). Operators still cannot configure those catalog flags from
administration.

## What Changes

- Admin form `administracion/documentos` adds CheckboxFields for `enabled` and
  `returned`, with EMPTY-create defaults `enabled: true`, `returned: false`.
- Frontend `TipoDeDocumento` type gains `returned?` (already has `enabled?`).
- i18n labels for the new fields in `en.json` / `es.json`; Englishize hardcoded
  Spanish toasts on the touched page via i18n keys.
- Backend: add `returned` to `DtoDocumentType`; map it in
  `DocumentType.setAtributos` / `getDto`; stop overwriting client `enabled` on
  create (default to `true` only when omitted).
- Vitest + Playwright cover create/edit of the two fields; confirm
  `SubmittedDocumentControllerTest` inheritance remains green.

**BREAKING CHANGES:** None. Existing clients that omit `enabled`/`returned`
keep today's defaults (`enabled=true`, `returned=false`).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| On create, a document type defaults to enabled and not returned | CU27 / entity columns | Made explicit in UI + API defaults |
| Admin can set `enabled` and `returned` on create and edit (when not in use) | CU27, CU32 | Made explicit (modeled, missing from form/DTO) |
| Create/update persist `enabled`/`returned` from the request body | CU27, CU32 | Changed — write path previously dropped `returned` and forced `enabled=true` |

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `tipo-documento-vencimiento-config`: extend the admin document-type catalog
  form/API contract with residual `enabled` and `returned` fields (does not
  re-implement #837 expires/dueDays/deliveredBy inheritance).

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Map `returned` + respect `enabled` on create/update; integration tests |
| `frontend` | yes | Form checkboxes, types, i18n, Vitest + Playwright |
| `frontend-swing` | no | Removed; out of scope |
| `notaire-shared` | yes | `DtoDocumentType.returned` field |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: `DocumentType.enabled`, `DocumentType.returned` (columns already exist)
- Endpoints: `POST/PUT/GET /api/v1/tipo-de-documento` — expose/persist `returned`;
  honor request `enabled` on create when provided
- Database (Flyway): none — columns already present
- Configuration / `.env`: none
- Dependencies: none
- **BREAKING:** no

### Architecture review

Follows existing layering (`adapter.in.web` → entity DTO mapping → repository).
No new architectural pattern; no ADR required. Frontend uses existing
`CheckboxField` / form-patterns / theme tokens.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU27 – Ingresar nuevo tipo de documento.md` | Note enabled/returned as loadable fields |
| `docs/100-business/102-use-cases/CU32 – Modificar tipo de documento.md` | Same for edit |
| `openspec/specs/tipo-documento-vencimiento-config/spec.md` | Sync delta requirements after merge/archive |
| `CHANGELOG.md` | User-visible entry under `[Unreleased]` |

## Out of Scope

- Re-implementing #837 SubmittedDocument inheritance of expires/dueDays/deliveredBy
  (already shipped).
- Inheriting `enabled`/`returned` onto SubmittedDocument (not in #800 residual scope).
- Renaming Spanish URL path `/tipo-de-documento` (rename-ADR deferral).
