# Production-line timeline, intent bar, live sync, configuration as code, telemetry

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2400 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `feat/2400_dashboard_production_line` |
| Gate 1 status | pending — owner-pending: Owner approval of this specification |

## Objetivo

The factory dashboard currently shows runs grouped by status and a static overview. With the kit
endpoints added in #2393 (`/factory/line`, `/factory/status`, `/factory/events`, `/factory/intents`,
`/factory/telemetry`, `/factory/config/files`, `DELETE /factory/catalogs`), the dashboard can now
present a **production-line timeline** with gates, a live intent bar, real-time SSE sync, a
configuration-as-code viewer, and a telemetry panel. This change wires those endpoints into the
dashboard so operators can monitor the factory in real time without leaving the browser.

## What Changes

- **Server proxy extensions** (`src/server/app.ts`, `src/server/upstream.ts`):
  - `DELETE` method proxied with CSRF validation and `If-Match` forwarded to the kit.
  - Streaming proxy for `/factory/events`: the server opens an `EventSource` to the kit, forwards
    `Last-Event-ID` from the browser, keeps the kit token server-side, and relays SSE frames to the
    client. Other methods (GET, POST, PUT) unchanged.
- **UI — Production line timeline** (new view replacing the current overview):
  - Horizontal timeline of stations from `/factory/line` with gates rendered between stations as
    required/advisory pills.
  - Expanding a station reveals agents, skills, tools, inputs, outputs, and guardrails.
  - Current-run position and step states from `/factory/status`; previous/next steps shown.
- **UI — Intent bar and drawer**:
  - Always-visible bar at the bottom showing the current intent's title and status.
  - Clicking opens a drawer with full intent text, acceptance criteria, references as links, and
    source.
  - Intake form (prompt/reference/spec) posting to `POST /factory/intents`.
- **UI — Live sync**:
  - `EventSource` on the dashboard's proxy URL for `/factory/events`.
  - Refreshes status, line, and intent on each event; live indicator dot in the sidebar.
  - Polling fallback (5s interval) when SSE is unavailable.
- **UI — Projects: config-as-code file tree**:
  - File tree from `GET /factory/config/files` with kind badges.
  - Read-only viewer with line numbers from `GET /factory/config/files/{path}`.
- **UI — Configuration: delete with inline confirmation**:
  - Delete button on catalog entries sending `DELETE` with `If-Match` (no `window.confirm`).
- **UI — Metrics: telemetry panel**:
  - Time and cost per intent and stage from `GET /factory/telemetry?group=intent|stage`.
  - Failures by agent/skill/tool from `?group=agent|skill|tool`.
  - RepoLens calls, avg latency, and per-tool breakdown.
- **i18n**: en and es copy for all new UI strings.
- **Accessibility**: WCAG AA contrast, no colour-only status indicators (text labels always present).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The dashboard never sends a kit token to the browser; the server holds it per session | ADR-006, #2393 | Made explicit |
| DELETE requires CSRF and If-Match; the server forwards both to the kit | ADR-006, #2398 | Made explicit |
| SSE token stays server-side; the dashboard server bridges the stream | ADR-006, #2397 | New |
| Config files are read-only in the viewer; secrets are redacted by the kit | #2398 | Made explicit |
| Telemetry is read-only; the dashboard never writes telemetry | #2394 | Made explicit |

## Capabilities

### New Capabilities

- `factory-dashboard-production-line`: production-line timeline with stations, gates, current-run
  position, intent bar, live SSE sync, config-as-code viewer, catalog delete, and telemetry panel.

### Modified Capabilities

- `factory-dashboard`: extends the existing dashboard capability with new views and proxy features;
  the existing contract (multi-workspace, CSRF, token scoping) is preserved.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `factory-dashboard` | yes | Server proxy (DELETE, SSE), new UI views, model types, i18n, tests |
| `backend-api` | no | Not touched — dashboard speaks only the kit HTTP contract |
| `frontend` | no | Not touched — dashboard is a separate module |
| `frontend-swing` | no | Not touched |
| `infra` / observability | no | Not touched |
| CI/CD (`.github/workflows`) | no | Not touched |

### Surface area

- Entities: none (dashboard is a read/write client of the kit; no new DB tables).
- Endpoints (dashboard server, additive):
  - `DELETE /api/workspaces/:ws/factory/*` — proxied with CSRF and If-Match.
  - `GET /api/workspaces/:ws/factory/events` — SSE streaming proxy (forwards Last-Event-ID).
- No database changes.
- Configuration / `.env`: none.
- Dependencies: none (uses existing Fastify, React 19, vitest, Playwright).

### Write set

- `framework/ai-sdlc/factory-dashboard/**`
- `framework/ai-sdlc/CHANGELOG.md`
- `docs/openspec/changes/feat-2400-dashboard-production-line/**`
- `docs/openspec/changes/archive/**feat-2400*/**`
- `docs/openspec/specs/**`

### Architecture review

No architectural change. The dashboard remains a client of the kit's HTTP contract (ADR-006).
SSE proxying keeps the token server-side, consistent with the existing security model.
Config-as-code viewer is read-only, delegating redaction to the kit.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `framework/ai-sdlc/factory-dashboard/README.md` | Add production line, intent bar, live sync, config viewer, telemetry to the views table |
| `framework/ai-sdlc/factory-dashboard/DESIGN.md` | Add timeline, intent bar, live indicator, config viewer, telemetry panel to Components |
| `docs/openspec/specs/factory-dashboard/spec.md` | Add requirements for production line, intent bar, live sync, config viewer, delete, telemetry |
| `CHANGELOG.md` | Features entry under `[Unreleased]` |

## Out of Scope

- Multi-workspace SSE fan-in (one workspace at a time).
- Real-time collaboration between multiple dashboard users.
- Writing config files through the viewer (read-only).
- Coach view changes (tracked separately if needed).
