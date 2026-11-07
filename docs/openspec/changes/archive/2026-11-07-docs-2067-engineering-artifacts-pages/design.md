> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The site already reads versions and counts from the repository (lib/tech-facts.ts, lib/ai-sdlc-facts.ts). The anatomy skill in .claude/skills/anatomy provides an isometric SVG kit with React components.

## Goals / Non-Goals

**Goals:** A newcomer can go from what Notaire is to how each artifact was produced, on one page, without leaving the site until they want the source file.
**Non-Goals:** No live data from GitHub at runtime; no WebGL; no change to the product.

## Decisions

Copy the anatomy kit's iso-kit.ts and React components into github-page/components/iso so the figures build with the site and need no runtime dependency. The card stacks in the cabinet grow with the logarithm of the file count, and the caption says so, because linear stacks would hide the small drawers. The lifecycle figure takes its steps and gates from LIFECYCLE_PHASES, the same data the page lists.

## Riesgos / Trade-offs

Large server payloads can trip the React key warning (anatomy references/react.md); every element prop gets a stable key. The figures add client JavaScript; the live code runs only while the figure is on screen.

## Testing Strategy

Vitest for the facts reader and the figure readouts; workspace/tests/test_pages_tech_docs.py for cited paths and no typed-in counts; e2e/site.spec.ts axe sweep covers the new route; screenshots at 1440 and 390 px.

## Regression Strategy

Existing site tests (site.test.ts search and site map), link check over the export.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
