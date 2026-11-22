# Factory Control (factory dashboard)

A web dashboard for one or more AI SDLC factories. It is an optional companion of the kit: it
speaks only the `aisdlc serve` contract (`../openapi/factory-control.openapi.yaml`) and, when you
add it, the RepoLens HTTP API. It never reads or writes a repository's files. The kit stays the
system of record and enforces scope, CSRF, `If-Match` and the actor. See
[ADR-006](../docs/adr/ADR-006-dashboard-contract-client.md).

What you get per workspace:

| View | Source | Notes |
|------|--------|-------|
| Production line | `/factory/line`, `/factory/status`, `/factory/config`, `/factory/events` | Station timeline with gates; current run position; live sync via SSE with polling fallback |
| Runs | `/factory/runs`, `/factory/runs/{id}/events`, `POST /factory/runs` | Steps and evidence per run; request a run with an approved workflow |
| Metrics | `/factory/report`, `/factory/telemetry` | KPI cards, stage duration bars, gates, autonomy, cost by model; telemetry by intent, stage, agent, skill, tool |
| Configuration | `/factory/catalogs/{kind}`, `DELETE /factory/catalogs/{kind}/{id}` | Providers, agents, skills, MCP, workflows, rules, prompts, hooks, memory, integrations; versioned edits; delete with confirmation |
| Projects | `/factory/projects`, `POST …/repos`, `/factory/config/files` | Repositories per project, `dry_run` or `live`; configuration-as-code file tree and viewer |
| Knowledge | RepoLens `search`, `orphans`, `metrics` | Search hits keep their repository label |
| Coach | `/factory/coach` (owner only) | Local harness habits and anti-patterns; never prompt text |
| Intent bar | `/factory/intents`, `POST /factory/intents` | Always-visible bar with current intent; drawer with details; intake form |

## Run it in any project

Requirements: Node 22 or later and pnpm; Python 3.11 or later for the kit.

1. In each repository you want to watch, start the kit with writes on:

   ```bash
   aisdlc serve --write --port 8765
   ```

2. Give each person their own token. The secret is printed once:

   ```bash
   aisdlc serve-token add --identity ana --scope write    # read | write | owner
   aisdlc serve-token list                                 # identities and scopes, never secrets
   aisdlc serve-token revoke --identity ana
   ```

   Tokens live in `.aisdlc/serve.tokens` (mode 0600). The kit writes `.aisdlc/.gitignore` so they
   never reach a commit.

3. Optional: serve the code knowledge graph of the repository with `repolens serve --port 8770`.

4. Describe your workspaces in a `dashboard.json` (git-ignored here; see `dashboard.example.json`):

   ```json
   {
     "listen": { "host": "127.0.0.1", "port": 8766 },
     "workspaces": [
       { "id": "shop", "label": "Shop", "kit": "http://127.0.0.1:8765", "knowledge": "http://127.0.0.1:8770" },
       { "id": "billing", "label": "Billing (build host)", "kit": "http://10.0.0.12:8765" }
     ]
   }
   ```

5. Build and start:

   ```bash
   pnpm install
   pnpm build
   FACTORY_DASHBOARD_CONFIG=./dashboard.json pnpm start
   ```

   Open `http://127.0.0.1:8766`, pick a workspace and sign in with your token.

Without a config file the dashboard serves one workspace, `local`, from `AISDLC_SERVE_URL`
(default `http://127.0.0.1:8765`) and `REPOLENS_URL`.

| Variable | Default | Meaning |
|----------|---------|---------|
| `FACTORY_DASHBOARD_CONFIG` | none | Path of `dashboard.json` |
| `FACTORY_DASHBOARD_HOST` / `FACTORY_DASHBOARD_PORT` | `127.0.0.1` / `8766` | Listen address |
| `AISDLC_SERVE_URL` | `http://127.0.0.1:8765` | Kit of the default workspace |
| `REPOLENS_URL` | none | RepoLens of the default workspace |
| `FACTORY_DASHBOARD_LABEL` | `Local` | Label of the default workspace |

## Security model

- The browser never sees a kit token. The server keeps it per session and workspace, behind an
  `HttpOnly; SameSite=Strict` cookie (`Secure` when `secureCookies` or `allowRemote` is set).
- Writes need the `X-Dashboard-CSRF` header from `/api/session`. The server adds the kit's
  `X-AISDLC-CSRF` itself.
- `DELETE` requests are proxied with CSRF validation and `If-Match` forwarded to the kit.
- The server streams `/factory/events` using the kit's server-side token and `Last-Event-ID`;
  the browser never sees the token.
- Only `/factory/...` paths of the contract are proxied; dot segments and encoded separators are
  refused. RepoLens is read-only through the dashboard.
- The server binds to loopback. Another address needs `"allowRemote": true` and TLS in front.
- Responses carry a strict CSP (`default-src 'self'`), `nosniff`, `no-referrer` and `no-store`.
- Sessions expire after `sessionTtlMinutes` (default 720) and live in memory.

## Develop

```bash
pnpm dev           # server with reload on :8766
pnpm dev:ui        # Vite on :5173, proxying /api to :8766
pnpm typecheck
pnpm test          # server tests (Vitest, Fastify inject, fake upstreams)
pnpm e2e           # Playwright + axe against a real, seeded `aisdlc serve`
```

The design tokens and rules are in [DESIGN.md](DESIGN.md).
