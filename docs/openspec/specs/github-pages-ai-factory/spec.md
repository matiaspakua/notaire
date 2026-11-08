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
