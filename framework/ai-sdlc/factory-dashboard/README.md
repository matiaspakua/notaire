# AI SDLC Factory Dashboard

Control and monitoring dashboard for the AI SDLC agentic factory production line.

## Overview

A standalone TypeScript/pnpm server that provides a real-time pub/sub dashboard for monitoring the AI SDLC factory lifecycle. It bridges the Python AI SDLC engine to a React UI via WebSocket and HTTP API.

## Architecture

```
┌─────────────────────────────────────────────────┐
│  React UI (Vite)                                │
│  ┌─────────────┐ ┌──────────┐ ┌──────────────┐ │
│  │ Production  │ │ Event    │ │ Agent Roster │ │
│  │ Line        │ │ Stream   │ │              │ │
│  └─────────────┘ └──────────┘ └──────────────┘ │
└────────────────────┬────────────────────────────┘
                     │ WebSocket + HTTP API
┌────────────────────▼────────────────────────────┐
│  Fastify Server (TypeScript)                    │
│  ┌─────────────┐ ┌──────────┐ ┌──────────────┐ │
│  │ Evidence    │ │ AI SDLC  │ │ WebSocket    │ │
│  │ Watcher     │ │ Bridge   │ │ Pub/Sub      │ │
│  └─────────────┘ └──────────┘ └──────────────┘ │
└────────────────────┬────────────────────────────┘
                     │ HTTP proxy + file watch
┌────────────────────▼────────────────────────────┐
│  AI SDLC Python Engine                          │
│  ┌─────────────┐ ┌──────────┐ ┌──────────────┐ │
│  │ aisdlc      │ │ Evidence │ │ Runs         │ │
│  │ serve       │ │ Ledger   │ │ Lifecycle    │ │
│  └─────────────┘ └──────────┘ └──────────────┘ │
└─────────────────────────────────────────────────┘
```

## Quick Start

```bash
# Install dependencies
cd framework/ai-sdlc/factory-dashboard
pnpm install

# Build the UI
pnpm build

# Start the server (requires aisdlc serve running on port 8765)
pnpm dev

# Or start with custom AISDLC root
AISDLC_ROOT=/path/to/project pnpm dev
```

The dashboard will be available at `http://127.0.0.1:8766`.

## Configuration

| Environment Variable | Default | Description |
|---|---|---|
| `FACTORY_DASHBOARD_PORT` | `8766` | Dashboard server port |
| `AISDLC_ROOT` | `process.cwd()` | Path to the AI SDLC project root |
| `AISDLC_SERVE_PORT` | `8765` | Port of the `aisdlc serve` HTTP adapter |

## API Endpoints

All endpoints proxy to the AI SDLC `aisdlc serve` adapter:

- `GET /api/factory/capabilities` — Live port capabilities
- `GET /api/factory/runs` — List runs
- `GET /api/factory/runs/:id` — Run detail
- `GET /api/factory/runs/:id/events` — Run evidence events
- `GET /api/factory/workflows` — List workflows
- `GET /api/factory/config` — Factory config
- `GET /api/factory/report` — Evidence report
- `GET /api/factory/dashboard` — Aggregated dashboard state
- `WS /ws` — WebSocket pub/sub for real-time events

## WebSocket Protocol

Messages are JSON objects:

```json
{
  "type": "event",
  "timestamp": "2024-01-01T00:00:00Z",
  "data": {
    "kind": "step.started",
    "run_id": "run-abc123",
    "step": "implement"
  }
}
```

Message types: `event`, `run_status`, `station_update`, `agent_update`, `intent_update`, `heartbeat`.

## Integration with AI SDLC

The dashboard watches the evidence ledger (`.aisdlc/evidence/ledger.jsonl`) for real-time events. When `aisdlc serve` is running, it also proxies the OpenAPI endpoints.

To start the dashboard with the AI SDLC factory:

```bash
# Start aisdlc serve first
aisdlc serve --port 8765

# Then start the dashboard
cd framework/ai-sdlc/factory-dashboard
pnpm dev
```

## Files

- `src/server/index.ts` — Fastify server with WebSocket pub/sub
- `src/ui/App.tsx` — Main React app with production line view
- `src/ui/components/` — UI components (StationCard, EventStream, IntentPanel)
- `src/ui/hooks/` — React hooks (useWebSocket, useDashboard)
- `src/ui/types/` — TypeScript types mirroring the OpenAPI model
- `src/ui/styles/dashboard.css` — Dark theme CSS with factory control room aesthetic
