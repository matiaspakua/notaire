> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

See proposal.md — Objetivo. EmptyState exists (#1942). DataTable already skeletons
while loading but maps errors to an empty table.

## Goals / Non-Goals

**Goals:**
- Shared LoadingState / ErrorState with token typography and optional retry.
- Pilots prove list + home failure UX.

**Non-Goals:**
- Rewriting every dashboard route.
- Changing React Query defaults globally.

## Decisions

1. **ErrorState mirrors EmptyState layout** — same vertical rhythm; AlertCircle +
   title + description + optional action.
2. **LoadingState is a labelled busy panel** — Skeleton blocks + `aria-busy` +
   sr-only/common loading text; respects `motion-safe`.
3. **DataTable owns the error branch** — when `error` is set, skip empty/rows and
   show ErrorState (keeps callers thin).
4. **Dashboard retry** — refetch the failed home queries via their `refetch()`.

## Riesgos / Trade-offs

- [Risk] Callers forget onRetry → Mitigation: ErrorState works without action;
  pilots always pass retry.
- [Risk] i18n missing retry key → Mitigation: add `common.retry` in en/es.

## Testing Strategy

| Scenario (spec) | Test level | Test file |
|-----------------|------------|-----------|
| LoadingState is busy | unit | `async-states.test.tsx` |
| ErrorState shows retry | unit | `async-states.test.tsx` |
| DataTable error branch | unit | `async-states.test.tsx` or data-table test |

## Regression Strategy

- Existing page tests must stay green; extend people/managements if they assert empty-on-error.
- `bash frontend/verify.sh` / vitest + tsc + eslint

## Playwright Strategy

- Path-scoped CI Playwright; no new TS unless a selector breaks.
- Spot-check people list when API down is hard in CI — covered by unit.

## Deployment Strategy

- Frontend-only; no Flyway; no feature flag.
- Smoke: `/dashboard`, `/dashboard/people`, `/dashboard/managements`

## Rollback Strategy

- Safe revert of the feature commit.
