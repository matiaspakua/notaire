# Design — Pages AI Factory section

## Context

`github-page/` is a Next.js static export (`output: "export"`, basePath `/notaire`) built and deployed by
`deploy-github-page.yml` after CI succeeds on `main`. Mermaid renders client-side in `components/MermaidDiagram.tsx`.

## Goals / Non-Goals

**Goals:** a separate, complete, grounded AI SDLC section. **Non-Goals:** changing the deploy workflow, the product docs tab, or the Constitution.

## Decisions

- Own route `/ai-sdlc/` with its own chrome (dark "factory" theme) so it cannot be mistaken for product docs.
- One content module holds diagrams and source paths through `src()`, `skill()`, `agent()` and `workflow()` helpers, so a test can resolve every citation.
- Counts (skills, agents, workflows, ADRs, OpenSpec changes, E2E suites, Use Cases) come from `node:fs` at build time, never hard-coded (Constitution §7 forbids hard-coded suite counts).

## Riesgos / Trade-offs

- Prose can drift from the source docs; the pages link the source of every claim, and the grounding test catches renamed files.

## Testing Strategy

| Scenario | Level | Test |
|----------|-------|------|
| Visitor reaches the factory from both entry points | unit | `test_section_is_a_top_level_route_apart_from_product_docs`, `test_story_nav_and_docs_tab_link_the_factory` |
| Every sub-page exists | unit | `test_every_subpage_exists` |
| A declared diagram is not orphaned | unit | `test_content_declares_every_required_diagram_as_mermaid`, `test_every_diagram_is_rendered_by_a_page` |
| A cited path is renamed | unit | `test_every_cited_path_exists`, `test_every_named_skill_agent_and_workflow_exists`, `test_counts_are_read_from_the_repository_at_build_time` |

## Regression Strategy

`workspace/tests` (including `test_pages_business_docs.py`), `docs/tests`, `npm run build` of `github-page`.

## Playwright Strategy

n/a for the product UI. The static build is rendered headless locally to check that every Mermaid diagram produces an SVG.

## Deployment Strategy

- Flyway migration required: no
- Feature flag: no
- Published by `deploy-github-page.yml` after the next green CI on `main`.

## Rollback Strategy

- Revert safe: yes
- Database rollback: none needed

## Migration Plan

1. Merge. 2. CI on `main` → Pages deploy. 3. Open `/notaire/ai-sdlc/`.

## Open Questions

None.
