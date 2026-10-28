> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

See proposal.md. EmptyState / LoadingState / ErrorState already exist (#1942/#1947).

## Goals / Non-Goals

**Goals:** Shared async chrome on viewer/hero; sentence-case type badges.

**Non-Goals:** Tracker SVG layout rewrite; editor validation UX.

## Decisions

1. Keep existing `data-testid`s (`workflow-empty`, `workflow-skeleton`, `workflow-not-found`, `wf-viewer`).
2. Hero not-found uses ErrorState (role=alert) without retry (user corrects search).
3. Node badge: drop uppercase; keep translated type label.

## Riesgos / Trade-offs

- [Risk] Visual change on badges → Mitigation: intentional; covered by node-box tests still seeing type text.

## Testing Strategy

| Scenario | Test |
|----------|------|
| Viewer empty uses EmptyState | `workflow-viewer.test.tsx` |
| Hero empty uses EmptyState + link | `dashboard-hero.test.tsx` |
| Node badge not uppercase CSS | `workflow-node-box.test.tsx` or source assert |

## Regression Strategy

- Existing workflow-* unit suite must stay green.

## Playwright Strategy

- Path-scoped CI; preserve testids.

## Deployment Strategy

- Frontend-only.

## Rollback Strategy

- Safe revert.
