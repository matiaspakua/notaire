# Pages UX redesign — one site shell, findable docs, AI Factory storytelling

> Governed by [CONSTITUTION.md](../../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1970 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `docs/1970_pages_ux_redesign` |
| Gate 1 status | passed |

## Objetivo

The Owner reported that the public GitHub Pages site does not render its menu on mobile, that the technical docs
are hard to find, and that the style is mixed between the story, the Docs tab and the AI Factory. He asked for an
expert UX assessment and a redesign focused on the AI Factory, with better diagrams and a story worth reading.

## What Changes

- A written, prioritized UX assessment: `github-page/docs/ux-assessment.md`.
- One site shell in the root layout: a single header with primary entries AI Factory, Product, Technical docs and
  Releases; a mobile drawer (focus trap, Esc, `aria-expanded`); a static search (`/` or Ctrl/⌘ K); one footer; a
  skip link. It replaces `Nav`, `FactoryChrome`, `DocsChrome`, `Footer` and the custom cursor.
- One site map (`github-page/lib/site.ts`) drives the header, drawer, sidebars, breadcrumbs, previous/next links and
  search.
- One design system in `app/globals.css` (Tailwind v4 `@theme` tokens): one grey ramp, brand blue, a violet accent
  for the AI Factory, one display face, and accessible text colours.
- `DocShell` for `/docs/` and `/ai-sdlc/`: breadcrumbs, a sidebar (grouped as five acts in the factory), a reading
  progress bar, readable line length and previous/next navigation.
- AI Factory storytelling: the overview opens with the problem and the five-act map; a new `/ai-sdlc/results/`
  chapter shows numbers read from the repository at build time; new SVG diagrams for the guardrail layers and the
  human/agent swimlane; Mermaid themed from the design tokens, scrollable on mobile.
- Pages CI on every pull request that touches `github-page/`: typecheck, ESLint, Vitest, build, internal link check
  and a Playwright smoke suite with axe at 375 and 1280 px. `preflight.sh` mirrors it.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Every page shares one header, footer and design tokens | Owner request (#1970) | New |
| Technical docs are one click from every page | Owner request (#1970) | New |
| The mobile menu is keyboard- and screen-reader-accessible | WCAG 2.1 AA; `ui-design`, `motion-design` skills | Made explicit (tests) |
| Factory claims and numbers come from the repository | CONSTITUTION §7, §8; #1934 | Extended to the results chapter |
| A CI gate added here is mirrored in `preflight.sh` | CONSTITUTION §13 | Applied |

## Capabilities

### New Capabilities

- `github-pages-site-shell`

### Modified Capabilities

- `github-pages-ai-factory`

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | – |
| `frontend` | no | – |
| `infra` / observability | no | – |
| CI/CD (`.github`) | yes | new `pages-ci.yml` (PR check, no deploy permissions) |
| `docs` | yes | OpenSpec change; `CHANGELOG.md`; CONSTITUTION §13 site row |
| `workspace` | yes | `workspace/tests/test_pages_site_shell.py`, `test_pages_ai_sdlc.py`; `preflight.sh`; `github-page/` |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: `github-page` dev dependencies for ESLint, Vitest, Testing Library, Playwright and axe (versions aligned with `frontend/`); self-hosted Fraunces and JetBrains Mono fonts (`@fontsource-variable`)

### Architecture review

Static-export Pages app only. Mermaid stays the diagram language for structural diagrams (ADR-027); the two new
explanatory visuals are inline SVG components, like `GateFlow` from #1958. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Documentation entry |
| `CONSTITUTION.md` | §13 "Public documentation site" row: the site's sections and the Pages CI check |
| `github-page/README.md` | Structure, design system, site map and how to test |
| `github-page/docs/ux-assessment.md` | New: the assessment and what was fixed |

## Out of Scope

- A dark theme toggle (proposed as a follow-up decision for the Owner).
- Rewriting the story page's sections; they adopt the shared shell and tokens only.
