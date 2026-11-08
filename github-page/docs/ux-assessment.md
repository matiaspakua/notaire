# GitHub Pages UX assessment (#1970)

Audit of <https://matiaspakua.github.io/notaire/> on 2026-10-28, before the redesign, and of the
local production build after it. Method: headless Chromium screenshots at **375**, 768 and 1280 px;
**axe-core** (WCAG 2 A/AA tags) on each page; a horizontal-overflow probe; header height and nav
reachability per viewport. Screenshots were kept outside the repository (`/workspace/pages-redesign/`
on the audit machine); the checks that matter are now automated in `e2e/site.spec.ts`.

Severity: **P0** blocks a task or excludes a user group, **P1** hurts findability or trust,
**P2** polish.

## P0

| # | Finding (before) | Evidence | Fix |
|---|------------------|----------|-----|
| 1 | The home nav does not fit at 375 px and there is no menu button: AI Factory, Docs and GitHub are cut off. | Overflowing nav links at 375 and 768 px; `menuBtn: false` on every page and width. | One `SiteHeader` on every page. Below `lg` a menu button (`aria-expanded`, `aria-controls`) opens a modal drawer (`role="dialog"`, `aria-modal`), traps Tab, closes on Esc and returns focus. |
| 2 | The docs and AI Factory headers are walls of pills on mobile: 215 px and 219 px tall at 375, a third of the screen before any content. | Header height probe. | A 64 px header; section pages move to the drawer and a sidebar. |
| 3 | Colour contrast below 4.5:1: 8 nodes on home, 22 on lifecycle, more on the docs pages (`#0A84FF` text on white is 3.6:1; `slate-400` labels are 2.6:1). | axe `color-contrast`. | Shared tokens: links `brand-700`, labels `slate-500`+, accent text `factory-700`, emerald and amber one step darker. |
| 4 | Mermaid host `div` carried `aria-label` without a role. | axe `aria-prohibited-attr`. | Host is `role="img"` with the caption as its name; the inner SVG is `aria-hidden`. |
| 5 | `cursor: none` and a custom cursor hide the pointer for anyone whose JS is slow or who uses a magnifier. No skip link. | `globals.css`, `CustomCursor.tsx`. | Native cursor restored, custom cursor removed; skip link to `#main`; visible `:focus-visible` ring. |

## P1

| # | Finding (before) | Fix |
|---|------------------|-----|
| 6 | Three different chromes (home `Nav`, `DocsChrome`, `FactoryChrome`), each with its own logo, colours and links. Moving between sections felt like three sites. | One header, one footer, one `DocShell` for docs and factory pages. |
| 7 | Technical docs are hard to find: a "Docs" tab on some pages, nothing on others; no search; no breadcrumbs. | Top level is AI Factory, Product, Technical docs, Releases, one click from every page. Static search (`/`, Ctrl/⌘K) over every page title, summary and keyword. Breadcrumbs and a section sidebar. |
| 8 | Mixed design language: two gray ramps (`neutral` and `slate`), four blues, ad-hoc hex values, three fonts for headings. | Tailwind v4 `@theme` tokens: one neutral ramp, `brand`, `factory` (violet accent for the AI Factory), ink/surface/hairline, one radius and shadow, Fraunces for display and the system stack for text. |
| 9 | Every docs page had the same `<title>`. | `pageMetadata()` from the site registry: "Page — Section · Notaire" and the page summary as description. |
| 10 | The AI Factory pages were a set of reference pages with no order; no way to tell where a reader is in the story. | Five acts (problem → how a change flows → what keeps it safe → what we learned → apply it), an act pill, previous/next links, reading progress, a 75ch measure. |
| 11 | No checks run on pull requests that touch `github-page/`: a broken page reached `main` and was found only after deploy. | `pages-ci.yml`: typecheck, ESLint, Vitest, static export, internal link check, Playwright + axe at 375 and 1280 px. Mirrored in `preflight.sh`. |

## P2

| # | Finding (before) | Fix |
|---|------------------|-----|
| 12 | "Scroll to discover" overlaps the hero CTA at 375 px. | Hidden below `md` and moved below the buttons; hero height accounts for the sticky header. |
| 13 | Decorative blur blobs and the case-study table overflow at 375 px (horizontal scroll on the whole page). | `overflow-x: clip` on `html`/`body`; tables and wide diagrams scroll inside their own focusable frame. |
| 14 | Scrollable code blocks, tables and diagrams could not be reached by keyboard. | `tabIndex=0` with an accessible name (axe `scrollable-region-focusable`). |
| 15 | Inline links told apart from text by colour alone. | Links are underlined (axe `link-in-text-block`). |
| 16 | Animations ignore `prefers-reduced-motion`. | A global reduced-motion reset; the SVG explainers stop animating. |

## After

On the local production build, axe reports **no WCAG 2 A/AA violations** on all 21 routes at 375
and 1280 px, no page scrolls sideways, and the drawer, search and pager tests pass
(`npm run test:smoke`). `npm run check:links` resolves every internal link and fragment.

## Not done here (decisions for the Owner)

- **Dark theme.** Tokens are ready for it (`ink`, `surface`, accents are variables), but a second
  theme doubles the visual QA. Left for a follow-up.
- **Lighthouse.** Not run in CI (it needs a stable runner budget); axe covers the accessibility half.
- **Required check.** `pages-ci.yml` is not yet a required status check on `main`.

## Follow-up: interactive diagrams and portfolio landing (#1977)

| # | Finding | Fix |
|---|---------|-----|
| 1 | The hero animation and particle canvas ran on load and ignored `prefers-reduced-motion`. | Removed; the landing is static and the only motion is what the reader starts. |
| 2 | Diagrams were static Mermaid, or a looping gate animation with no pause or keyboard control. | `components/diagrams/`: one stepper (`useStepper`, `StepControls`) with Prev/Play/Next, arrow keys, an `aria-live` step panel and a step list; Play runs once and stops. |
| 3 | The testing page had a broken `(` link and no visual of the layers. | Rewritten around `TestPyramid` and counts from `testCounts()`. |
| 4 | Dimming inactive CI stages with opacity dropped text below 4.5:1 (axe `color-contrast`). | Inactive stages keep full-contrast text; the active one is marked by border and number colour. |
| 5 | Scaling an SVG node used the view-box origin, so outer nodes slid off the edge. | `transform-box: fill-box` with a centred origin. |

Checks: `e2e/site.spec.ts` adds `/docs/testing/` to the axe sweep, steps the request diagram with the
keyboard (and waits to prove nothing autoplays) and steps the pipeline under `reducedMotion: "reduce"`.
The portfolio critique is in [`portfolio-review.md`](portfolio-review.md).

## Follow-up: anatomy pages, glossary and content review (#2089)

| # | Finding | Fix |
|---|---------|-----|
| 1 | The site told what the product does and how agents are gated, but no page showed how one request moves through the system, or how development, QA, security and operations fit together over the environments. | Three step-through illustrated machines, one per question: `/product/anatomy/` (the notary's counter), `/docs/engineering/` (the release line) and `/ai-sdlc/anatomy/` (the lock chamber). Each step names the real repository paths, and a test checks that they exist. |
| 2 | Terms such as management, folio, ratchet or Foreman were used without a definition. | `/docs/glossary/`, linked from the three anatomy pages. |
| 3 | The reading path started at the product and jumped to the artifacts and the lifecycle. | Two anatomy pages join the path, so a reader goes from the basics (how it works) to the loop (engineering) to the rulebook (AI SDLC). |
| 4 | Stale or wrong claims in the story components: "Modern Microservices" over a layered backend, a hand-typed "786 commits in 2026" and "200+ E2E tests", a 2025 card that said Spring Boot 4. | Reworded or read from the metrics; the Timeline keeps its dated stack names because they describe the past. |
| 5 | The plate captions and legends of the three existing figures were 3.2:1 on the card, below WCAG AA (axe `color-contrast`). | `--anatomy-muted` is darkened for the light theme in `components/figures/figures.css`; the smoke suite now covers the new pages too. |
