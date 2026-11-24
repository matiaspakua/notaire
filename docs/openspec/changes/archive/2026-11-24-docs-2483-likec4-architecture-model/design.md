> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

ADR-027 made Mermaid the only diagram language, yet the SAD keeps nine PlantUML fences and its context, building-block and legacy sections describe a system that no longer exists. Pages draws its own hand-written system context. LikeC4 1.60 offers a typed C4 DSL, deployment and dynamic views, a static interactive site and a programmatic API for computed views.

## Goals / Non-Goals

**Goals:** One model built from the code; generated Mermaid in Markdown so GitHub still renders it; an interactive site on Pages; a drift test that needs no Node; every architecture gap filed as an Issue.
**Non-Goals:** Replacing business diagrams (use cases, sequences per CU, state machines); fixing the code findings in this PR.

## Decisions

D1 LikeC4 1.60.0 pinned exactly (released 2026-10-15; 1.61 is under two weeks old), in its own npm project because it needs React 19.3 while Pages pins 19.2.4.
D2 A small generator (`scripts/mermaid.mjs`, pure and unit-tested) converts computed views to Mermaid instead of `likec4 gen mermaid`, which drops step numbers, sequence variants, merged-edge labels and finding styles.
D3 Generated Mermaid is committed and embedded between `likec4-view` markers, so GitHub renders the docs without a build; the Python test compares text only.
D4 The interactive site is built in the Pages workflows with `--use-hash-history`, because GitHub Pages only serves the root 404.html and deep links would land on the Next.js 404.
D5 Icons are omitted: LikeC4 loads some from a CDN and the site stays self-contained.
D6 PlantUML sources superseded by model views move to docs/000-archive; business PlantUML stays.

## Riesgos / Trade-offs

Generated files can be edited by hand: the docs test and `npm run check` fail. The LikeC4 site adds about 7 MB to the Pages artifact. A LikeC4 upgrade can change computed views: regenerate and review the diff.

## Testing Strategy

`node --test scripts/` unit-tests the Mermaid conversion (shapes, nesting, classes, merged edges, sequence and numbered dynamic views, unsafe characters). `docs/tests/test_architecture_model.py` checks the pin, one `.mmd` per view, verbatim doc blocks, the Pages data and that findings link Issues. Vitest `tests/architecture-model.test.tsx` checks the Pages data, grouping, links, the component and the Spanish strings. Pages Playwright smoke covers the new route.

## Regression Strategy

Existing docs tests, Pages Vitest, typecheck, lint, link check and Playwright smoke stay green; the architecture-diagram English test drops the assertions for the archived PlantUML files only.

## Playwright Strategy

Pages smoke (`npm run test:smoke`) visits /docs/architecture/model/ at 375 and 1280 px with axe; product frontend untouched.

## Deployment Strategy

Ships with the next push to main through deploy-github-page.yml.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
