> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Follow-on to #2316. Skills: impeccable, motion-design; frontend-design is form-oriented so Pages reuse FactoryUI + framer-motion. RepoLens HTTP UI is a single Cytoscape index.html.

## Goals / Non-Goals

**Goals:** Clearer visual story and purposeful motion on the Pages RepoLens showcase.
**Non-Goals:** New metrics; Cytoscape redesign; product form theming.

## Decisions

1. **Reuse FlowDiagram + framer-motion** already on the site.
2. **Replace how-it-works card grid with pipeline + short prose** to avoid same-size card scaffolding.
3. **Leave Cytoscape explorer alone** after assessment (local tool UI, not the portfolio surface).
4. **Keep PageHeader eyebrow** for Factory shell consistency despite impeccable kicker ban on greenfield heroes.

## Riesgos / Trade-offs

- [Motion noise] → one focal pipeline + quiet chart entry; reduced-motion gates
- [Bundle] → no new animation libraries
- [Metric drift] → existing honesty tests remain

## Testing Strategy

RepoLensUiPolishTest covers pipeline export and chart tooltip markup. Existing RepoLensPagesTest keeps metric honesty. preflight --full.

## Regression Strategy

bash workspace/sdlc/preflight.sh --full

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
