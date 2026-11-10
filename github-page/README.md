# Notaire GitHub Pages site

The public site at <https://matiaspakua.github.io/notaire/>: a Next.js static export (`output: "export"`, base path `/notaire`).
Everything that can change (versions, counts, test totals, workflows, ADRs) is read from the repository when `next build` runs, so a
page never types a number by hand.

## Sections

| Section | Routes | What it is for |
|---|---|---|
| Home | `/` | The portfolio landing: what was built, the proof and three illustrated machines. |
| Product | `/product/…` | What Notaire does, **how it works** (Anatomy Fig 1), its story, platform and numbers. |
| AI Factory | `/ai-sdlc/…` | The AI SDLC that builds Notaire, told as five acts. Includes the **AI SDLC anatomy** (Fig 3). |
| Security | `/security/…` | Trust boundaries, threats, toolchain and reporting. |
| Technical docs | `/docs/…` | Architecture, modules, database, API, testing, the **engineering anatomy** (Fig 2) and the glossary. |
| Releases | `/docs/releases/` | Every product cut. |

The site map lives in one place, `lib/site.ts`. Add a page there and it appears in the header, sidebars, breadcrumbs, previous/next
links and search; a registered href with no page file fails `npm test`.

## Theme and language

The header offers a theme (light, dark, or the system setting) and a language (English, Spanish, or the system language).
The choice is stored in `localStorage` (`notaire.theme`, `notaire.lang`) and applied by a script in the document head, before paint.
`prefers-reduced-motion` already collapses transitions. Spanish covers the shell, the home page, the product, security,
the AI Factory, the about page and every technical-docs page, including the glossary, the stack, business, modules,
hexagonal architecture, the database, the API, security docs and the roadmap. One dictionary (`lib/i18n/`) is the source
of the sentences. Technical terms such as Issue, pull request, gate, OpenSpec, Flyway, commit and kit stay in English.

## Commands

```bash
npm ci
npm run dev              # http://localhost:3000
npm run typecheck && npm run lint && npm test
npm run build            # static export to out/
npm run check:links      # every internal link and fragment in out/
npm run test:smoke       # Playwright + axe at 375 and 1280 px, against out/ (build first)
```

## Anatomy figures

The three step-through machines in `components/anatomy/` are isometric SVG built at build time with the kit in `components/iso/`
(the `anatomy` skill in `.claude/skills/`):

| Figure | File | Page |
|---|---|---|
| Fig 1, the notary's counter | `system-figure.ts` | `/product/anatomy/` |
| Fig 2, the release line | `engineering-figure.ts` | `/docs/engineering/` |
| Fig 3, the lock chamber | `sdlc-figure.ts` | `/ai-sdlc/anatomy/` |

Each figure exports its SVG, its legend and an ordered list of steps (`figure-types.ts`). A step names a station to light, the pose of the
moving parts, a readout and a `where` line of real repository paths. `tests/anatomy.test.tsx` fails if a step lights a station that does not
exist, moves a part that does not exist, or points at a path that is not in the repository. `AnatomyFigure.tsx` is the only client code: it
moves the named parts, dims the other stations and drives the shared `StepControls`. Nothing plays on its own.

## Rules for content

- Read a fact from the repository (`lib/tech-facts.ts`, `lib/ai-sdlc-facts.ts`) instead of typing it.
- Coverage in `public/metrics.json` comes from `workspace/ci/generate-github-page-metrics.sh`. When the JaCoCo snapshot or Vitest
  summary is missing, percentage fields are JSON `null` and `lastUpdated` is `unknown` — never a fabricated `0` (#2203).
- Describe the CI model as it is: a pull request is proved by the local `preflight.sh --full`; the product workflows run on a version tag or
  on demand (see `/ai-sdlc/quality-gates/`).
- Colour contrast must stay at WCAG AA; `npm run test:smoke` runs axe on every page.

See also `docs/ux-assessment.md`, `docs/portfolio-review.md` and `docs/demo.md`.
