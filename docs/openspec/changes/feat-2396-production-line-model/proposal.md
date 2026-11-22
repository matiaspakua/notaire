# The production line model — stages, gates, guardrails, inputs and outputs

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2396 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `feat/2396_production_line_model` |
| Gate 1 status | passed |

## Objetivo

The factory has no machine-readable representation of its own production line. Agents,
tooling and the future dashboard need a stable payload that lists every stage in
lifecycle order, with its gates, the AI agents and skills assigned to it, tools and MCP
servers, model routing, effort budget, and what flows in and out of each stage. Without
it, the factory cannot explain itself or be supervised.

## What Changes

- **New module `framework/ai-sdlc/aisdlc/line.py`** that composes existing sources
  (`stages.stage_dicts`, `profile.gates_for`, `profile.stages`, `profile.protected`,
  `catalogs.list_entries`, `isolator.isolator_status`) into a single `build_line(ctx)`
  function that returns the production-line payload. No logic is duplicated.
- **`profile.StageProfile`** gains two optional fields (`inputs`, `outputs`) parsed with
  the existing `_string_list` helper from `[[stages]]` blocks in the TOML. Additive,
  backward-compatible.
- **`stages.stage_dicts`** includes `inputs` and `outputs` in its per-stage dicts
  (additive, existing keys unchanged).
- **`cli.py`** gains an `aisdlc line [--json]` subcommand with a human text rendering
  (without `--json`) and a JSON payload (with `--json`).
- **`serve.py`** gains `GET /factory/line` as a read-scope endpoint in `_dispatch_get`,
  calling `line.build_line(ctx)`.
- **`framework/ai-sdlc/openapi/factory-control.openapi.yaml`** gains the
  `/factory/line` path and `ProductionLine`, `LineStage`, `LineGate`, `LineGuardrails`
  schemas.
- **`.aisdlc/aisdlc.toml`** adds `inputs`/`outputs` to the four existing `[[stages]]`
  blocks (`plan`, `red`, `pre-push`, `pre-pr`).
- **`framework/ai-sdlc/templates/project/aisdlc.toml`** documents the optional
  `inputs`/`outputs` keys in the commented template.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The production line is derived from the profile; the profile is the single source of truth for stage order, gates, agents, skills, MCP and model routing | CU76, #2393 shared contract (C) | Made explicit |
| A stage appears in the line if it is declared in `[[stages]]` or has at least one gate; stages with neither are omitted | CU76, #2393 (C) | New |
| Stage order follows `profile.stages()`: canonical lifecycle order first, then alphabetical for any extras | CU76 | New |
| `inputs`/`outputs` on a stage are optional; when absent, the kit provides defaults for the seven known stage ids (plan, red, green, pre-push, pre-pr, merge, release); `io_source` distinguishes `"profile"` from `"default"` | CU76, #2393 (C) | New |
| Guardrails expose protected paths (owner + path list), hook-paths (protected paths whose value contains `hook`), the rules catalog entries, and the isolator status — never secrets | CU76, #2393 (C) | New |
| `GET /factory/line` is read scope; it does not require write or owner scope | CU76, #2393 (C) | New |

## Capabilities

### New Capabilities

- `aisdlc-production-line`: ordered stage/gate/guardrail view of the factory, plus
  CLI and HTTP access

### Modified Capabilities

- `aisdlc-serve`: adds the `/factory/line` GET path (read scope)
- `aisdlc-stage-profiles`: adds `inputs`/`outputs` fields to `StageProfile` and
  `stage_dicts` output

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

- Entities: `StageProfile` (additive fields `inputs`, `outputs`)
- Endpoints: `GET /factory/line` (new, read scope)
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: stdlib only (no new imports)

### Write set

- `framework/ai-sdlc/aisdlc/line.py`
- `framework/ai-sdlc/aisdlc/stages.py`
- `framework/ai-sdlc/aisdlc/profile.py`
- `framework/ai-sdlc/aisdlc/cli.py`
- `framework/ai-sdlc/aisdlc/serve.py`
- `framework/ai-sdlc/aisdlc/contract.py`
- `framework/ai-sdlc/tests/**`
- `framework/ai-sdlc/openapi/factory-control.openapi.yaml`
- `framework/ai-sdlc/README.md`
- `framework/ai-sdlc/CHANGELOG.md`
- `framework/ai-sdlc/templates/project/aisdlc.toml`
- `.aisdlc/aisdlc.toml`
- `docs/openspec/changes/feat-2396-production-line-model/**`
- `docs/openspec/changes/archive/**feat-2396*/**`
- `docs/openspec/specs/**`

### Architecture review

The change follows the existing hexagonal architecture: `line.py` is a pure
composition of existing ports (profile, catalogs, isolator, stages). No new
ports are introduced. The CLI and serve additions follow the existing dispatch
patterns in `cli.py` and `serve.py`. No ADR is required; this is an additive
internal observability feature within the kit.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `framework/ai-sdlc/CHANGELOG.md` | Add `[Unreleased]` entry for `aisdlc line` and `GET /factory/line` |
| `framework/ai-sdlc/README.md` | Document the `aisdlc line` command and `GET /factory/line` |

## Out of Scope

- Writing or mutating the production line (`POST /factory/line` or any write path) — tracked
  in future issues under #2393.
- The live-state dashboard or SSE event stream (#2393 D) — separate child issue.
- Storing telemetry or run history for line stages — belongs to #2393 A (store).
- Intent submission or the intent model (#2393 B) — separate child issue #2395.
