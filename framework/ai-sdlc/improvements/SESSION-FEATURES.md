# Session Features — AI SDLC Factory Dashboard

> **Branch**: `feat/2381_factory_dashboard`  
> **PR**: [#2383](https://github.com/matiaspakua/notaire/pull/2383)  
> **Session**: 2026-11-21  
> **Scope**: All features implemented in this session on this branch.

---

## 1. CLI Tools Benchmark Integration

### 1.1. Multi-CLI Provider Support
Extended `../repolens/bench/providers.py` to support 4 additional CLI tools beyond the existing Claude and Cursor agents:

- **Codex CLI** (`codex exec --format json`) — OpenAI's coding assistant
- **AGY CLI** (`agy --print --output-format stream-json`) — AGY agent with stream-json output
- **kiro-cli** (`kiro-cli chat --v3`) — kiro agent mode
- **opencode** (`opencode run --format json --auto`) — OpenCode with auto-approval

Each provider includes a `ProviderSpec` entry with command, fallback commands, auth environment variables, and probe command. Added `build_argv()` branches for each CLI's unique non-interactive mode.

### 1.2. CLI Experiment Runner
Created `../repolens/bench/cli_experiment.py` — a lightweight experiment script that runs 4 representative tasks (lexical, cross-stack, multi-hop, docs) against each CLI, handling per-CLI output format differences (AGY stream-json parsing, kiro plain text capture).

### 1.3. Experiment Results
- **`cli-comparison.json`**: Raw results from 8 runs across 2 CLIs × 4 tasks
- **`summary-cli-comparison.md`**: Human-readable findings — AGY achieved recall=1.0 (38s avg), kiro scored recall=0.75 (44s avg)
- **`repolens-bench-snapshot.json`**: Updated with `cliComparison` section for GitHub Pages showcase

---

## 2. Standalone Factory Dashboard Server

### 2.1. TypeScript/pnpm Project Structure
Created `../factory-dashboard` — a standalone TypeScript service with:

- **Fastify HTTP server** on port 8766 with REST API endpoints
- **WebSocket pub/sub** (`/ws`) for real-time event broadcasting
- **Vite + React UI** built as static assets served by the server
- **Evidence ledger watcher** via chokidar, monitoring `../../../.aisdlc/evidence/ledger.jsonl` for real-time factory events
- **Auto-detection** of project root by walking up from the server directory until `../../../.claude` is found

### 2.2. Light Minimalist Theme with Dark Mode Toggle
Complete light theme design with optional dark mode:

- Industrial precision palette — cool steel grays, distinct phase colors
- Proper type scale (11px–24px), 12px card radius, SVG-ready browser surfaces
- Custom scrollbar, selection, and focus styles
- `[data-theme="dark"]` CSS variable overrides for GitHub-dark colors
- Theme toggle button in the sidebar footer

### 2.3. Sidebar Navigation Layout
Sidebar-based layout replacing tab-based navigation:

- Always-visible sidebar with SVG icons (no emoji)
- Phase indicators with completion counts (e.g., "3/4")
- Clean intent bar showing the factory input
- View-based routing: Production Line, Metrics, Event Stream, Configuration, History, RepoLens Impact, File Viewer

---

## 3. Production Line Visualization

### 3.1. Active Station Focus + Compact Timeline (Hybrid Approach)
Replaced the 16-card grid with a temporal visualization that makes the factory's sequential nature visible:

- **`ActiveStationView`** (201 lines): Prominent card showing the currently running station with phase-colored left border, pulsing status dot, agent info, skills/tools tags, quality gates, human approval indicators, and elapsed time
- **`CompactTimeline`** (134 lines): Horizontal progress bar with clickable status dots (checkmark for completed, filled circle for active, X for skipped, exclamation for error), phase dividers, hover tooltips, and legend

### 3.2. Station Detail Panel
Click-to-inspect slide-in panel showing full station configuration:

- Status, assigned agent with role and LLM model
- Skills, tools, quality gates
- Human approval requirements
- RepoLens integration link (when applicable)

### 3.3. Sequential Stage Execution
Factory runs one stage at a time with one agent, with the Foreman agent monitoring the entire process. Each agent is configured with:

- One `AGENT.md` file
- One or more rules
- One or more skills
- One or more MCP/tools
- Specific AI model (LLM) and effort associated

---

## 4. Intent View — Core Factory Input

### 4.1. Prominent Work Order Component
Replaced the thin 56px intent bar with `IntentView` (306 lines) — a prominent, always-visible "Work Order" that anchors the entire dashboard:

- **Compact header**: Source icon, task ID, title, run badge, spec badge, expand arrow
- **Expandable detail**: Two-column layout with description, flow, acceptance criteria with progress tracking, restrictions with severity colors, links with type badges, references, and markdown body
- **Supports**: Simple prompts, task IDs (GitHub/Jira), and full specifications

### 4.2. Intent ID as Primary Factory Tracker
Made `intent_id` the unifying tracker across all factory data:

- Added `intent_id` to all types: `EvidenceEvent`, `Run`, `ForemanState`, `AgentStageRecord`, `TokenUsage`, `ErrorBucket`, `StageDuration`, `RunLeadTime`, `ControlRequest`, `ControlResponse`, `AgentStageRecord`
- Extended `PersistentStorage` with `costs_by_intent` and `evidence_by_intent` grouping
- New API endpoints: `/api/storage/costs/by-intent`, `/api/storage/evidence/by-intent`, `/api/storage/runs/by-intent`, `/api/factory/costs/intent/:id`, `/api/factory/evidence/intent/:id`, `/api/factory/runs/intent/:id`, `/api/factory/agent-records/intent/:id`, `/api/factory/metrics/intent/:id`
- Event stream filters by intent, History view displays intent badges, Metrics view correlates all data by intent

---

## 5. Bidirectional factory controls

### 5.1. Control Bar Component
`ControlBar.tsx` (96 lines) — transport-agnostic control surface with:

- Play, pause, resume, stop SVG buttons
- State machine-driven enable/disable logic
- Live state indicator with pulsing dot

### 5.2. REST API and WebSocket Command Handling
- `POST /api/factory/runs/:id/control` endpoint forwarding to `aisdlc serve`
- WebSocket message handler processing control commands directly
- Station skip action via `onSkip` callback in StationDetail
- Control state machine responding to `run.paused/resumed/stopped/play` events

### 5.3. Control Types
Full type system for control commands: `ControlCommand` (play, pause, resume, stop, skip), `ControlRequest`, `ControlResponse`, `ControlState`. Extended `EvidenceKind` with `run.paused`, `run.resumed`, `run.stopped`, `step.skipped`.

---

## 6. Factory Configuration Module

### 6.1. Configuration Store
`config-store.ts` (488 lines) — `ConfigStore` class that discovers and manages:

- **10 agents** from `../../../.claude/agents`
- **9 rules** from `../../../.claude/rules`
- **3 MCP servers** from `../../../.aisdlc/aisdlc.toml`
- **37 skills** from `../../../.claude/skills` and `.aisdlc/` generic skills list

Full CRUD operations: `get`, `create`, `update`, `delete`, `toggle` for each config type.

### 6.2. Configuration UI
`ConfigView.tsx` (676 lines) — tabbed UI with 4 sub-views:

- **Agents**: Search, category filter, enable/disable toggles, edit forms, delete actions
- **Rules**: Same CRUD interface with severity indicators
- **MCP Servers**: Health check endpoint, connection status badges
- **Skills**: Category-based organization, enable/disable per skill

### 6.3. AI Providers Configuration
Added "AI Providers" tab to Configuration:

- **5 providers**: Anthropic, OpenAI, Google, Microsoft, OpenRouter
- **Model management**: Expandable model lists with pricing, capabilities, and default model selection
- **Authentication**: API key detection from environment variables, auth status badges (✓ configured, ⚠ missing)
- **Usage credits**: Per-provider usage bars and credit tracking
- **Full CRUD**: Enable/disable providers, set default models, create custom providers

---

## 7. Metrics Module

### 7.1. KPI Cards and Charts
`MetricsView.tsx` (979 lines) — comprehensive metrics view with:

- **5 KPI cards**: Lead Time, Throughput, Token Cost, Error Rate, Success Rate
- **6 chart components** (pure SVG, no external library):
  - Horizontal bar chart for stage durations
  - Line charts for token cost/volume trends
  - Donut chart for error distribution
  - Throughput bar chart
  - Lead time table

### 7.2. Intent-Based Filtering
- Search input for filtering intents by issue number, title, source, or run ID
- Clickable intent buttons for quick filtering
- `IntentDetailView` with 4 tabs: Overview, Stages, Agents, Config
- Per-intent KPI cards showing lead time, token cost, stages, outcome, and errors

### 7.3. Foreman Monitor Integration
Merged Foreman status into Metrics:

- Foreman agent status indicator (monitoring, alerting, idle)
- Sequential stage-by-stage execution timeline
- Per-agent token/time summary table
- Stage timeline grouped by phase

---

## 8. Persistent Storage Layer

### 8.1. PersistentStorage Class
`storage.ts` (337 lines) — reads from `../../../.aisdlc` directory:

- **Evidence ledger**: `evidence/ledger.jsonl`
- **Costs**: `costs/` directory
- **Runs**: `runs/` directory
- **Projects**: `projects/` directory
- **Catalogs**: agents, skills, MCP servers, workflows

### 8.2. History View
`HistoryView.tsx` (385 lines) — historical run browser:

- Summary cards (total evidence, costs, runs)
- Stage breakdown bars
- Searchable evidence table with intent ID column
- Cost-by-model table
- Graceful empty state when `../../../.aisdlc` is missing

### 8.3. Dashboard Without Active Intent
The dashboard runs even with no active intent, showing historical data from persistent storage. All views (Production Line, Metrics, Configuration, History) remain functional without a live `aisdlc serve` instance.

---

## 9. RepoLens Integration

### 9.1. RepoLens Metrics API
Added `GET /api/metrics` endpoint to RepoLens server (`../repolens/repolens/api.py`):

- Aggregates benchmark results from `bench/results/`
- Retrieval recall (grep vs graph), latency, output size
- Agent tool calls, token usage, cost savings
- Computed improvement ratios

### 9.2. RepoLens Metrics View
`RepoLensMetricsView.tsx` (389 lines) — full metrics view in the dashboard:

- Headline KPIs (recall improvement, latency reduction, output size reduction)
- Tabbed views: Retrieval, Agent Sessions, Index
- Bar charts for recall/latency/output size comparison
- Agent comparison cards with improvement ratios
- Knowledge graph stats

### 9.3. Proxy Endpoints
Factory dashboard proxies RepoLens API:

- `/api/repolens/metrics` — benchmark metrics
- `/api/repolens/metrics/usage` — usage events
- `/api/repolens/stats` — index statistics

---

## 10. File Viewer Module

### 10.1. File Browser
`FileViewerView.tsx` (325 lines) — browse and preview factory configuration files:

- Categorized file tree: Factory, Agents, Rules, Skills, Framework, Benchmarks, Docs
- Syntax highlighting for Markdown, JSON, YAML, TOML
- "Open in editor" button linking to GitHub
- Clean preview panel with line numbers

---

## 11. Event Stream

### 11.1. Real-Time Event Log
`EventStream.tsx` — log view showing real-time evidence events:

- WebSocket-connected, auto-scrolling event feed
- Phase-colored event kinds
- Timestamp, kind, detail (intent ID, run ID, step, status)
- Intent-based filtering via `filterIntentId` prop
- Play/pause controls for the event stream

---

## 12. Error Boundary and Resilience

### 12.1. Error Boundary Component
`ErrorBoundary.tsx` — catches React render errors so the dashboard never shows a blank screen:

- Styled error card with details and reload button
- Global error/rejection handlers in `main.tsx`
- Loading indicator in `index.html` showing "Loading Factory Dashboard…" until React mounts
- `<noscript>` fallback for browsers without JavaScript

---

## 13. GitHub Pages Integration

### 13.1. CLI Comparison Showcase
Updated `../../../github-page/lib/repolens-bench-snapshot.json` with `cliComparison` section:

- AGY and kiro summary data
- Unavailable CLI reasons (Codex rate-limited, opencode auth required)
- Accessible via GitHub Pages at `/ai-sdlc/repolens/`

### 13.2. Factory Production Line Dashboard (GitHub Pages)
Created `../../../github-page/app/ai-sdlc/factory-line/page.tsx` (678 lines) — static factory visualization:

- 16 stations across 5 phases
- Event stream with animations
- Agent roster
- Integration points
- Mermaid sequence diagrams
- Navigation integration (sidebar, i18n, reading path)

---

## 14. RepoLens Light Theme

### 14.1. Theme Toggle for RepoLens UI
Added light/dark theme toggle to RepoLens Explorer (`repolens/ui/index.html`):

- Manual theme toggle button (🌙/☀️) in the header
- `data-theme` attribute-based theming with system preference as default
- Users can explicitly switch between light and dark regardless of OS settings

---

## 15. Type System and API Endpoints

### 15.1. Comprehensive TypeScript Types
`types/index.ts` — full type system mirroring the AI SDLC entity model:

- **Core types**: `Station`, `Agent`, `EvidenceEvent`, `Run`, `Workflow`, `Phase`
- **Control types**: `ControlCommand`, `ControlRequest`, `ControlResponse`, `ControlState`
- **Metrics types**: `MetricsSnapshot`, `StageDuration`, `TokenUsage`, `ErrorBucket`, `RunLeadTime`
- **Foreman types**: `ForemanState`, `AgentStageRecord`
- **Config types**: `AgentConfig`, `RuleConfig`, `McpServerConfig`, `SkillConfig`, `ConfigCollection`
- **AI Provider types**: `AiProviderConfig`, `AiModelConfig`, `AiProviderId`, `AuthMethod`
- **Intent types**: `Intent`, `IntentLink`, `IntentAcceptanceCriterion`, `IntentRestriction`
- **File Viewer types**: `FileViewerEntry`, `FileViewerContent`
- **RepoLens types**: `RetrievalMetrics`, `AgentComparison`, `RepoLensMetrics`

### 15.2. REST API Endpoints
Complete REST API in `server/index.ts`:

- **Dashboard**: `/api/factory/capabilities`, `/api/factory/dashboard`
- **Control**: `POST /api/factory/runs/:id/control`
- **Metrics**: `/api/factory/metrics`, `/api/factory/agent-records`, `/api/factory/foreman`
- **Storage**: 8 endpoints for evidence, costs, runs, projects, catalogs
- **Intent-based**: 8 endpoints for filtering by intent ID
- **Config**: 20 endpoints for agents, rules, MCP, skills, AI providers CRUD
- **RepoLens proxy**: 3 endpoints forwarding to RepoLens server
- **File viewer**: `/api/factory/files`, `/api/factory/files/:path`

---

## 16. CSS and Design System

### 16.1. Comprehensive Stylesheet
`dashboard.css` (2295+ lines) — complete design system:

- CSS custom properties for light/dark themes
- Phase color tokens (Intake, Plan, Prove, Publish, Ship)
- Status color tokens (idle, running, done, error, waiting, skipped, paused)
- Type scale, spacing scale, border radius tokens
- Component styles for all views and components
- Dark theme overrides using `[data-theme="dark"]`
- Responsive breakpoints for mobile, tablet, desktop
- Animation keyframes for pulse, conveyor, station enter
- `prefers-reduced-motion` support

---

## Summary Statistics

| Category | Files | Lines of Code |
|----------|-------|---------------|
| Factory Dashboard Server | 17 | ~3,500 |
| UI Components | 12 | ~4,200 |
| CSS Stylesheet | 1 | ~2,295 |
| Types | 1 | ~600 |
| Config Store | 1 | ~488 |
| Storage | 1 | ~337 |
| CLI Experiment | 1 | ~286 |
| Providers | 1 | ~120 |
| RepoLens API | 1 | ~80 |
| GitHub Pages | 4 | ~1,200 |
| **Total** | **~40** | **~13,000** |

---

## Dependencies

- **Fastify** — HTTP server
- **WebSocket** — real-time pub/sub
- **React 18** — UI framework
- **Vite** — build tool
- **TypeScript** — type safety
- **chokidar** — file system watcher
- **pnpm** — package manager
