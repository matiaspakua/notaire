# The read-only demo (`/demo/`)

> Issue #1985 · Published at <https://matiaspakua.github.io/notaire/demo/>

The Pages site serves the **real Notaire frontend** with sample data. Visitors sign in with any name and
password, browse every screen and cannot change anything. A banner on every page reads
"Demo – sample data, read-only".

## How it works

| Piece | File | What it does |
|-------|------|--------------|
| Recorder | `demo/record-fixtures.mjs` | Signs in to the seeded local stack, opens every route of the app with Playwright and saves each `GET /api/v1/**` answer, plus the login answer, to `demo/fixtures.json`. Values under secret-looking keys (`password`, `token`, `hash`, …) become `"demo"`. |
| Fixtures | `demo/fixtures.json` | The recorded answers, keyed `"GET /path?query"`. Committed, so the build needs no backend. |
| Shim | `demo/demo-shim.js` | Inlined in `<head>` of every demo page. Replaces `fetch` for `/api/**` only. |
| Build | `demo/build-demo.mjs` (`npm run build:demo`) | Copies `../frontend` to `.demo-build/`, applies the patches below, runs `next build` and copies the export to `out/demo/`. |

The shim answers:

- **GET**: the exact `path?query` from the fixtures, else the same path with any query, else **404**. It never
  answers 401: the app reacts to 401 by sending the visitor to `/login?expired=1`, which is outside the
  `/notaire/demo` base path.
- **`POST /users/login`**: the recorded admin user, whatever the credentials.
- **Logout and client-error beacons**: a no-op.
- **Any other POST, PUT, PATCH or DELETE**: 403 "Demo is read-only", and **no request leaves the browser**.

The patches a static export needs, applied to the copy only (`frontend/` is never modified):

1. Drop `src/app/api` (the backend proxy route), `src/proxy.ts` (edge auth and CSP nonce) and the tests.
2. Fixed `en` locale instead of the `NEXT_LOCALE` cookie; the language switch is a no-op.
3. No `headers()` nonce in the root layout; add the shim and the banner.
4. `next.config.ts`: `output: "export"`, `basePath: "/notaire/demo"`.
5. `generateStaticParams` for the one dynamic route (`/dashboard/administration/workflows/[id]`), one page per
   recorded workflow.

Each patch checks the text it replaces. If the frontend changes underneath it, `build:demo` fails with
`build-demo: patch for <file> no longer applies`; Pages CI runs on `frontend/**` changes to catch that before merge.

## Refreshing the fixtures

```bash
# 1. Start the seeded local stack (backend on :8080 with Flyway demo data, frontend on :3000).
# 2. From github-page/:
APP_URL=http://localhost:3000 DEMO_USER=admin DEMO_PASSWORD=admin node demo/record-fixtures.mjs
# 3. Rebuild and check:
npm run build && npm run build:demo && npm run test:smoke
```

Review the diff of `demo/fixtures.json` before committing it: the tests reject JWTs, BCrypt hashes and unscrubbed
secret-looking keys, but personal data is only as safe as the seed data it came from (Flyway demo seeds, no real
people).

## Why this approach

- **Not MSW.** Mock Service Worker needs a service worker registered at the scope root and its handler set wired
  into the app. Under the `/notaire/demo/` base path on GitHub Pages that means a scope header the host cannot set,
  or a worker file inside the export plus app code to start it. The frontend already sends every API call through
  `fetch("/api/v1/…")` in `src/lib/api-client.ts`, so replacing `fetch` with one inlined script covers the same
  ground with no app change.
- **Not hand-written mocks.** Fixtures recorded from the real stack match the real API, and nothing in them is
  invented. Hand-written handlers drift and need maintenance per endpoint.
- **Not a screenshot tour.** The static export worked: about 40 routes, about 13 MB. A tour stays the fallback if
  the frontend ever needs a server to render.
- **Not a hosted backend.** GitHub Pages serves static files only and the Owner ruled out paid hosting. Options
  and costs for a demo with a real backend are in [#1986](https://github.com/matiaspakua/notaire/issues/1986).

## Limits

- Data is a snapshot from the recording date shown in the banner. Searches and filters that were not recorded
  return the unfiltered list or nothing.
- Saving, deleting and server-side validation do not work; the app shows its normal error toast.
- English only.
