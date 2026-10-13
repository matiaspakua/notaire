# Templates: all required-document templates overview (#1554)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1554 |
| Use Case | CU79 – Administrar plantillas de trámite |
| Branch | `feat/1554_all_templates_overview` |
| Gate 1 status | draft |

## Objetivo

CU79 administers procedure templates. `GET /procedure-templates` (all rows) existed (#1250 bucket C) with no UI caller; the UI only read templates per procedure type. Owner answered Q2: yes.

## What Changes

- `useAllProcedureTemplates()` (key `procedureTemplateKeys.all`; checklist mutations now also invalidate it).
- `AllProcedureTemplates` on /dashboard/administration/templates: procedure type, document type, condition (Requerido), notes; client-side filters by procedure and document; empty state.
- Each row links to `/dashboard/administration/procedures?checklist=<id>`, which opens that procedure's existing RequiredDocumentsDialog (page wrapped in Suspense for `useSearchParams`).
- Allowlist: `GET /api/v1/procedure-templates` removed. i18n `administracion.plantillas.todas.*` (es/en).
- Tests: Vitest `all-procedure-templates.test.tsx` (4); Playwright TS-0145 (desktop + 390px). CHANGELOG.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Every template row is a required document of its procedure type | CU79 | Existing (backend), now surfaced |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `procedure-template-overview`: Listing every required-document template across procedure types.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | hook, overview component, procedures deep link, i18n |
| `testing/e2e` | yes | TS-0145 |
| `contracts` | yes | allowlist |

### Surface area

- Endpoints / entities / Flyway / configuration: none (reuses `GET /procedure-templates`)

### Architecture review

Frontend only; the overview is its own component; the editor is reused through a query-string deep link.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Added entry |
| `contracts/api-reachability-allowlist.yaml` | bucket C entry removed |
