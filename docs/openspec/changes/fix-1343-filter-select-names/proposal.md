# Filter selects have accessible names

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1343 |
| Use Case | RNF-07 – Diseño de campos y combos; CU76 |
| Branch | `fix/1343_filter_select_names` |
| Gate 1 status | draft |

## Objetivo

axe reported critical `button-name`/`select-name` violations for the five filter controls that sit outside a labelled `FormField`: the client filter on gestiones, the status filters on presupuestos and folios, the module filter on auditoria and the workflow selector on estados-gestion. Screen readers announced them as an unnamed "combobox" or "button".

## What Changes

- `aria-label` on the five controls from new keys `gestiones.clienteFilter`, `presupuestos.estadoFilter`, `administracion.folios.estadoFilter`, `administracion.estadosGestion.workflowFilter` and `auditoria.moduleFilter` (es/en); the decorative filter icon on auditoria is `aria-hidden`.
- Vitest `select-accessible-names.test.ts`: a static scan that fails on any `<SelectTrigger>` or `<select>` in `src/app` without `aria-label`/`aria-labelledby` or a labelled `FormField` around it.
- Playwright `TS-0042`: the filters are reachable with `getByRole("combobox", { name })`.
- CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Every form control states its purpose to assistive technology | #1343, RNF-07, WCAG 4.1.2/1.3.1 | Made explicit |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `ui-accessibility`: Dashboard controls and dialogs expose accessible names in the active locale.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | 5 dashboard pages, messages |
| `testing` | yes | Playwright TS-0042 |
| `backend-api` | no |  |

### Surface area

- Routes: /dashboard/gestiones, /presupuestos, /administracion/folios, /auditoria, /administracion/estados-gestion
- API: none

### Architecture review

No architecture change.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
