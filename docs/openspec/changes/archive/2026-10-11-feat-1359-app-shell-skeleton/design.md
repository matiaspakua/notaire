# Design

## Context

The dashboard layout is a client component gated on a hydration flag (Zustand reads the session from localStorage after mount). Rendering `null` on the server and before mount made the first frame blank. Three components hand-rolled `animate-pulse` blocks; under reduced motion the global CSS shortened the animation but did not express the intent.

## Goals / Non-Goals

Goals: the server HTML and the first frame of any dashboard URL show the shell; no layout shift when the real shell mounts; one Skeleton primitive; no pulsing under reduced motion. Non-goals: route-level `loading.tsx` suspense; per-page loading states beyond `DataTable` (pages keep their existing ones).

## Decisions

The skeleton is returned from the same branch that returned `null`, so the auth and redirect logic stays identical. It mirrors the real layout's structure (sidebar `w-72` only from `md`, header row with `px-6 lg:px-8 pt-4`, content `p-6 lg:p-8`) so the swap does not move content. The container carries the accessible state (`role="status"`, `aria-busy`, label) and the blocks are `aria-hidden`. `motion-safe:animate-pulse` puts the reduced-motion rule in the class instead of relying on the global override. `StatValue` keeps a `<span>` block (it sits inside a `<p>`), with the same motion-safe class.

## Riesgos / Trade-offs

Unauthenticated visitors see the skeleton for the instant before the redirect instead of a blank page, which is the intended behavior.

## Testing Strategy

Vitest `app-shell-skeleton.test.tsx`: server render (no effects) contains the skeleton and not the page; no session keeps the skeleton and redirects; with a session the real shell replaces it; skeleton attributes and sidebar width; `Skeleton` classes and `aria-hidden`; `DataTable` loading rows are 5 Skeleton blocks on a busy table; guard against plain `animate-pulse`. Written first and red (modules missing).

## Regression Strategy

`bash frontend/verify.sh`; full Playwright chromium suite.

## Playwright Strategy

TS-0125: with JavaScript disabled (the first paint), `/dashboard/personas` shows the busy skeleton and its sidebar; with JavaScript the real sidebar replaces it and cumulative layout shift stays under 0.05. Red on the pre-change build.

## Deployment Strategy

Frontend only.

## Rollback Strategy

Revert the commits.
