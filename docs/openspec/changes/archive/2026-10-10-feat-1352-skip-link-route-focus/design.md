# Design

## Context

`app/dashboard/layout.tsx` renders the sidebar, then a header row (mobile menu button and breadcrumb), then `<main>` with `AnimatePresence` + `PageTransition key={pathname}`. Each page renders its own `AppHeader` with the only `<h1>`. The Next.js App Router keeps focus where it was after `<Link>` navigation.

## Goals / Non-Goals

Goals: a WCAG 2.4.1 bypass for the sidebar, and focus on the new page heading after navigation. Non-goals: skip links on the login page, which has no repeated navigation; a live-region announcer; reordering the sidebar (#1367).

## Decisions

- The skip link is `sr-only` with `focus:not-sr-only focus:fixed`, so it shows over the top-left corner without reflowing the layout. It uses the `primary` token and the shared focus ring. `onClick` focuses the target and calls `preventDefault`, so the URL keeps no `#main-content` hash, and Enter (which fires click) behaves the same.
- `<main tabIndex={-1}>` with `focus:outline-none`: it is a programmatic focus target only, so it is never in the Tab order.
- Route focus waits for the heading that belongs to the **current** pathname wrapper. With `AnimatePresence mode="wait"` (or any exit animation), the outgoing page's `<h1>` is still mounted when the pathname changes. Focusing it would lose focus to `<body>` once it unmounts. The extra wrapper is a plain block `div` inside the motion wrapper, so layout is unchanged.
- The hook retries every 50ms, up to 20 times (about 1s), because the new page mounts after the transition. If no heading appears, it falls back to `<main>`, so focus never stays on a link that is no longer relevant.
- `preventScroll` keeps the scroll reset that Next.js does itself.

## Riesgos / Trade-offs

Mouse users also get focus moved to the heading. That is invisible, because programmatic focus after a pointer click does not match `:focus-visible`. #1433 edits the same `<main>` block (motion tokens), so whichever PR merges second needs a small merge.

## Testing Strategy

`dashboard-layout.test.tsx` (4 of 5 failed first: no skip link, no main id, catalog key missing, focus left on the link): the skip link is the first focusable element and targets main; activating it focuses main; es/en labels; the first load keeps `document.body` focused; a pathname change focuses the new `<h1>` with `tabindex=-1`. TS-0108 (2 of 3 failed first on the pre-change build): the first Tab focuses a visible skip link, Enter focuses main and the next Tab stays in main; a sidebar click focuses the Gestiones `<h1>`; the first load keeps focus on `<body>`.

## Regression Strategy

`bash frontend/verify.sh`; Playwright TS-0108, TS-0041 (responsive, mobile drawer focus) and TS-0045 (breadcrumb), plus the full chromium suite against the branch build.

## Playwright Strategy

TS-0108-keyboard-navigation.spec.ts, chromium, against the production build.

## Deployment Strategy

Frontend only; ships with the next frontend image.

## Rollback Strategy

Revert the commits; no data or API impact.
