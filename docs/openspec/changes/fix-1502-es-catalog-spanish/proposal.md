# Restore the Spanish es.json values the English rename turned English (#1502)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1502 |
| Use Case | RNF-04 – Internacionalización (es-AR default locale); RNF-06 – Usabilidad |
| Branch | `fix/1502_es_json_spanish` |
| Gate 1 status | draft |

## Objetivo

The English identifier rename (#1495) also replaced entity words inside Spanish UI copy, so Spanish users saw toasts and labels such as "Person actualizada", "Property creado", "Nuevo Notebook" and "Role". Code identifiers are English; es.json values stay Spanish. The 27 affected values are restored from the pre-rename catalog and a guard keeps them from coming back.

## What Changes

- `frontend/messages/es.json`: 27 values restored from `d4bd1693` (personas, gestiones.fields, inmuebles, protocolo.cuadernos, administracion.usuarios/conceptos/plantillas/roles, suplencias). Sentence-case changes from #1495 that stay Spanish ("Movimientos de testimonio", "Método de pago") are kept. `en.json` untouched.
- Vitest `es-catalog-spanish.test.ts`: fails when an es.json value contains an English entity name from the rename map ("Roles" excluded: it is also Spanish).
- Playwright `TS-0023`: the users table header is "Rol" again (the rename had changed the assertion to "Role").
- CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| es.json values are Spanish; only identifiers and keys are English | CONSTITUTION (English code, localized copy); #1490 scope | Made explicit |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `frontend-i18n-page-coverage`: Spanish UI copy stays Spanish after code renames.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | es.json, guard test |
| `testing/e2e` | yes | TS-0023 assertion |
| `backend-api` | no | — |

### Surface area

- Endpoints: none
- Entities / Flyway / configuration: none

### Architecture review

No architecture change: message catalog values only.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
