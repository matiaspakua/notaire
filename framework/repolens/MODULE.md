# repolens (framework/repolens)

**Purpose:** Local, open-source code knowledge graph. A system of its own beside the AI SDLC kit, not inside
it: the kit calls it as a command, an MCP server or an HTTP API and never imports it (ADR-006 of the kit), so
either can move to its own repository.
Indexes one or more folders (code, tests, docs, configs, data) into a typed property graph and answers hybrid
semantic + graph queries over a CLI, an HTTP API and an MCP server, so AI agents locate code with fewer tool
calls and tokens than grep-driven search; measures how agents use it (`repolens report`).

**Verify:** `bash framework/repolens/verify.sh` (lint, tests with coverage floor, wheel, smoke run);
preflight runs it when a branch touches this module.

## Contract (what other modules may rely on)

- The launcher `bin/repolens` (runs this checkout from any directory) and the `repolens` console script, with
  exit codes 0 ok, 1 not found / no results, 2 usage or configuration error. An empty worktree store is not a
  miss: search copies another worktree's index of the same git directory when one exists, otherwise it exits 2
  and says the index is being built
- Zero-configuration discovery: the nearest `repolens.toml` from the working directory, else the repository
- The TOML profile schema documented in `README.md` (`[index]`, `[[source]]`, `[embed]`, `[git]`, `[tags]`,
  `[routes]`, `[ranking]`, `[harness]`)
- The MCP tools `search`, `node`, `impact`, `path`, `outline`, `snippet` (+ `neighbors`, `stats`, `reindex`
  with `--all-tools`) and the HTTP endpoints under `/api/*`; `repolens serve --bench <folder>` (benchmark results for the explorer)
- `repolens setup` writes the harness registrations described in `README.md` (the Claude Code usage hook matches
  `Read|Grep|Glob|Bash|mcp__repolens__.*`); `repolens report --gate` prints
  one advisory line and exits 0 (the factory's `signal` stage relies on both)
- The usage log `<state>/usage.jsonl` (one JSON event per line, fields documented in `repolens/usage.py`)
- The wheel built from `pyproject.toml` (version from `repolens/__init__.py`, tags `repolens-v*`)

## Seams (what this module reads and writes outside itself)

- Reads the folders a profile names and their git history (`git log`)
- Writes its state only to `<project>/.repolens/` (store, usage log, index lock) and to
  `$REPOLENS_HOME` (default `~/.cache/repolens`: shared vector cache, model cache, dependency venv, benchmark
  data)
- Writes harness registrations (`.mcp.json`, `.claude/settings.json`, `.codex/config.toml`, `opencode.json`,
  `.cursor/mcp.json`, git hooks, `.gitignore`) only when someone runs `repolens setup`

## Must not

- Name an adopter's path, stack or tool inside `repolens/` (adopter specifics live in the adopter's
  `repolens.toml`)
- Send code or queries to a remote service (models run locally; the only network access is the one-time model
  download and `setup --install`)
- Index secrets (`.env`, key stores)
- Fail or slow an agent's tool call because of telemetry (the hook always exits 0 and imports the standard
  library only)
- Be imported by product code
