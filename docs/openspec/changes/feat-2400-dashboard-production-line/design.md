> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

The dashboard already proxies `/factory/*` paths (GET, POST, PUT) through a Fastify server to the
kit's `aisdlc serve`. The kit now exposes `/factory/line` (production line model), `/factory/status`
(live run position), `/factory/events` (SSE), `/factory/intents` (CRUD), `/factory/telemetry`
(aggregated metrics), `/factory/config/files` (config tree + read), and `DELETE /factory/catalogs`.
The UI has an overview view with run stations, a metrics view with KPI cards, a catalogs view with
edit forms, and a projects view. The existing look (commit `57a0ea32`) uses industrial steel grays
with phase/status colors — it must be preserved.

## Goals / Non-Goals

**Goals:**

- Present the production line as a horizontal timeline with stations and gates.
- Show the current run's position, previous steps, and next steps in real time.
- Keep the intent visible at all times with a bottom bar and expandable drawer.
- Sync the dashboard live via SSE with polling fallback.
- Let operators browse config files (read-only) and delete catalog entries safely.
- Surface telemetry (time, cost, failures, RepoLens) in the Metrics view.

**Non-Goals:**

- Multi-workspace SSE fan-in.
- Writing config files through the viewer.
- Changing the coach view.
- Real-time collaboration between multiple users.

## Decisions

- **D1: SSE proxy keeps token server-side.** The dashboard server opens an `EventSource` to the kit
  with the bearer token, then relays frames to the browser. The browser never sees the token.
  Alternative: client-side `EventSource` would require exposing the token in the browser — rejected
  per ADR-006.
- **D2: DELETE proxied through the existing proxy route.** The proxy route already handles CSRF and
  token forwarding; adding `DELETE` to the method list and forwarding `If-Match` is minimal and
  consistent. Alternative: a dedicated delete endpoint would duplicate CSRF/token logic.
- **D3: Inline confirmation for delete, not `window.confirm`.** An inline modal/dialog with the
  entry name and version keeps the UX consistent and is accessible (focus trap, keyboard dismiss).
  `window.confirm` is not accessible and blocks the main thread.
- **D4: Polling fallback when SSE unavailable.** If `EventSource` construction fails (kit not
  supporting SSE), the dashboard falls back to polling `/factory/status` every 5 seconds. This
  ensures the dashboard works with older kit versions.
- **D5: Config viewer uses existing read-only pattern.** The kit already redacts secrets; the
  dashboard renders the content with line numbers in a `<pre>` block. No syntax highlighting is
  added to keep dependencies minimal.

## Riesgos / Trade-offs

- [SSE proxy adds server-side state] → Each active SSE connection holds an `EventSource` to the kit.
  The server must clean up on client disconnect. Mitigation: Fastify `onClose` hook cancels the
  upstream stream.
- [SSE proxy may buffer large events] → The kit streams compact JSON; the dashboard relays it
  verbatim. No transformation is applied, so buffering is minimal.
- [Existing overview view is replaced] → The current overview (run stations by status) is replaced
  by the production-line timeline. Existing tests for the overview view must be updated.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| DELETE proxied with CSRF and If-Match | unit | `server.test.ts` |
| SSE proxy forwards Last-Event-ID | unit | `server.test.ts` |
| SSE proxy cleans up on client disconnect | unit | `server.test.ts` |
| Production line timeline renders stations and gates | E2E | `tests/e2e/production-line.spec.ts` |
| Station expand shows agents, skills, tools | E2E | `tests/e2e/production-line.spec.ts` |
| Current run position from /factory/status | E2E | `tests/e2e/production-line.spec.ts` |
| Intent bar always visible | E2E | `tests/e2e/intent-bar.spec.ts` |
| Intent drawer shows full intent | E2E | `tests/e2e/intent-bar.spec.ts` |
| Intake form posts to /factory/intents | E2E | `tests/e2e/intent-bar.spec.ts` |
| Live sync via SSE updates status | E2E | `tests/e2e/live-sync.spec.ts` |
| Polling fallback when SSE unavailable | unit | `hooks.test.ts` |
| Config file tree renders | E2E | `tests/e2e/config-viewer.spec.ts` |
| Config file viewer shows content with line numbers | E2E | `tests/e2e/config-viewer.spec.ts` |
| Delete with inline confirmation sends If-Match | E2E | `tests/e2e/catalog-delete.spec.ts` |
| Telemetry panel shows time, cost, failures | E2E | `tests/e2e/telemetry.spec.ts` |
| WCAG AA no colour-only status | E2E (axe) | `tests/e2e/a11y.spec.ts` |

- New server tests: `tests/server.test.ts` — DELETE proxy, SSE proxy, SSE cleanup.
- New E2E tests: `tests/e2e/production-line.spec.ts`, `tests/e2e/intent-bar.spec.ts`,
  `tests/e2e/live-sync.spec.ts`, `tests/e2e/config-viewer.spec.ts`, `tests/e2e/catalog-delete.spec.ts`,
  `tests/e2e/telemetry.spec.ts`, `tests/e2e/a11y.spec.ts`.
- Coverage impact: vitest floor for server tests; existing floor applies.

## Regression Strategy

- Existing tests affected: `tests/server.test.ts` (proxy tests — add DELETE method),
  `tests/e2e/` (overview tests — rename/update for production-line timeline).
- Full suite command: `cd framework/ai-sdlc/factory-dashboard && pnpm test && pnpm e2e`.
- Legacy paths at risk: none — dashboard is a standalone module.

## Playwright Strategy

- Specs to add/update under `tests/e2e/`:
  - `production-line.spec.ts` — timeline, stations, gates, expand, current run position.
  - `intent-bar.spec.ts` — bar visibility, drawer, intake form.
  - `live-sync.spec.ts` — SSE updates, polling fallback, live indicator.
  - `config-viewer.spec.ts` — file tree, viewer with line numbers.
  - `catalog-delete.spec.ts` — inline confirmation, If-Match, 409 on mismatch.
  - `telemetry.spec.ts` — time, cost, failures, RepoLens.
  - `a11y.spec.ts` — axe core on all new views.
- Golden path: sign in → see production line → expand station → see intent bar → submit intent →
  see live update → view config file → delete catalog entry → see telemetry.
- Edge / error paths: kit unreachable, SSE fails (polling fallback), delete 409, config file not found.
- Viewports: 320px (mobile) / 768px (tablet) / 1024px (desktop).
- Command: `cd framework/ai-sdlc/factory-dashboard && pnpm e2e`.

## Deployment Strategy

- Flyway migration required: no.
- Deployment order / coupling: none — dashboard is built independently.
- Configuration or `.env` keys: none.
- Feature flag: no.
- Smoke test after deploy (Gate 5): open dashboard, sign in, verify production line renders,
  verify intent bar visible, verify SSE connects (live indicator green).

## Rollback Strategy

- Revert safe: yes — no data migration, no schema change. A git revert restores the previous
  dashboard build.
- Database rollback: none needed.
- Data written under the new behavior after revert: none — dashboard writes only through kit
  endpoints that existed before (intents, catalogs).
- Blast radius if rollback is delayed: none — the dashboard is optional; the kit continues
  operating independently.

## Open Questions

None.
