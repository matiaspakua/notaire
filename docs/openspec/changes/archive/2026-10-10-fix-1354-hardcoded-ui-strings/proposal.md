# UI strings come from the message catalogs, with a static guard

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1354 |
| Use Case | RNF-05 – Aspecto visual; ADR-015 (i18n) |
| Branch | `fix/1354_hardcoded_strings` |
| Gate 1 status | draft |

## Objetivo

With the English locale, Spanish text remained on many screens (breadcrumb, column headers, form labels, placeholders, report buttons, status badges, the payment balance panel, the empty table message) and the language switcher had an English aria-label in the Spanish UI. The breadcrumb also title-cased unknown slugs, dropping accents.

## What Changes

- `Breadcrumb`: labels from `breadcrumb.segments.*` and `breadcrumb.admin.*` (administration sub-routes have their own labels); no Spanish map and no title-casing fallback; record ids are shown as is; the nav is named by `breadcrumb.label`.
- About 50 literals in copias, protocolo, documentos, escrituras, gestiones, inmuebles, pagos, administración (usuarios, conceptos, trámites, folios), `DataTable` (default empty message = `common.noData`), `WorkflowViewer` and `LanguageSwitcher` (`role=group` + `common.languageSelector`) moved to `messages/es.json` and `messages/en.json` (80 keys, parity kept). Spanish values are unchanged.
- Vitest `hardcoded-ui-strings.test.ts`: static scan of `src/app` and `src/components` for literal attributes, column headers, toasts, default props, JSX text and accented literals, with a 3-entry allowlist (brand, two backend status codes); `breadcrumb.test.tsx` requires a label in both catalogs for every dashboard route.
- Playwright TS-0040: EN pagos, copias, protocolo, administración/usuarios and movimientos-testimonio show English breadcrumb, headers and buttons; ES accessible names are Spanish. CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| No user-visible or assistive-technology string is hardcoded | #1354, ADR-015 | Enforced by a static test |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `ui-i18n`: Localized UI strings.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | Breadcrumb, DataTable, WorkflowViewer, LanguageSwitcher, 11 pages, messages/*.json |
| `testing` | yes | Playwright TS-0040 |

### Surface area

- Breadcrumb on every dashboard page
- 11 dashboard pages listed above

### Architecture review

No architecture change; next-intl as before.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
