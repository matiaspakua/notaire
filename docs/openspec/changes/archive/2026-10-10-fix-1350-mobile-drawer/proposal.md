# The mobile navigation drawer is a modal sheet: focus, Escape, close button, scroll lock, aria-expanded

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1350 |
| Use Case | RNF-10 – Seguimiento del trabajo sobre ventanas; CU76 |
| Branch | `fix/1350_mobile_drawer` |
| Gate 1 status | draft |

## Objetivo

Below 768px the sidebar drawer left focus on the toggle, ignored Escape, let Tab reach the page behind the backdrop, had no close button, let the body scroll, and its toggle had no aria-expanded/aria-controls and a hardcoded English name.

## What Changes

- `AppSidebar`: the shared body (logo, user, nav, footer) renders in the static desktop `<aside>` and, below 768px, in a Radix Dialog sheet (`id=mobile-sidebar`): focus moves to the first nav link, Tab is trapped, Escape/outside click/close button close it and return focus to the toggle, page scroll is locked.
- Visible close button `btn-sidebar-close` named `navigation.closeMenu`; sheet title and nav `aria-label` `navigation.mainNav` (sr-only title).
- `dashboard/layout.tsx`: toggle gets `aria-expanded`, `aria-controls=mobile-sidebar` and the translated `navigation.openMenu`; the sheet closes when the viewport grows to desktop.
- i18n `navigation.openMenu`, `closeMenu`, `mainNav` in es and en. Test ids `sidebar`, `sidebar-backdrop`, `btn-sidebar-toggle` kept.
- Playwright TS-0041 (2 new cases); CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The mobile drawer follows the WAI-ARIA dialog pattern | #1350, WCAG 2.1.2/2.4.3/4.1.2 | Made explicit |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `ui-navigation`: Mobile navigation drawer behaviour.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | AppSidebar, dashboard layout, messages |
| `testing` | yes | Playwright TS-0041 |

### Surface area

- Every /dashboard route below 768px

### Architecture review

No architecture change; Radix Dialog is already a dependency.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
