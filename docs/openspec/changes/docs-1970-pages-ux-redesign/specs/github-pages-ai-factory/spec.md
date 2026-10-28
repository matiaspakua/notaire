## MODIFIED Requirements

### Requirement: AI Factory is a top-level section apart from the product docs

The GitHub Pages site SHALL publish the AI SDLC as its own top-level section under `/ai-sdlc/`, inside the shared site
shell with its own accent colour, and SHALL NOT place it under the product `/docs/` tab. The site header SHALL link it
from every page.

#### Scenario: Visitor reaches the factory from both entry points

- **WHEN** a visitor opens the story site or the Docs tab
- **THEN** a link to `/ai-sdlc/` is present, and the section lives outside `app/docs/`

## ADDED Requirements

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
