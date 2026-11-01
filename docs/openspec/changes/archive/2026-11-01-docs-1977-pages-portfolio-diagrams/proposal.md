# Pages — interactive diagrams, site polish and a portfolio-ready landing

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1977 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `docs/1977_pages_portfolio_diagrams` |
| Gate 1 status | passed |

## Objetivo

The Owner wants the GitHub Pages site to be a key portfolio project that shows how he uses AI to build a
state-of-the-art product. He asked for interactive, dynamic diagrams for each aspect of the architecture, built with
the repo's motion-design, ui-design and frontend-design skills, and for the whole site to be reviewed for coherence,
explanation and UI/UX. This change builds on #1976 (product sidebar and architecture internals).

## What Changes

- A shared, reader-driven stepper (`components/diagrams/`): Previous / Play / Next, arrow keys, a live region and a
  list of steps that doubles as the small-screen and screen-reader version. Nothing autoplays; with reduced motion
  every change is instant.
- Seven interactive diagrams, fed by `lib/tech-facts.ts` at build time:
  hexagon map and one request through the payment slice (`/docs/hexagonal/`), Flyway timeline (`/docs/database/`),
  OpenAPI contract flow (`/docs/api/`), test pyramid (`/docs/testing/`), CI/CD pipeline grouped by workflow trigger
  (`/docs/stack/`), and the AI SDLC factory (`/` and `/ai-sdlc/`). The factory replaces the looping `GateFlow`.
- A portfolio landing: a value proposition, real counts from the repository, a short case study path and an app
  screenshot gallery.
- A written portfolio review (`github-page/docs/portfolio-review.md`) and an updated UX assessment.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Motion communicates, is short and never autoplays | `.claude/rules/ui-ux-design.md`, motion-design skill | Made explicit for diagrams |
| Every diagram works without the SVG (keyboard, screen reader, 375 px) | WCAG 2.1 AA, ui-design skill | New |
| Counts on the site come from the repository | CONSTITUTION §7, §8 | Extended to the diagrams |

## Capabilities

### New Capabilities

- `github-pages-interactive-diagrams`

### Modified Capabilities

- none

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | – |
| `frontend` | no | – |
| `infra` / observability | no | – |
| CI/CD (`.github`) | no | – |
| `docs` | yes | OpenSpec change; CHANGELOG |
| `workspace` | yes | `workspace/tests/test_pages_diagrams.py`, `test_pages_ai_sdlc.py`; `github-page/` |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none (framer-motion is already a dependency of `github-page`)

### Architecture review

Static-export Pages app only. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `github-page/docs/portfolio-review.md` | New: hiring-manager review and what was acted on |
| `github-page/docs/ux-assessment.md` | Diagrams, motion and the landing |
| `github-page/README.md` | `components/diagrams/` and `lib/diagram-flows.ts` |
| `CHANGELOG.md` | Documentation entry |

## Out of Scope

- New product features or backend changes.
- Findings unrelated to the site; each is tracked as its own issue.
