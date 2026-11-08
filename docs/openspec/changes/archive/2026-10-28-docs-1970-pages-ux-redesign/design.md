# Design — Pages UX redesign

## Context

`github-page/` is a Next.js 16 static export (basePath `/notaire`) deployed by `deploy-github-page.yml`. Before this
change it had three unrelated chromes: a fixed glass nav on the story (no mobile menu; links overflow off-screen at
375 px), a light sticky header on `/docs/`, and a dark two-row header on `/ai-sdlc/` (219 px tall on mobile). No
pull-request check built, linted or tested the app. The assessment is in `github-page/docs/ux-assessment.md`.

## Goals / Non-Goals

**Goals:** one accessible shell and design system; technical docs findable in one click and by search; the AI
Factory as a five-act story with themed, mobile-safe diagrams; a PR check for the Pages app.
**Non-Goals:** new product content; a dark theme; changes to the deploy workflow.

## Decisions

- **One registry.** `lib/site.ts` lists every section, page, summary and keyword. Header, drawer, sidebars,
  breadcrumbs, previous/next and search all read it, so navigation cannot drift between sections, and a test fails
  any href without a page file.
- **Shell in the root layout.** `SiteHeader` and `SiteFooter` render once in `app/layout.tsx`; `DocShell` wraps
  `/docs/` and `/ai-sdlc/`. The factory keeps a violet accent (`--color-factory-*`), the docs the brand blue.
- **Drawer, not a dropdown.** Below `lg` the header shows search and a menu button. The drawer is a modal dialog with
  a focus trap (`useFocusTrap`), Esc and backdrop close, scroll lock and focus return; it shows the four entries and
  the current section's pages.
- **Static search.** The site map doubles as the search index (titles, summaries, keywords); no build plugin needed.
- **Tokens through Tailwind v4 `@theme`.** The neutral ramp maps onto slate, so story and docs share one grey;
  brand text uses `brand-700` for 4.5:1 contrast. Display type is Fraunces (self-hosted), body stays the system UI
  stack for speed, mono is JetBrains Mono.
- **Diagrams.** Mermaid renders with `theme: "base"` and variables read from the CSS tokens. Wide diagrams keep a
  readable minimum width and scroll inside their figure on small screens. `GuardrailStack` and `Swimlane` are
  inline SVG with `<title>`/`<desc>`, and an ordered-list fallback on narrow screens; animation honours
  `prefers-reduced-motion`.
- **Removed:** the custom cursor (`cursor: none` hid the system pointer site-wide).

## Riesgos / Trade-offs

- A bigger dependency set for `github-page` (dev only). Mitigation: versions match `frontend/`; the deploy only uses
  the runtime ones.
- The Playwright smoke suite adds about two minutes to PRs that touch `github-page/`; it runs only on those paths.

## Testing Strategy

| Scenario | Level | Test |
|----------|-------|------|
| Mobile menu opens, traps focus, closes on Esc | component + e2e | `tests/SiteHeader.test.tsx`; `e2e/site.spec.ts` |
| Technical docs one click from every page | component + e2e | `SiteHeader.test.tsx`; `e2e/site.spec.ts` |
| Search finds a page | component + e2e | `tests/SearchDialog.test.tsx`; `e2e/site.spec.ts` |
| Every nav href has a page | unit | `tests/site.test.ts` |
| No horizontal scroll at 375 px, no axe violations | e2e | `e2e/site.spec.ts` |
| Every internal link and anchor resolves | build | `scripts/check-links.mjs` |
| One shell, one token set, PR check exists | unit | `workspace/tests/test_pages_site_shell.py` |
| Results and SVG diagrams are grounded and accessible | unit | `workspace/tests/test_pages_ai_sdlc.py` |

## Regression Strategy

All `workspace/tests`, `docs/tests`, the existing `test_pages_ai_sdlc.py` and `test_pages_business_docs.py`, and the
production build of `github-page`.

## Playwright Strategy

`github-page/e2e/site.spec.ts` runs against the static export served under `/notaire` at 375 px (mobile project)
and 1280 px (desktop project). `testing/e2e` covers the product UI and is not touched.

## Deployment Strategy

- Flyway migration required: no
- Feature flag: no
- Published by `deploy-github-page.yml` after the next green CI on `main`.

## Rollback Strategy

- Revert safe: yes
- Database rollback: none needed

## Migration Plan

1. Merge. 2. CI on `main`, then the Pages deploy. 3. Open `/notaire/`, `/notaire/docs/` and `/notaire/ai-sdlc/` at
375 px and check the menu.

## Open Questions

- Should the site get a dark theme toggle? Tokens are ready for it; it is out of scope here.
