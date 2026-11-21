# Add Junie CLI reader to aisdlc coach

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2387 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `feat/2387_coach_junie` |
| Gate 1 status | passed |

## Objetivo

The `aisdlc coach` command reads Claude Code, Codex, OpenCode and Antigravity session stores but not the Junie CLI. Junie sessions must be analysed alongside the other harnesses so the coach report covers the full set of agent runtimes used in this factory.

## What Changes

- Add a `junie()` reader function to `framework/ai-sdlc/aisdlc/coach.py` that reads `~/.junie/sessions/index.jsonl` for sessions, `~/.junie/sessions/<sessionId>/events.jsonl` for tool/terminal block events and model usage, and `~/.junie/history.jsonl` for prompts matched by project path and time window.
- Register `"junie"` in `HARNESSES` and `READERS`.
- Update the OpenAPI `harness` enum to include `"junie"`.
- Update the kit README coach row and CHANGELOG `[Unreleased]`.
- Add tests with a synthetic home directory that verify sessions, prompts, tool calls, models, project filtering, `--days`, and that `EnvironmentVariablesUpdatedEvent` (secrets) and prompt text are never emitted.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Coach must never read `EnvironmentVariablesUpdatedEvent` (contains secrets) | CU76 | New |
| Coach must never emit prompt text in the JSON report | CU76 | Made explicit |
| Junie sessions are filtered by `projectDir` matching the project root | CU76 | New |
| Tool calls are counted once per unique `stepId` in `ToolBlockUpdatedEvent`/`TerminalBlockUpdatedEvent` | CU76 | New |

## Capabilities

### New Capabilities

(none — this extends an existing capability)

### Modified Capabilities

- `factory-dashboard`: The coach requirement now includes the Junie CLI as a supported harness alongside Claude Code, OpenCode, Codex and AGY.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: none
- Endpoints: `GET /factory/coach` response gains a `"junie"` harness row (additive)
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Write set

- `` `framework/ai-sdlc/aisdlc/coach.py` ``
- `` `framework/ai-sdlc/tests/**` ``
- `` `framework/ai-sdlc/openapi/factory-control.openapi.yaml` ``
- `` `framework/ai-sdlc/README.md` ``
- `` `framework/ai-sdlc/CHANGELOG.md` ``
- `` `docs/openspec/changes/feat-2387-coach-junie/**` ``
- `` `docs/openspec/specs/**` `` (archive only)

### Architecture review

Follows the existing pattern: each harness reader is a standalone generator function accepting a `home: Path` and yielding `Session` objects. No architectural change.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `framework/ai-sdlc/README.md` | Coach row lists Junie CLI alongside other harnesses |
| `framework/ai-sdlc/CHANGELOG.md` | `[Unreleased]` entry for (#2387) |
| `framework/ai-sdlc/openapi/factory-control.openapi.yaml` | Harness enum adds `"junie"` |
| `docs/openspec/specs/factory-dashboard/spec.md` | Coach requirement mentions Junie CLI |

## Out of Scope

Dashboard UI changes — the dashboard consumes the coach report through the contract and needs no modification.
