> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Frontend CI runs `vitest --coverage`; the job fails when a metric dips under `thresholds`. Tests added since 2026-10-03 lifted real coverage far above the floor without the floor following.

## Goals / Non-Goals

**Goals:** a floor that catches regressions.
**Non-Goals:** new tests; the 80% target.

## Decisions

1. **Two points of headroom**, not one: local and CI runs differ slightly (jsdom timing, file discovery), and a flaky gate costs more than the extra points protect.
2. **Guard test mirrors the config** so a later lowering needs an explicit, reviewed edit of both.

## Riesgos / Trade-offs

- [CI measures lower than local] → 2-point headroom; CI runs the same command.

## Testing Strategy

`vitest-coverage-thresholds.test.ts` raised first (fails against the old config), then the config. `npx vitest run --coverage` proves the new floors hold.

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
