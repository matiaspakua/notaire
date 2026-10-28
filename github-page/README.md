# Notaire — GitHub Pages site

Static Next.js site published at <https://matiaspakua.github.io/notaire/>
(`output: export`, and `basePath: /notaire` in production builds, set in [`next.config.ts`](next.config.ts)).
It is deployed by [`.github/workflows/deploy-github-page.yml`](../.github/workflows/deploy-github-page.yml)
after CI succeeds on `main` (or on manual dispatch).

Every page shares one shell (#1970): a single header with the primary navigation
(**AI Factory**, **Product**, **Technical docs**, **Releases**), an accessible mobile drawer,
site search (`/` or Ctrl/⌘K), and a footer. Docs and AI Factory pages add breadcrumbs,
a section sidebar, previous/next links and reading progress. The registry behind all of it
is [`lib/site.ts`](lib/site.ts): add a page there and it appears in the nav, the sidebar,
the search index and the pager. Design tokens (colours, type, radius, shadow) live in the
`@theme` block of [`app/globals.css`](app/globals.css); the AI Factory swaps the shared accent
for violet through `.theme-factory`. The UX audit behind this is [`docs/ux-assessment.md`](docs/ux-assessment.md).

The site has three parts. They are kept apart on purpose:

| Route | What it is | Source |
|-------|------------|--------|
| `/` | The story of the product | [`app/page.tsx`](app/page.tsx), `components/` |
| `/docs/` | **Product** docs: what Notaire does and how it is built (domain, architecture, API, operations) | [`app/docs/`](app/docs/), `lib/docs-content.ts` |
| `/ai-sdlc/` | **AI Factory**: the AI SDLC that builds Notaire, told in five acts (problem → how a change flows → what keeps it safe → what we learned and the results → how to apply it) | [`app/ai-sdlc/`](app/ai-sdlc/), [`lib/ai-sdlc-content.ts`](lib/ai-sdlc-content.ts), [`lib/ai-sdlc-facts.ts`](lib/ai-sdlc-facts.ts) |

The AI Factory pages only summarise. Every claim cites a repository file through
the `src()` / `skill()` / `agent()` / `workflow()` helpers. The test
[`workspace/tests/test_pages_ai_sdlc.py`](../workspace/tests/test_pages_ai_sdlc.py)
fails if a cited path does not exist. Counts (skills, agents, workflows, ADRs, use
cases, security families) are read from the repo at build time, so they never go
stale. The source of truth is still [`CONSTITUTION.md`](../CONSTITUTION.md) and
[`docs/300-development/304-ai-sdlc-cloud/`](../docs/300-development/304-ai-sdlc-cloud/).

The **case study** (`/ai-sdlc/case-study/`) charts monthly history from a committed snapshot,
[`lib/ai-sdlc-history.json`](lib/ai-sdlc-history.json), which records when and how it was generated.
Refresh it with `python3 workspace/ci/generate-ai-sdlc-history.py` (needs git and an authenticated `gh`).
The **framework** page (`/ai-sdlc/framework/`) presents [`framework/ai-sdlc/`](../framework/ai-sdlc/) and its
[report](../docs/300-development/306-ai-sdlc-framework/ai_sdlc_framework.md) (#1958).

Structural diagrams are Mermaid strings rendered on the client by
[`components/MermaidDiagram.tsx`](components/MermaidDiagram.tsx), themed from the CSS tokens.
Explainers are hand-built inline SVG with a `<title>`, a `<desc>` and a list fallback on narrow
screens: [`GateFlow`](components/GateFlow.tsx), [`GuardrailStack`](components/GuardrailStack.tsx)
and [`Swimlane`](components/Swimlane.tsx). Animations stop under `prefers-reduced-motion`.
The **results** page (`/ai-sdlc/results/`) prints only numbers read at build time.

## Local build

```bash
cd github-page
npm ci
npm run dev                         # http://localhost:3000/ (no basePath in dev)
npm run typecheck && npm run lint && npm test   # tsc, ESLint, Vitest
NODE_ENV=production npm run build   # static export into out/
npm run check:links                 # every internal href and #fragment in out/ resolves
npx playwright install chromium     # once
npm run test:smoke                    # Playwright + axe at 375 and 1280 px, served under /notaire
python3 -m pytest ../workspace/tests/test_pages_ai_sdlc.py ../workspace/tests/test_pages_site_shell.py -q
```

Pull requests run all of the above in [`pages-ci.yml`](../.github/workflows/pages-ci.yml);
`bash workspace/sdlc/preflight.sh` mirrors it (`--full` adds Playwright).

`prebuild` regenerates `lib/releases.json` from the product release manifest
(`workspace/release/product_release.py`).
