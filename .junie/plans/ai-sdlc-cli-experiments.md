---
sessionId: session-261119-141600-a5xc
---

# Requirements

Run controlled experiments comparing AI SDLC + RepoLens across 4 CLI tools (Codex, AGY, kiro-cli, opencode). Extend providers.py, run 4 tasks per CLI with baseline and graph arms, capture metrics, publish to GitHub Pages, and create a GitHub issue.

# Technical Design

providers.py currently supports only claude and cursor. Add 4 ProviderSpec entries and build_argv branches for codex, agy, kiro, opencode. Tasks: totp-verify, person-create, folio-slice, hex-adr. Results saved as agent-<cli>.json. Update repolens-bench-snapshot.json. Risks: rate limits, MCP support varies.

# Delivery Steps

### ✓ Step 1: Add CLI provider specs to providers.py
Add 4 ProviderSpec entries and build_argv branches for codex, agy, kiro-cli, opencode. Test with python3 providers.py probe.

### ✓ Step 2: Run experiments and capture results
Run 4 tasks against each CLI with baseline and graph arms. Save agent-<cli>.json results. Create summary-cli-comparison.md.

### ✓ Step 3: Update GitHub Pages and create issue
Update repolens-bench-snapshot.json. Create GitHub issue with findings. Commit and create PR.

### ✓ Step 4: Factory production line dashboard
Create interactive UI dashboard at /ai-sdlc/factory-line/ with intent panel, RepoLens integration, animations, event stream, and agent roster. PR #2383 open.

### ✓ Step 5: Standalone factory dashboard server
Create dedicated TypeScript/pnpm UI server at framework/ai-sdlc/factory-dashboard/ with Fastify HTTP + WebSocket pub/sub, evidence ledger watcher, React production line UI, light minimalist theme, sidebar navigation, station detail panel, and integration with aisdlc serve.

### ✓ Step 6: Bidirectional factory control (play/pause/stop/skip)
Add control commands to the factory dashboard so users can direct the factory via UI, REST API, and WebSocket. ControlBar component, POST /api/factory/runs/:id/control endpoint, WebSocket command handling, station skip action, and control state machine.

### ✓ Step 7: Factory configuration module
Implement full lifecycle management for agents, rules, MCP integrations, and skills catalog with CRUD operations, enable/disable toggles, and associations. Backend API endpoints in factory-dashboard server, UI module with sidebar navigation, and integration with aisdlc config. Fixed blank page bug: `API_BASE` was used before definition in `types/index.ts`.

### ✓ Step 8: Sequential stage execution, agent tracking, and file viewer
Factory runs 1 stage at a time with 1 agent, Foreman monitors all. Each agent configured with AGENT.md, rules, skills, MCP/tools, specific LLM model. Track per-agent token consumption and time spent. Add file viewer module to open factory config files (markdown, JSON, YAML) in a nice editor within the dashboard.

### ✓ Step 9: Persistent storage layer for dashboard without active intent
The dashboard runs even with no active intent, showing historical data from `.aisdlc/`. Implemented `PersistentStorage` class (storage.ts, 337 lines) reading evidence, costs, runs, projects, and catalogs. Added `HistoryView` (385 lines) with stage breakdown, evidence table, and cost-by-model table. 8 new API endpoints in server. Added RepoLens metrics API (`/api/metrics`) and `RepoLensMetricsView` (389 lines) showing recall, latency, and agent comparison. Added `FileViewerView` (325 lines) for factory config files. Added `ForemanMonitorView` merged into Metrics with intent filtering. PR #2383 updated with all changes.

### ✓ Step 10: Hybrid Approach for Production Line visualization
Replace the 16-card grid with Active Station Focus + Compact Timeline. Created `ActiveStationView` (201 lines) showing the currently running station prominently with agent info, skills, tools, gates, human indicators, and phase-colored border. Created `CompactTimeline` (134 lines) with horizontal progress dots, phase dividers, status icons, tooltips, and legend. Added 660 lines of CSS with dark theme support and responsive breakpoints. Updated `App.tsx` to use new components. Build succeeds (43 modules, 325KB JS + 80KB CSS).

### ✓ Step 11: AI Providers configuration module
Add "AI Providers" tab to Configuration so users can select AI providers (Anthropic, OpenAI, Google, Microsoft, OpenRouter), configure available models, set authentication (API key, service account, OAuth), manage usage credits, and set default models. Added `AiProviderConfig` and `AiModelConfig` types, `discoverAiProviders()` in config-store with env var detection, 5 REST API endpoints, and `ProvidersView` UI with model expansion, auth status badges, usage bars, and default model selection.

### ✓ Step 12: Intent View — Core factory input component
Replace the thin 56px intent bar with `IntentView` — a prominent, always-visible "Work Order" that anchors the entire dashboard. Supports simple prompts, task IDs (GitHub/Jira), and full specifications with acceptance criteria, restrictions, links, references, flow, and markdown body. Collapsible to compact header, expandable to full detail with two-column layout.

### ✓ Step 13: Intent ID as primary factory tracker
Make `intent_id` the unifying tracker across all factory data — evidence, costs, runs, agent records, metrics, events, and configuration. Updated types, storage, server APIs, and UI components so everything correlates by intent ID.