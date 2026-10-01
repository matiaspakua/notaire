# OpenCode worker backend for the local-AI harness

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1107 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `feat/1107_opencode_worker_backend` |
| Gate 1 status | passed |

## Objetivo

The harness runs its worker through Codex CLI only. OpenCode, an agent entry point
the Constitution names (§10), gives gpt-oss-20b real `edit`/`write` tools instead of
Codex's `exec_command` + `apply_patch` mix: 8/8 on the TDD smoke task against 7/8.

## What Changes

- Adapter key `backend.agent` (`codex` | `opencode`, env `AGENT` overrides) and
  `backend.opencode_model`.
- New `bin/worker.py`: builds the worker command and environment for either agent
  and extracts the final message; `run_worker` calls it.
- New `local-ai/opencode/opencode.json`: an isolated config (oMLX provider, no MCP,
  no instructions, snapshots/LSP/formatters off). The run disables the project
  config, Claude Code prompts and external skills.
- The worker keeps the harness guards: the git hooks path (git config env) and the
  crawl-guard shims first on `PATH`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The worker agent is a harness setting, not code | AI-SDLC.md | New |

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `ai-sdlc-enforcement`: selectable worker agent.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `local-ai` harness and adapter | yes | see What Changes |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: `.aisdlc/project.yml` gains `backend.agent`, `backend.opencode_model`
- Dependencies: OpenCode CLI (already installed for the Owner; optional for Codex runs)

### Architecture review

Developer tooling only; no ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `local-ai/sdlc/AI-SDLC.md` | worker agents, OpenCode isolation |
| `local-ai/README.md` | running the worker with OpenCode |
| `CHANGELOG.md` | n/a — not user visible |

## Out of Scope

Switching the default agent; that follows once real issues complete with OpenCode.
