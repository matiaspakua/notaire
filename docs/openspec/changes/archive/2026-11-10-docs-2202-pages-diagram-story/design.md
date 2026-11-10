## Context

The public site already steps some diagrams. The factory steps said only Matias merges. The quality-gate picture was a static Mermaid chart. Lead time in the snapshot is a median and a p90, not a list of pull requests.

GitHub Pages is a Persuade surface (Fraunces, slate, brand blue, factory violet). It is not the product desk. Diagram plates stay on light paper so labels keep contrast in both themes.

## Goals / Non-Goals

**Goals**

- One visual key for flow nodes.
- A factory story that matches main, stepped from the Issue to close.
- A quality-gate stepper beside the map.
- A lead-time figure that does not invent a distribution.
- Spanish from the existing dictionary. Reduced motion. No layout shift. No new library.

**Non-Goals**

- Redrawing the isometric anatomy figures.
- Computing a histogram the snapshot does not store.
- Rebasing this branch. Main is merged in.

## Decisions

1. **Light plates stay.** Node fills are chosen for contrast on white. Dark mode does not invert them.
2. **Steps are the text alternative.** The SVG is named. The step list is what assistive tech reads.
3. **Two marks, not bins.** Median and p90 are the only lead-time numbers in `delivery-metrics.json`.

## Riesgos

- A later kit change (plugin allow list, merge rule) can make the sentences stale. They are tested against the words that are true today.
- The wide factory drawing is hidden below the `md` breakpoint. The step list is the phone version.

## Testing Strategy

- Vitest locks the factory sentences, the quality-gate order, the trust path, and the lead-time wording.
- Existing diagram tests still require steps not to play on their own.
- Playwright + axe on the pages suite, both themes.

## Regression Strategy

- `preflight.sh --full` is the regression. No product assertion is weakened.

## Playwright Strategy

- Existing `github-page/e2e/site.spec.ts` covers `/ai-sdlc/results/` and `/ai-sdlc/quality-gates/` for scroll and axe at 375 and 1280.

## Deployment Strategy

Merge to `main` publishes Pages. No workflow change. The Owner merges.

## Rollback Strategy

Revert the merge commit. The previous export is the previous `main` build.
