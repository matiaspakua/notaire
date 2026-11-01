# Design — live read-only demo, delivery outcomes and an About section

## Context

The Pages site is a static Next.js export on GitHub Pages under `/notaire`. GitHub Pages cannot run the Spring
backend, and the Owner ruled out paid hosting. The frontend talks to the API only through `src/lib/api-client.ts`
(`/api/v1/**` with `fetch`), which makes `fetch` the one seam to replace.

## Goals / Non-Goals

**Goals**
- Visitors use the real UI, with real seeded data, and cannot change anything.
- Outcome numbers are reproducible from GitHub and show their window and method.
- The About section states only sourced facts.

**Non-Goals**
- Server-side behaviour in the demo (search, validation, writes).
- Changing `frontend/` source for the demo's sake.
- Changing the Actions run-retention policy (the restore and first-push metrics depend on it; see Risks).

## Decisions

1. **Static export of the real frontend, not a screenshot tour.** A spike exported 42 routes (about 13 MB) after
   five mechanical patches that `build-demo.mjs` applies to a temporary copy: drop `src/app/api`, `src/proxy.ts` and
   tests; fixed `en` locale instead of cookies; no-op `setLocale`; no `headers()` nonce in the root layout; export
   `next.config` with `basePath: "/notaire/demo"`; `generateStaticParams` for the one dynamic route. Every patch
   asserts its target text, so a frontend change fails the build loudly instead of shipping a broken demo.
2. **A fetch shim, not MSW.** MSW needs a service worker registered at the scope root and its own handler set; the
   shim is one classic script in `<head>`, loaded before the app chunks, and it replays recorded answers. GETs match
   `path?query` first, then the path. An unmatched GET gets a 404 JSON body, never a 401: a 401 would trigger the
   app's session-expiry redirect to `/login`, outside the base path. `POST /users/login` succeeds with the recorded
   user for any credentials. Every other write returns 403 "Demo is read-only" without touching the network.
3. **Fixtures recorded, not written.** `record-fixtures.mjs` drives Playwright through every route of the real app
   on the seeded local stack and stores the GET answers. Values under keys that look like secrets are replaced with
   `"demo"`.
4. **Metrics in a committed snapshot, refreshed at deploy.** The committed JSON keeps local builds offline. The
   deploy job already has the default `GITHUB_TOKEN` with read access, so it re-runs the generator before
   `npm run build`. A scheduled job that commits to `main` was rejected: `main` takes changes through PRs only.
5. **About in one file.** `lib/about.json` holds every fact with its source, and `placeholders` lists what only the
   Owner can supply. The page renders facts only.

## Riesgos / Trade-offs

- The Actions cleanup workflow deletes all but the latest run per workflow each night, and check runs go with them.
  Time-to-restore and first-push CI pass rate are therefore stored as unavailable with that reason, not computed
  from one or two surviving runs.
- Fixtures go stale as the API changes; the demo shows the data from its recording date, which it states.
- The demo adds `npm ci` in `frontend/` and a second Next build to Pages CI (about 2 minutes).

## Testing Strategy

- Vitest: the shim (GET replay, query fallback, 404 for unknown, login, read-only writes, no network for writes),
  delivery metrics rendering (values and window from the snapshot, unavailable metrics show their reason) and the
  About section (facts only, no placeholder text).
- Python (`workspace/tests/test_pages_demo_outcomes_about.py`): fixtures contain no tokens or secret-looking values;
  the metrics snapshot has a window, a method and no invented keys; `about.json` facts all have sources; the
  workflows build the demo and refresh the metrics.
- Playwright: `/demo/` shows the banner, signs in and lists seeded data; a write is refused; axe at 375 and 1280 px.

## Regression Strategy

The existing Pages suites (Vitest, link check, Playwright + axe, `test_pages_*.py`) run unchanged. No backend or
frontend source changes, so their suites are unaffected.

## Playwright Strategy

The Pages smoke suite (`github-page/e2e/`) gets a demo spec and covers `/about/` and the results page at 375 and
1280 px. `testing/e2e` is not affected.

## Deployment Strategy

Merged to `main`; `deploy-github-page.yml` refreshes the metrics, builds the site and the demo, and publishes.

## Rollback Strategy

Revert the PR. The demo is a separate folder in the export, so removing the build step removes it, and the site
pages fall back to their previous content.
