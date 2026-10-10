# Skip link and focus on route change (#1352)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1352 |
| Use Case | CU76 – Navegar la aplicación; RNF-10 – Accesibilidad (WCAG 2.1 AA: 2.4.1 Bypass Blocks, 2.4.3 Focus Order) |
| Branch | `feat/1352_skip_link_route_focus` |
| Gate 1 status | draft |

## Objetivo

Keyboard users had to Tab through the user card, 22 sidebar links, the language switcher and logout before reaching page content, on every page. There was no skip link, and `<main>` had no id to target. After a client-side navigation, focus stayed on the clicked sidebar link, so screen readers did not announce the new page.

## What Changes

- `components/layout/SkipLink.tsx`: a "Saltar al contenido" / "Skip to content" link, the first focusable element of the dashboard layout. It stays visually hidden until it gets keyboard focus. Activating it focuses `<main>` without changing the URL hash.
- `app/dashboard/layout.tsx`: `<main id="main-content" tabIndex={-1}>`; each page is wrapped in `[data-route-path=<pathname>]` inside the existing page transition.
- `hooks/useRouteFocus.ts`: when the pathname changes (not on the first load, and not for query-only changes such as `?page=`), focus moves to the `<h1>` of the page wrapper for the current pathname, with `tabindex="-1"` and `preventScroll`. If no heading appears within about 1s, focus falls back to `<main>`.
- i18n `navigation.skipToContent` (es, en).
- Vitest `dashboard-layout.test.tsx`; Playwright `TS-0108-keyboard-navigation.spec.ts`; CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The first Tab on any dashboard page reaches a visible skip link that moves focus to the page content | #1352, RNF-10 (WCAG 2.4.1) | New |
| After a client-side navigation, focus is on the new page heading | #1352, RNF-10 (WCAG 2.4.3) | New |
| The first load and query-only URL changes do not move focus | #1352 | Made explicit |

## Capabilities

### New Capabilities

- `keyboard-navigation`: a skip link to the main content, and heading focus after route changes.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | dashboard layout, SkipLink, useRouteFocus, messages es/en |
| `testing` | yes | Playwright TS-0108 |
| `backend-api` | no | — |

### Surface area

- Routes: every `/dashboard/**` page (shared layout)
- API: none

### Architecture review

No architecture change. A live-region announcement of the page title was the issue's alternative; heading focus already makes screen readers read the new title, so the live region is not added.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Added entry |
