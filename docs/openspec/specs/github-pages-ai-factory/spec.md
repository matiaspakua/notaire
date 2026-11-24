# github-pages-ai-factory Specification

## Purpose

The GitHub Pages "AI Factory" section (`/ai-sdlc/`) that explains the AI SDLC building Notaire, kept apart from the product docs and grounded in cited repository files. Source: #1934; owner CU76.

## Requirements

### Requirement: AI Factory is a top-level section apart from the product docs

The GitHub Pages site SHALL publish the AI SDLC as its own top-level section under `/ai-sdlc/`, inside the shared site
shell with its own accent colour, and SHALL NOT place it under the product `/docs/` tab. The site header SHALL link it
from every page.

#### Scenario: Visitor reaches the factory from both entry points

- **WHEN** a visitor opens the story site or the Docs tab
- **THEN** a link to `/ai-sdlc/` is present, and the section lives outside `app/docs/`

### Requirement: The section covers the full AI SDLC

The section SHALL have pages for the factory overview, the lifecycle step by step, guardrails, quality gates and CI, OpenSpec adaptations, agents and skills, governance (issues, labels, milestones, PRs, releases), lessons learned, and a playbook for applying the approach to another project.

#### Scenario: Every sub-page exists

- **WHEN** the site is built
- **THEN** `/ai-sdlc/lifecycle/`, `/guardrails/`, `/quality-gates/`, `/openspec/`, `/agents/`, `/governance/`, `/lessons/` and `/playbook/` are exported

### Requirement: Diagrams are Mermaid and rendered

The section SHALL draw its diagrams in Mermaid (ADR-027): factory vs product, lifecycle flow, guardrail layers, quality-gate pipeline, roles, OpenSpec flow, issue state machine and PR sequence. Each diagram SHALL be rendered by a page.

#### Scenario: A declared diagram is not orphaned

- **WHEN** the content module declares a diagram
- **THEN** at least one `/ai-sdlc/` page renders it through `MermaidDiagram`

### Requirement: Content is grounded in the repository

Every repository path the section cites SHALL exist, every skill, agent and workflow it names SHALL exist in its catalog, and counts SHALL be read from the repository at build time.

#### Scenario: A cited path is renamed

- **WHEN** a cited file is moved or deleted without updating the section
- **THEN** `workspace/tests/test_pages_ai_sdlc.py` fails

### Requirement: The AI Factory tells the Notaire case study step by step

The AI Factory section MUST include a `/ai-sdlc/case-study/` page that walks from the problem to the solution in ordered chapters, shows the repository history as data read from a committed snapshot with provenance, and pairs each chapter with real evidence: an issue, a file in the repository, a before/after example or a code excerpt.

#### Scenario: the case study page exists and is linked

- **WHEN** the site is built
- **THEN** `/ai-sdlc/case-study/` renders and the section navigation links it

#### Scenario: history comes from a snapshot with provenance

- **WHEN** the case study shows commits, merged PRs or bug issues per month
- **THEN** the numbers come from `github-page/lib/ai-sdlc-history.json`, which records when and by which command it was generated

### Requirement: The AI Factory explains the reusable framework

The AI Factory section MUST include a `/ai-sdlc/framework/` page that presents the stack-agnostic framework (components, stages and gates, adoption levels, profile example) and links the framework report and kit in the repository.

#### Scenario: the framework page cites the kit

- **WHEN** the framework page is built
- **THEN** it cites `framework/ai-sdlc/` and the report through `src()` so the Pages test proves both exist

#### Scenario: animation respects reduced motion

- **WHEN** a visitor prefers reduced motion
- **THEN** the animated gate flow renders as a static diagram

### Requirement: The AI Factory reads as a story in five acts

The section SHALL order its pages as five acts — the problem, how a change flows, what keeps it safe, what was
learned (lessons and results), and how to apply it — and every page SHALL show its act, a reading progress
indicator and previous/next links in that order.

#### Scenario: Reader follows the story

- **WHEN** a reader finishes the Lifecycle page
- **THEN** the page links the previous chapter (Case study) and the next one (OpenSpec)

### Requirement: Results are read from the repository

The section SHALL include a `/ai-sdlc/results/` page whose numbers come from the repository at build time
(`lib/ai-sdlc-facts.ts`) or from the committed history snapshot with provenance, never typed into the page.

#### Scenario: A count changes

- **WHEN** a skill, workflow or archived OpenSpec change is added and the site is rebuilt
- **THEN** the results page shows the new count without an edit

### Requirement: Explanatory diagrams are accessible on every screen

Mermaid diagrams SHALL use one theme derived from the design tokens and scroll inside their figure on small screens.
The guardrail layers and the human/agent swimlane SHALL also be drawn as inline SVG with a title and description,
with a list fallback on narrow screens, and animation SHALL respect `prefers-reduced-motion`.

#### Scenario: Swimlane lanes follow the Constitution roles

- **WHEN** the swimlane is rendered
- **THEN** it has lanes for the Owner, the Worker agent, the Foreman agent and CI

### Requirement: A newcomer can read the factory in order

The AI Factory overview SHALL list a reading order that starts at the overview and ends at the playbook, with each step linking a real factory page. The order SHALL move from what the factory is, through one change and the gates, to the reusable kit.

#### Scenario: The path is complete

- **WHEN** the reading order is read
- **THEN** every step href is a factory page that exists, the first href is `/ai-sdlc/`, and the last href is `/ai-sdlc/playbook/`

### Requirement: The framework page separates shipped behavior from planned work

The framework page SHALL describe kit commands that exist on `main` and SHALL label work that is not done as planned. Plugin ids shown as loaded SHALL be the `allow` list in `.aisdlc/aisdlc.toml`. Planned inventory rows SHALL be read from `.aisdlc/migration-inventory.toml`. The page SHALL NOT say that a missing bubblewrap probe means the process is sandboxed, and SHALL NOT present Spec Kit or AWS AI-DLC as loaded plugins.

#### Scenario: Loaded plugins match the profile

- **WHEN** the site is built
- **THEN** the loaded plugin names equal the allow list in `.aisdlc/aisdlc.toml`

#### Scenario: Planned moves stay planned

- **WHEN** the migration inventory marks `workspace/sdlc` as planned
- **THEN** the framework page lists that path under planned work

### Requirement: The factory diagram matches the kit on main

The public factory diagram SHALL step one change from a GitHub Issue to a closed Issue. It SHALL say that the pull-request proof is `preflight.sh --full` on the agent machine, that product CI runs on a version tag, that the Foreman may merge a Worker pull request, that the Owner merges protected paths, and that Workers never merge. It SHALL say that `aisdlc foreman plan` does not execute agents and that the loaded plugins are openspec and example-method. It SHALL NOT say that only the Owner merges.

#### Scenario: Merge rule

- **WHEN** a visitor steps to the merge step
- **THEN** the text says Workers never merge and names the Foreman

### Requirement: Lead time is the snapshot, not a histogram

The results page SHALL show the lead-time median and p90 from `delivery-metrics.json`. It SHALL say that those two numbers are not a histogram of every pull request.

#### Scenario: Snapshot without samples

- **WHEN** the delivery snapshot has a median and a p90
- **THEN** the page shows both and says it is not a histogram

### Requirement: RepoLens page lives in the AI Factory

The AI Factory section SHALL include `/ai-sdlc/repolens/` in the shared document shell, linked from the factory navigation and kept outside product `/docs/`.

#### Scenario: Visitor opens RepoLens from the factory sidebar

- **WHEN** a visitor opens any `/ai-sdlc/` page
- **THEN** the sidebar lists a RepoLens entry whose href is `/ai-sdlc/repolens/`

#### Scenario: Home and reading path surface RepoLens

- **WHEN** a visitor opens the home page or the factory reading path
- **THEN** a link to `/ai-sdlc/repolens/` is present

### Requirement: RepoLens story covers basics, mechanics, factory plug-in, and the learning loop

The RepoLens page SHALL explain in plain English what RepoLens is, how it works (SQLite+FTS5, tree-sitter, embeddings, typed Personalized PageRank, CLI/HTTP/MCP), how it plugs into factory stages (plan/red/pre-pr, advisory signal gate, per-worktree index, shared vector cache), and the self-improvement loop with cited fixes such as CLI usage telemetry and ambiguous same-directory call edges.

#### Scenario: Reader learns the factory plug-in

- **WHEN** a reader opens `/ai-sdlc/repolens/`
- **THEN** the page names plan, red, and pre-pr MCP stages and the advisory `repolens report --gate` signal

#### Scenario: Learning loop cites real fixes

- **WHEN** the learning-loop section is read
- **THEN** it cites repository evidence for usage telemetry and ambiguous-call fixes without inventing outcomes

### Requirement: Benchmark charts are generated from committed result files

The page SHALL render interactive charts for the latest committed offline retrieval and agent benches (hit@10 / recall@10, latency, bytes per query, agent tool calls) and the live factory-loop A/B comparison from committed markdown/JSON under `framework/repolens/bench/results/`. Numbers SHALL match those files (or the report generator's published headline labels derived from them); charts SHALL show sample size and caveats; dark mode and ES/EN SHALL work, keeping English technical terms where clearer.

#### Scenario: Chart data tracks the committed snapshot

- **WHEN** the Pages app builds
- **THEN** chart values come from a build-time or committed snapshot sourced from `framework/repolens/bench/results/` and unit tests fail if key published ratios drift from the JSON sources

#### Scenario: Theme and locale

- **WHEN** a visitor toggles dark mode or Spanish
- **THEN** the RepoLens page remains readable and translated routes include `/ai-sdlc/repolens/`

### Requirement: RepoLens page explains ranking with an interactive pipeline

The `/ai-sdlc/repolens/` page SHALL include a stepped diagram that walks parse → store → embed → rank → serve, using the site's FlowDiagram pattern, with reduced-motion support.

#### Scenario: Reader steps the ranking pipeline

- **WHEN** a visitor opens `/ai-sdlc/repolens/` and advances the pipeline controls
- **THEN** successive nodes and edges highlight to show how a query becomes ranked hits

#### Scenario: Reduced motion

- **WHEN** the visitor prefers reduced motion
- **THEN** the pipeline and chart entrances render without continuous or large animated transitions

### Requirement: Charts stay honest and become clearer

Benchmark charts SHALL keep values from the committed RepoLens bench snapshot, SHALL expose per-bar values on hover or keyboard focus, and SHALL remain readable in light and dark themes.

#### Scenario: Chart tooltip

- **WHEN** a visitor focuses or hovers a bar
- **THEN** the formatted value for that arm is announced or shown without changing the underlying number

#### Scenario: Snapshot still gates honesty

- **WHEN** unit tests run
- **THEN** headline factors still match the committed JSON via the existing snapshot loader

### Requirement: Spanish pages carry no English prose

Every translated route SHALL read in Spanish without English prose, apart from quoted Issue titles and real command
output. Text nodes and the `aria-label`, `title`, `alt` and `placeholder` attributes under `#main` SHALL be localised,
including content rendered after the page mounts.

#### Scenario: a route read in Spanish

- **WHEN** `e2e/spanish-coverage.spec.ts` reads a translated route with the Spanish preference
- **THEN** no line of English prose remains

#### Scenario: a table's label and late content

- **GIVEN** a table labelled `Table: Role, Who` and a node added after the first pass
- **WHEN** the page is localised to Spanish
- **THEN** the label reads `Tabla: Rol, Quién` and the late node is translated

### Requirement: Published numbers come from their sources

The Pages metrics SHALL read the coverage floors from `pom.xml` and `vitest.config.ts`, the Owner's target, and the AI
tools configured in the repository from `metrics.json`; copy that counts stations, agents, gates or phases of the
factory line SHALL match its data; the roadmap snapshot SHALL be refreshable without changing GitHub.

#### Scenario: coverage floors

- **WHEN** `generate-github-page-metrics.sh` runs
- **THEN** `coverageFloors` equals the minimums in `pom.xml` and `vitest.config.ts`, and `coverageTarget` is 90

#### Scenario: the factory line copy

- **WHEN** a station is added
- **THEN** `factory-line-counts.test.ts` fails until the copy says the new count

#### Scenario: a read-only roadmap refresh

- **WHEN** `apply_milestone_policy.py --snapshot` runs
- **THEN** both snapshot files are written and nothing is posted to GitHub

### Requirement: Pages deploys in its own job

`deploy-github-page.yml` SHALL upload the Pages artifact in a `build` job and deploy it in a `deploy` job that needs
`build` and owns the `github-pages` environment.

#### Scenario: a push to main

- **WHEN** a commit lands on `main`
- **THEN** the `deploy` job starts after `build` uploaded the artifact and deploys it on the first attempt
