# Design

## Context

#1058 put every screen in the sidebar; with 21 screens the flat list scrolls at 1440x900 and fills the mobile drawer (#1350). The grid in `app/dashboard/page.tsx` had its own 14-item array.

## Goals / Non-Goals

Goals: six labelled, collapsible sections; one config for sidebar and grid; RBAC unchanged except Auditoría aligned with its route guard. Non-goals: group crumb in the breadcrumb (follow-up), reordering routes, new screens.

## Decisions

A disclosure button inside each `<h2>` (WAI-ARIA disclosure pattern) instead of Radix Collapsible: no new dependency, and the heading stays in the outline. Collapsed keys (not expanded keys) are stored so a new section defaults to open. The active section is opened by adjusting state during render on a pathname change, not in an effect. Reading localStorage in the `useState` initializer is safe because the layout renders the sidebar only after client mount. Group labels live in `navGroups` because `i18n.test.ts` requires flat `navigation` values.

## Riesgos / Trade-offs

Users who collapse a section then deep-link elsewhere keep it collapsed (intended). Tests that click nav links are unaffected: every section is open by default.

## Testing Strategy

Vitest `dashboard-nav.test.ts` (#1367 block: section membership/order, translated labels, every top-level route exactly once, admin-only = `ADMIN_ROUTE_PREFIXES`, grid derived from featured items, no private grid array) and `app-sidebar.test.tsx` (six/five sections, Inicio outside, active section expanded, collapse persists, stored collapse never hides the current section). Written first: 11 failures observed.

## Regression Strategy

`bash frontend/verify.sh`; full Playwright chromium suite.

## Playwright Strategy

`TS-0121-grouped-sidebar.spec.ts`: section headings in order for an admin, active section open with `aria-current`, collapse survives reload and reopens on visiting its route, grid order, non-admin EMPLEADO sees five sections and no Auditoría/Administración. Observed failing 4/4 on the pre-change build.

## Deployment Strategy

Frontend only; rollback is reverting the PR (a stale `notaire.sidebar.collapsed` key is ignored by the old code).

## Rollback Strategy

Revert the commit.
