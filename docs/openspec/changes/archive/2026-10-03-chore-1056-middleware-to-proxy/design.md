> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #1056 (audit-2026-09, FRONTEND, tech-debt, priority:medium, CU84):
Next 16 deprecates `middleware.ts`. Verified on `origin/main` tip
`ce97e114` (2026-10-03):

| Location | Finding |
|----------|---------|
| `frontend/package.json` | `"next": "^16.3.8"` (issue text cites 16.3.4 warning) |
| `frontend/src/middleware.ts` | Sole edge file; `export function middleware`; `config.matcher` present |
| Edge role | Auth redirect + admin UX deny via `AUTH_STATUS_COOKIE` / `AUTH_ROLE_COOKIE` |
| Skips | `/_next`, favicon, `/api/`, paths with `.` |
| `frontend/src/proxy.ts` | **Absent** on main |
| `next.config.ts` | Separate `rewrites()` API proxy to `BACKEND_URL` + static CSP headers |
| #1052 | Shipped — admin role/status UX cookies; helpers in `admin-access.ts` |
| #1051 | OPEN — HttpOnly JWT + CSP nonce; design may inject nonce via middleware |
| Related | #1043 / #1045 open (suggested queue ahead); Playwright-heavy serialize |

**#1051 interaction (must not regress):** Today edge never reads the JWT — only
non-HttpOnly UX markers. After #1051, browser API auth uses HttpOnly cookie
through the **rewrite/BFF**, while edge continues using status/role markers
unless that change explicitly adds CSP nonce headers in the edge file. This
migration MUST preserve cookie forwarding skips for `/api/**`, preserve UX
cookie reads, and carry forward any #1051 edge additions (e.g. CSP nonce) if
\#1051 merges first.

## Goals / Non-Goals

**Goals:**

- Official codemod applied: `proxy.ts` + `export function proxy` (+ `config` if
  still required by Next 16 after codemod).
- Identical route-guard semantics vs pre-change `middleware.ts`.
- No middleware-deprecation warning from `next build`.
- Auth E2E green (login, logout bounce, admin deny, session helpers).
- No regression of #1051 HttpOnly cookie / proxy-forward path when present.

**Non-Goals:**

- Changing auth business rules, cookie names, matcher breadth, or CSP policy.
- Implementing #1051, #1043, or #1045.
- Renaming `next.config.ts` rewrites or inventing a new BFF.
- Reading HttpOnly JWT inside the edge proxy for auth decisions.

## Decisions

1. **Use the official codemod as the primary transform**
   - Why: AC explicitly requires `npx @next/codemod middleware-to-proxy`.
   - After codemod: verify file path `frontend/src/proxy.ts`, export name
     `proxy`, delete leftover `middleware.ts`, fix any import/doc references.
   - Manual rename only if codemod fails in CI/agent env — same end state.

2. **Preserve logic byte-for-byte aside from the export/file rename**
   - Why: KIS; AC is convention + warning removal + E2E green, not behavior
     change. Keep `PUBLIC_PATHS`, cookie decode, admin deny, matcher.
   - Alternative rejected: “rewrite auth while renaming” — couples #1051/#1052
     risk into a chore.

3. **Prefer implement after #1051 when #1051 touches the edge file**
   - Why: #1051 design injects CSP nonce via middleware. Migrating once after
     that lands avoids dual PRs editing the same file and reduces rebase pain.
   - If coordinator schedules #1056 earlier: #1051 MUST target `proxy.ts` and
     rebase; document in implement kickoff.
   - User/coordinator queue: after **#1043/#1045** unless promoted; also
     **after #1045** per dispatch note; always serialize vs Playwright-heavy PRs.

4. **Do not conflate Next `proxy.ts` with the API rewrite proxy**
   - Why: Naming collision. Edge `proxy` = request interceptor. Rewrite proxy =
     `/api/v1/:path*` → `BACKEND_URL`. Comments/docs must keep them distinct so
     implementers do not move auth logic into `rewrites()` or vice versa.

5. **TDD / proof order for a convention rename**
   - Failing checks first: (a) assert absence of deprecated `middleware` export
     file / presence of `proxy` after change; (b) unit tests for
     `admin-access` remain the decision core; (c) optional thin unit that
     imports `proxy` and exercises redirect matrix with mocked `NextRequest`
     if feasible without Next runtime; (d) `next build` log must not contain
     middleware deprecation; (e) Playwright auth suite last.
   - Pre-change: confirm build still warns (documents red for AC #2).

6. **E2E remains the behavior gate**
   - Why: Edge file is thin; golden paths (TS-0002 login/logout, admin guard
     specs, auth setup) prove redirects still work with UX cookies and, after
     #1051, with HttpOnly session.

## Riesgos / Trade-offs

- **[Risk] Codemod version mismatch with Next 16.3.8** → Pin/run latest
  `@next/codemod`; if transform incomplete, finish rename manually to AC shape.
- **[Risk] Concurrent edit with #1051 (CSP nonce / cookie forward)** → Prefer
  #1051 first; otherwise rebase and ensure `/api/**` skip + UX cookies remain;
  never teach edge to require script-readable JWT.
- **[Risk] Playwright flake / runner contention** → Serialize after in-flight
  E2E PRs; use heavy-CI merge workflow.
- **[Risk] Docs/tests still say “middleware”** → Grep update comments and
  E2E notes; keep zustand `persist` “middleware” wording (different concept).
- **[Trade-off] No behavior improvement in this PR** → Intentional tech-debt
  chore; value is forward-compat with Next 16.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Codemod yields `proxy.ts` + `export function proxy` | unit / file assert | frontend unit or scripted assert in PR checks |
| Deprecated `middleware.ts` removed | unit / file assert | same |
| Unauthenticated protected route → `/login` | E2E (+ optional unit) | Playwright auth / login specs |
| Authenticated `/login` → `/dashboard` | E2E | TS-0002 / login specs |
| Non-admin admin route denied | unit + E2E | `admin-access.test.ts` + admin E2E |
| `/api/**` not redirected by edge proxy | unit / E2E | proxy unit or existing API-through-proxy E2E |
| `next build` has no middleware deprecation warning | build log assert | `npm run build` in frontend CI / local |
| Auth E2E suite green | E2E | `npx playwright test` (auth-related + full PR gate) |
| HttpOnly cookie path not broken (#1051) | E2E / regression | post-#1051 login helpers; no localStorage JWT regress |

- New unit tests: prefer importing edge `proxy` with mocked request if stable;
  otherwise file-existence + admin-access matrix (already exists) + build log.
- Integration (Java): n/a
- Coverage (JaCoCo): n/a (frontend-only); Vitest coverage on touched helpers.

## Regression Strategy

- Existing tests affected:
  - `admin-access.test.ts` — should stay green unchanged.
  - `auth-store.test.ts` — cookie clear comments may say “middleware”; update
    wording only.
  - E2E comments in TS-0044 / setup that say “Next.js middleware” → “edge proxy”.
  - Any import of `middleware` from the edge file (none expected today).
- Full suite command: `cd frontend && npm test && npm run lint && npm run build`
  then Playwright.
- HTTP/Bruno API suite: n/a (no backend change).
- Legacy paths: none.

## Playwright Strategy

- Specs to add/update under `frontend/tests/e2e/`:
  - Update wording only where needed; **behavior** regressions caught by
    existing auth specs (TS-0002 login/logout, admin guard, session expiry).
  - Re-run full auth-dependent suite; PR must pass `playwright-e2e.yml`.
- Golden path: login → dashboard → (admin allow/deny) → logout → protected
  route redirects to `/login`.
- Edge: unauthenticated deep link to dashboard redirects; `/api/v1` calls still
  reach backend via rewrite (not hijacked to `/login`).
- Viewports: existing suite defaults.
- Command: `cd frontend && npx playwright test`
- **Do not** mark Playwright n/a — AC requires Auth E2E green; this is
  Playwright-heavy for scheduling.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: frontend-only; deploy with normal frontend image
  once #1043 exists; until then compose/build as today.
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): UI login/logout; confirm no build warning in
  CI artifacts; after #1051, confirm HttpOnly cookie still set and API calls work.

## Rollback Strategy

- Revert safe: yes — revert PR restores `middleware.ts` (deprecation warning
  returns; behavior should match).
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback delayed: edge auth failure → users stuck at login or
  open dashboard; mitigate with Playwright gate before merge.

## Migration Plan

1. Wait for serialize: after **#1043/#1045** queue (or coordinator promote);
   prefer after **#1051** if it edits the edge file; no Playwright-heavy PR in
   flight.
2. Copy this draft into `openspec/changes/chore-1056-middleware-to-proxy/`.
3. Validate: `bash scripts/validate-sdlc-plan.sh chore-1056-middleware-to-proxy`.
4. Branch `cursor/chore-1056-middleware-to-proxy-69d3` from updated `main`.
5. Observe pre-change `next build` deprecation warning (red for AC).
6. Apply codemod → fixups → tests → docs → PR `Closes #1056` → heavy CI → merge.
7. Gate 5 smoke; archive OpenSpec change.

## Open Questions

- None blocking Gate 1. Exact `@next/codemod` CLI flags/workdir confirmed at
  implement (`frontend/` vs repo root) by dry-run on the branch tip.
