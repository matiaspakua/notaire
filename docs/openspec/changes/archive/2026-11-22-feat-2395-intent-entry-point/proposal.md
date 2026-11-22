# Intent entry point for CLI, API, MCP and UI (B)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2395 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `feat/2395_intent_entry_point` |
| Gate 1 status | passed |

## Objetivo

The factory currently has no unified Intent entity. Operators, the dashboard, and MCP clients need a single entry point to create, list, and inspect work intents (prompts, references, specs) so that every run can be traced back to its originating intent.

## What Changes

Add a new `intent_model.py` service module (submit, list, show, attach_run, spec parser) used by CLI, HTTP API, and MCP. Extend `aisdlc intent` with `submit`, `list`, `show` subcommands. Add HTTP endpoints `POST/GET /factory/intents` and `GET /factory/intents/{id}`. Add MCP tools `intent_submit`, `intent_show`, `factory_status`. Bind intents to runs via `POST /factory/runs`. Persist intents as versioned JSON files under `.aisdlc/intents/`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Intents are stored as versioned files; the kit is the system of record | #2393 shared contract | New |
| Secrets matching SECRET_VALUES patterns are refused with `literal_secret` | `aisdlc/factory.py` SECRET_VALUES | New |
| Reference intents store the reference without calling external systems | #2393 shared contract | New |
| The ledger records id, kind, source, actor but never the intent text | CONSTITUTION evidence rules | New |
| Unknown reference systems are rejected | Design decision | New |

## Capabilities

### New Capabilities

- `factory-intents`: unified Intent entity with versioned file persistence, spec Markdown parsing, CLI, HTTP, and MCP entry points.

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `framework/ai-sdlc` (kit) | yes | new module `intent_model.py`; CLI, serve, MCP, runs extensions; tests |

### Surface area

- Entities: Intent (JSON file under `.aisdlc/intents/<id>/`)
- Endpoints: `POST /factory/intents` (201), `GET /factory/intents`, `GET /factory/intents/{id}`; `POST /factory/runs` accepts optional `intent`
- Database (Flyway `V{n}`): none — file-based persistence
- Configuration / `.env`: none
- Dependencies: none — kit stays stdlib-only

### Write set

- `framework/ai-sdlc/aisdlc/intents.py`
- `framework/ai-sdlc/aisdlc/intent_model.py`
- `framework/ai-sdlc/aisdlc/mcp_server.py`
- `framework/ai-sdlc/aisdlc/cli.py`
- `framework/ai-sdlc/aisdlc/serve.py`
- `framework/ai-sdlc/aisdlc/runs.py`
- `framework/ai-sdlc/aisdlc/contract.py`
- `framework/ai-sdlc/tests/**`
- `framework/ai-sdlc/openapi/factory-control.openapi.yaml`
- `framework/ai-sdlc/README.md`
- `framework/ai-sdlc/CHANGELOG.md`
- `docs/openspec/changes/feat-2395-intent-entry-point/**`
- `docs/openspec/changes/archive/**feat-2395*/**`
- `docs/openspec/specs/**`

### Architecture review

Follows existing kit patterns: new service module `intent_model.py` mirrors `runs.py` and `projects.py` for versioned-entity helpers. No ADR required — the change is additive within the stdlib-only kit boundary.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `framework/ai-sdlc/README.md` | intent commands, MCP tools, API endpoints |
| `framework/ai-sdlc/CHANGELOG.md` | additive entry under Unreleased |
| `framework/ai-sdlc/openapi/factory-control.openapi.yaml` | new intent endpoints and run intent binding |

## Out of Scope

- UI/dashboard intake form (tracked as epic child G, a follow-on issue).
- Store mirror beyond a best-effort hook (tracked as epic child A).
- External system calls from reference intents (GitHub, Jira APIs are not invoked).
