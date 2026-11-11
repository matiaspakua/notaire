# Spanish for remaining GitHub Pages bodies (anatomy, Mermaid, captions)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2202 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `docs/2202_pages-remaining-spanish` |
| Gate 1 status | passed |

## Objetivo

#2209–#2232 registered Spanish routes and content-module dictionaries, but anatomy figures, Mermaid sentence labels, diagram captions, and a few shell labels still render English when Spanish is selected. This change finishes those surfaces so #2202 acceptance criteria hold end to end.

## What Changes

- Spanish `pair()` entries for the three anatomy figures (plate title, hint, caption, keys, step title/text/readout) and for Mermaid sentence labels plus Diagram captions.
- `AnatomyFigure` applies the site locale through `tr()` so plate HTML and step readouts stay Spanish when the reader steps.
- Footer docs links and the “AI Factory chapters” sidebar label follow the same dictionary.
- Coverage and anatomy unit tests fail when those Spanish keys are missing; Playwright samples `/product/anatomy/` and `/ai-sdlc/lifecycle/` in Spanish.

English technical terms stay in English: Issue, pull request, gate, OpenSpec, Flyway, commit, kit, Owner (and paths, commands, class names).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Every user-visible sentence on a translated route has a Spanish dictionary entry; a missing entry fails the unit test. | #2202 AC, github-pages-site-shell | Changed (extends to anatomy + Mermaid) |
| Diagram sentence labels translate; identifiers stay as in the repository. | #2202 AC | Made explicit |
| English technical terms stay in English where translation would be worse. | #2202 AC | Unchanged |

## Capabilities

### New Capabilities

### Modified Capabilities

- `github-pages-site-shell`: Spanish coverage includes anatomy figures, Mermaid sentence labels, and diagram captions on already-translated routes.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `github-page` | yes | i18n dictionary, AnatomyFigure, DocShell, SiteFooter, tests, Playwright |
| `docs` | yes | This OpenSpec change; site-shell delta on archive |
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: none
- Endpoints: none
- Database / Flyway: none
- BREAKING: none

## Documentation Impact

- `github-page/README.md` — note anatomy/Mermaid Spanish coverage if the Spanish section is incomplete.
- OpenSpec main spec `github-pages-site-shell` updated on archive.
- `CHANGELOG.md` `[Unreleased]` only if no other Unreleased writer holds the fence; otherwise leave to the open release PR.

## Out of Scope

- Wilson / portal / Context Lake / `[committer] required=true`
- Reversing #2201 shell/home work
- A third locale
- Product app i18n (`frontend/messages`)
- Redrawing isometric SVG geometry
