> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

See proposal.md — Objetivo. `AppHeader` already provides sticky title + actions;
empty tables use an ad-hoc icon + paragraph. Dashboard home hand-rolls an `h1`.

## Goals / Non-Goals

**Goals:**
- One token-backed API for page title, empty invitation, and section rhythm.
- Pilots prove the pattern without a wholesale rewrite.

**Non-Goals:**
- Replacing every `AppHeader` call site with a different sticky behaviour.
- Adding chart libraries or workflow motion changes.

## Decisions

1. **PageHeader is the content API; AppHeader wraps it** — keeps sticky glass in one
   place; dashboard home can use PageHeader without the glass bar.
2. **EmptyState is presentational** — callers pass title / description / action;
   DataTable keeps `emptyMessage` as the title when no richer props are passed.
3. **No ALL-CAPS on touched table headers** — DataTable column headers drop
   `uppercase tracking-wider` (design-system restraint) in this PR.
4. **Motion** — EmptyState uses a single subtle fade via existing utilities; no
   stagger on empty.

## Riesgos / Trade-offs

- [Risk] Visual shift on all DataTable empties → Mitigation: same icon + copy path;
  layout only.
- [Risk] Header case change surprises users → Mitigation: intentional; AC requires it.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| PageHeader renders title and actions | unit | `page-chrome.test.tsx` |
| EmptyState shows action when provided | unit | `page-chrome.test.tsx` |
| PageSection groups title and children | unit | `page-chrome.test.tsx` |
| Dashboard uses PageHeader / PageSection | unit | `dashboard-pages-render.test.tsx` or chrome test |
| People / managements still render | unit | existing page tests |

## Regression Strategy

- Existing tests affected: DataTable consumers, AppHeader callers, dashboard render.
- Full suite: `bash frontend/verify.sh`
- Bruno: n/a

## Playwright Strategy

- Specs: existing login → people smoke if available; or dashboard render via existing suite.
- Golden path: open people list empty/non-empty still works.
- Viewports: 320 / 768 / 1024 on dashboard and people.
- Command: path-scoped CI Playwright on the PR.

## Deployment Strategy

- Flyway: no
- Frontend-only deploy
- Feature flag: no
- Smoke: `/dashboard`, `/dashboard/people`

## Rollback Strategy

- Safe revert; no schema/API change.
