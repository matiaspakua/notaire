> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Frontend CI fails when a metric dips under `thresholds`. The page components only call fetch through TanStack Query and next-intl, so a mocked fetch and translator are enough to render them.

## Goals / Non-Goals

**Goals:** catch a crashing page in seconds; keep the floor honest.
**Non-Goals:** per-page behaviour tests.

## Decisions

1. **One parametrised file**, not 30: the pages share the same harness and the smoke contract is the same.
2. **Empty API for every endpoint** (list or page object by URL shape) keeps the test independent of response schemas.
3. **2 points of headroom** under the measurement, as in #1663.

## Riesgos / Trade-offs

- [Smoke tests pass on a page that renders but is wrong] → Playwright covers behaviour; this only guards render crashes.
- [CI measures lower than local] → 2-point headroom.

## Testing Strategy

`dashboard-pages-render.test.tsx` (31 cases) and `vitest-coverage-thresholds.test.ts` (raised first, red against the old config).

## Regression Strategy

Frontend CI: typecheck, ESLint, Vitest with coverage, build.

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
