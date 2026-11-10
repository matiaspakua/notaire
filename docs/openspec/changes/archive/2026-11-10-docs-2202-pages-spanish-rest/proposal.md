# Spanish for the remaining public pages

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2202 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/pages-spanish-rest-2277` |
| Gate 1 status | passed |

## Objetivo

A reader who selects Spanish can follow the product, the security section, the AI Factory, and the technical-docs reading path without an “this page is still in English” notice. Deeper technical pages that this change does not translate keep that notice.

## What Changes

- One dictionary keyed by the English sentence (`github-page/lib/i18n/`). Pages are not forked.
- Spanish for product, security, every AI Factory page, `/docs/` plus the reading-path pages (artifacts, architecture, engineering, testing, releases), the about page, and the demo banner.
- Sidebar titles for those sections follow the same choice.
- A unit test fails when a translated route has no Spanish entry for the strings it renders from the content modules. Playwright checks a Spanish heading on one page in each section, in light and dark, at 375 and 1280 px.
- English technical terms stay in English: Issue, pull request, gate, OpenSpec, Flyway, commit, kit, Owner.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Public copy has one source. A missing Spanish entry for a translated route fails the test. | CONSTITUTION.md §2 KISS, DRY | New |
| A page that is not translated stays in English and says so. | docs/openspec/specs/github-pages-site-shell/spec.md | Changed |
| English technical terms stay in English. | #2202 acceptance criteria | Made explicit |

## Capabilities

### New Capabilities

### Modified Capabilities

- `github-pages-site-shell`: which routes render in Spanish, and which still show the English notice.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| github-page | yes | Dictionary, translated routes, demo banner, tests |
| docs | yes | This OpenSpec change, then the archived spec |

No backend, schema, or product `frontend/` i18n change.

## Documentation Impact

- `github-page/README.md` — which routes are Spanish.
- `CHANGELOG.md` — `[Unreleased]`.
- OpenSpec main spec `github-pages-site-shell` updated when this change is archived.

Pages under `/docs/` that are not on the reading path stay in English with the notice. A follow-up can translate them with the same dictionary.
