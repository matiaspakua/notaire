# Notaire — GitHub Pages site

Static Next.js site published at <https://matiaspakua.github.io/notaire/>
(`output: export`, and `basePath: /notaire` in production builds, set in [`next.config.ts`](next.config.ts)).
It is deployed by [`.github/workflows/deploy-github-page.yml`](../.github/workflows/deploy-github-page.yml)
after CI succeeds on `main` (or on manual dispatch).

The site has three parts. They are kept apart on purpose:

| Route | What it is | Source |
|-------|------------|--------|
| `/` | The story of the product | [`app/page.tsx`](app/page.tsx), `components/` |
| `/docs/` | **Product** docs: what Notaire does and how it is built (domain, architecture, API, operations) | [`app/docs/`](app/docs/), `lib/docs-content.ts` |
| `/ai-sdlc/` | **AI Factory**: the AI SDLC that builds Notaire (lifecycle, guardrails, quality gates, OpenSpec, agents and skills, governance, lessons, playbook) | [`app/ai-sdlc/`](app/ai-sdlc/), [`lib/ai-sdlc-content.ts`](lib/ai-sdlc-content.ts), [`lib/ai-sdlc-facts.ts`](lib/ai-sdlc-facts.ts) |

The AI Factory pages only summarise. Every claim cites a repository file through
the `src()` / `skill()` / `agent()` / `workflow()` helpers. The test
[`workspace/tests/test_pages_ai_sdlc.py`](../workspace/tests/test_pages_ai_sdlc.py)
fails if a cited path does not exist. Counts (skills, agents, workflows, ADRs, use
cases, security families) are read from the repo at build time, so they never go
stale. The source of truth is still [`CONSTITUTION.md`](../CONSTITUTION.md) and
[`docs/300-development/304-ai-sdlc-cloud/`](../docs/300-development/304-ai-sdlc-cloud/).

Diagrams are Mermaid strings rendered on the client by
[`components/MermaidDiagram.tsx`](components/MermaidDiagram.tsx).

## Local build

```bash
cd github-page
npm ci
npm run dev                         # http://localhost:3000/ (no basePath in dev)
NODE_ENV=production npm run build   # static export into out/
python3 -m pytest ../workspace/tests/test_pages_ai_sdlc.py -q
```

`prebuild` regenerates `lib/releases.json` from the product release manifest
(`workspace/release/product_release.py`).
