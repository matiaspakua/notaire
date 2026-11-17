# repolens (framework/ai-sdlc/graph-db)

**Purpose:** Local, open-source code knowledge graph. Indexes one or more folders (code, tests, docs,
configs, data) into a typed property graph and answers hybrid semantic + graph queries over a CLI, an HTTP
API and an MCP server, so AI agents locate code with fewer tool calls and tokens than grep-driven search.

**Verify:** `bash framework/ai-sdlc/graph-db/verify.sh` (lint, tests with coverage floor, wheel, smoke run).

## Contract (what other modules may rely on)

- The `repolens` console script and its exit codes: 0 ok, 1 not found / no results, 2 usage or configuration error
- The TOML profile schema documented in `README.md` (`[index]`, `[[source]]`, `[embed]`, `[git]`, `[tags]`,
  `[routes]`, `[ranking]`)
- The MCP tools `search`, `node`, `impact`, `path`, `outline`, `snippet` (+ `neighbors`, `stats`, `reindex`
  with `--all-tools`) and the HTTP endpoints under `/api/*`
- The wheel built from `pyproject.toml` (version from `repolens/__init__.py`, tags `repolens-v*`)

## Seams (what this module reads from outside)

- The folders a profile names, their git history (`git log`), and an embedding model cache
  (`~/.cache/repolens/models`, override with `REPOLENS_MODEL_CACHE`)

## Must not

- Name an adopter's path, stack or tool inside `repolens/` (adopter specifics live in `examples/*.toml`)
- Send code or queries to a remote service at query time (models run locally; the only network access is the
  one-time model download)
- Index secrets (`.env`, key stores) or write anything into the indexed folders
- Be imported by product code
