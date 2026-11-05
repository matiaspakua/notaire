> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Blocks Folio #579 and other tips from opening PRs under the #2001 local --full gate.

## Goals / Non-Goals

**Goals:** ['Observability ink passes axe on /product/platform/', 'No other platform axe regressions']
**Non-Goals:** ['Restyling other architecture layers unless they fail axe']

## Decisions

['Darken ink to rose-800-class #9f1239 rather than lightening glass (keeps visual hierarchy)']

## Riesgos / Trade-offs

['None material — one color token']

## Testing Strategy

["cd github-page && npx playwright test -g 'product/platform'"]

## Regression Strategy

['Other site.spec routes remain green']

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
