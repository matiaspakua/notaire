# Design — interactive diagrams and the portfolio landing

## Context

After #1976 the site explains the architecture with Mermaid charts and tables. They are static, and the one animated
diagram (`GateFlow`) loops forever, which `.claude/rules/ui-ux-design.md` and the motion-design skill advise against
(no autoplay, at most two simultaneous animations, 150–300 ms, transforms and opacity only).

## Goals / Non-Goals

- Goals: one interactive diagram per architecture aspect, readable without motion or the drawing; a landing that a
  hiring manager understands in one screen; every number read from the repository.
- Non-goals: new product features, a redesign of the colour system, video or 3D effects.

## Decisions

- **The reader drives.** `useStepper` holds the step; Play advances once through every step and stops. It never
  starts by itself. `transition(reduced)` returns a zero duration when the reader asks for reduced motion.
- **The list is the diagram for everyone who cannot use the SVG.** `StepControls` renders Previous / Play / Next
  (arrow keys too), an `aria-live` panel with the current step and its source file, and an ordered list of steps
  as buttons with `aria-current="step"`. The SVG is `role="img"` with a title and description and is hidden below
  `md`, where the list carries the content.
- **Data, not drawings.** `FlowDiagram` draws nodes and edges from `lib/diagram-flows.ts`, where every class name and
  path is a real file (tested). The hexagon, pyramid, timeline and pipeline receive counts from `lib/tech-facts.ts`
  as props; `workflowTriggers()` reads each workflow's `on:` block, so the pipeline regroups itself.
- **Mermaid stays where it is the right tool.** The package map on the hexagonal page and the lifecycle charts
  remain; the payment sequence and the contract flow become interactive because stepping is what explains them.

## Riesgos / Trade-offs

- `GateFlow` is removed; its reduced-motion test moves to the new factory diagram.
- The SVGs are hidden below 768 px; the list version is complete but less visual.

## Testing Strategy

- Vitest + Testing Library (`tests/diagrams.test.tsx`): no autoplay, keyboard stepping, live region, play-once, step
  list, accessible SVG, data integrity of `lib/diagram-flows.ts`, trigger grouping, phases, counts.
- `workspace/tests/test_pages_diagrams.py`: shared stepper, reduced motion, placement on each page, cited files exist.

## Regression Strategy

- All existing Vitest, workspace (`quick.sh`) and Playwright suites stay green; `test_pages_ai_sdlc.py` follows the
  factory diagram that replaces `GateFlow`.

## Playwright Strategy

- `e2e/site.spec.ts`: the axe sweep covers every page with a diagram at 375 and 1280 px; a new test steps through
  the request flow with the keyboard and checks the live region on both projects.

## Deployment Strategy

- Merged by the Owner; `deploy-github-page.yml` publishes after CI on main. Smoke: open `/notaire/docs/hexagonal/`
  and step through the request flow at 375 and 1280 px.

## Rollback Strategy

Revert the PR. No data, schema or API is involved.
