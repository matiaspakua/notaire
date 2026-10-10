# Design

## Context

The three cards read totalElements from size=1 page queries (#1397).

## Goals / Non-Goals

Goal: honest loading and error states and a guard against list downloads. Non-goals: a dedicated count endpoint (page totals are already a few hundred bytes).

## Decisions

Reuse the existing page hooks instead of a new apiGetCount helper: they already request size=1 and share the cache key shape with the list pages.

## Riesgos / Trade-offs

None: presentation only.

## Testing Strategy

dashboard-stat-value.test.tsx (module missing) and TS-0118 failed first (test commit).

## Regression Strategy

`bash frontend/verify.sh`; Playwright chromium suite against the branch build.

## Playwright Strategy

TS-0118 on /dashboard.

## Deployment Strategy

Frontend only; ships with the next frontend image.

## Rollback Strategy

Revert the commit.
