# Fix i18n page coverage for remaining dashboard pages and login leftovers

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1059 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/fix-1059-i18n-page-coverage-69d3` |
| Gate 1 status | passed (artifacts ready for implement) |

## Objetivo

Several dashboard pages and login leftover strings still hardcode Spanish and
never call `useTranslations`, so switching locale to English (TS-0040) leaves
those surfaces in Spanish. Close the ADR-015 coverage gap for the remaining
user-visible UI without redesigning screens.

## What Changes

- Wire `useTranslations` on gap UI pages: workflows list, workflows editor,
  suplencias, reportes, roles (complete remaining strings), and
  `dashboard/items` (canonical Items UI; `administracion/items` only redirects).
- Translate login leftovers: connection, lockout, validation, welcome, forgot,
  footer.
- Fix auditoria leftover filter label (`Todos los módulos`) on the canonical
  `dashboard/auditoria` page.
- Add/extend `messages/{es,en}.json` namespaces so both catalogs stay key-synced.
- Extend `i18n.test.ts` so missing required keys/namespaces fail (AC).
- Optionally extend TS-0040 to assert EN titles on 1–2 former gap pages.
- Update permanent docs (CU76 pointer, ADR-015 pointer, E2E mapping, CHANGELOG).

Redirect stubs with no UI strings remain exempt: `/`, `/auditoria`,
`administracion/items`, `administracion/auditoria`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Every user-visible UI string on covered pages MUST come from `messages/{es,en}.json` via next-intl | ADR-015; #1059 AC; CU76 | Made explicit for remaining gap pages + login leftovers |
| Spanish and English catalogs MUST expose identical key structures; missing required namespaces/keys MUST fail unit tests | ADR-015; #1059 AC | Changed (extend integrity test) |
| Redirect-only stubs without UI copy are out of i18n wiring scope | #1059 decisions | Made explicit |

## Capabilities

### New Capabilities

- `frontend-i18n-page-coverage`: Remaining dashboard pages and login leftovers
  resolve all user-visible strings through next-intl catalogs; unit tests fail
  when required namespaces or keys are missing.

### Modified Capabilities

- (none under `openspec/specs/` today cover client i18n page coverage)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | yes | pages, message catalogs, unit i18n test, optional TS-0040 |
| `frontend-swing` | no | Removed / out of scope |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | Existing frontend / Playwright jobs cover this |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none (uses existing next-intl)

### Architecture review

Follows ADR-015 (next-intl + `messages/{es,en}.json`) and the established
`administracion.usuarios` CRUD translation pattern (`t` + `tc("common")`).
No new ADR required; update ADR-015 with a short coverage pointer only.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | Pointer/AC note that remaining dashboard pages + login leftovers are covered by i18n catalogs (#1059) |
| `docs/200-architecture/202-ADR/ADR-015-internationalization.md` | Short pointer that page coverage includes former gap modules |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | Note TS-0040 coverage extension for gap-page EN titles if added |
| `CHANGELOG.md` | `[Unreleased]` entry for i18n page coverage |

## Out of Scope

- Visual redesign or copy rewrite beyond translating existing strings.
- Translating redirect stubs that have no UI.
- Backend localization / Accept-Language.
- Completing leftover hardcoded strings on already-partial pages outside the
  listed gap set (e.g. usuarios column leftovers) unless needed for shared keys.
- Merging the PR (draft only).
