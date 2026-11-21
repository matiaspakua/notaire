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
The dashboard must run even with no active intent, showing historical data and factory configuration. Implement a storage layer reading from `.aisdlc/` (runs, evidence, costs, projects, catalogs) and serve it through the dashboard API. Add multi-project support, historical run browser, and intent history view. The dashboard should show past runs, their metrics, and allow filtering by project/intent.