# fix(aisdlc): /factory/line stages omit agents, skills, tools, model, effort and guardrails

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2404 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `fix/2404_line_stage_metadata` |
| Gate 1 status | passed |

## Objetivo

On main, `GET /factory/line` and `aisdlc line --json` return only `id, gates, inputs, outputs, io_source` for each stage. The shared contract in #2393 (Line section C) specifies that every stage SHALL carry agents, skills, tools/MCP, model, effort, and guardrails. The dashboard (#2400) currently works around this gap by merging `GET /factory/config` stages[] into the line payload — a deviation that should be removed once this fix lands.

## What Changes

- **`framework/ai-sdlc/aisdlc/line.py`** — `build_line()` now enriches every stage dict with `agents`, `skills`, `mcp`, `tools`, `model`, `effort`, and `guardrails` drawn from `stage_dicts(profile)` (for stages with a `[[stages]]` block) and `profile.grants` (for per-stage guardrails). Gate-only stages (no `[[stages]]` block) get empty lists, `null` model, and `{}` effort.
- **`framework/ai-sdlc/aisdlc/stages.py`** — no code change required; `stage_dicts(profile)` already returns the needed keys. `build_line` reuses it as the single source of truth.
- **`framework/ai-sdlc/aisdlc/serve.py`** — no code change required; `GET /factory/line` already calls `build_line(ctx)`.
- **`framework/ai-sdlc/openapi/factory-control.openapi.yaml`** — `LineStage` schema gets `required` expanded to include all keys (`id, gates, agents, skills, mcp, tools, model, effort, guardrails, inputs, outputs, io_source`), a `guardrails` array property added, and `contract info.version` minor bump (1.3.0 → 1.4.0).
- **`framework/ai-sdlc/tests/test_line*.py`** — new test with a realistic profile pinning all per-stage keys.
- **`framework/ai-sdlc/README.md`** — update the `aisdlc line` section to reflect the enriched stage payload.
- **`framework/ai-sdlc/CHANGELOG.md`** — add `[Unreleased]` entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Every stage in the line payload ALWAYS carries keys: agents, skills, mcp, tools, model, effort, guardrails — never missing | #2393 Line contract (C), #2404 | Made explicit |
| `stage_dicts(profile)` is the single source of truth for per-stage metadata (agents, skills, mcp, model, effort); `build_line` reuses it | #2404 design decision | Made explicit |
| Per-stage guardrails = `[[grants]]` entries whose `stage` field equals the stage id, serialized via `grant_dict` with `"kind": "grant"` added; empty list when none | #2404 design decision | New |
| `tools` equals `mcp` ids — the profile has no other tool source | #2404 design decision | Made explicit |
| Gate-only stages (no `[[stages]]` block) get empty lists, `null` model, `{}` effort | #2404 design decision | New |
| The top-level payload `guardrails` block is unchanged; per-stage `guardrails` is a new array on each stage | #2404 design decision | New |

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `aisdlc-production-line`: every stage in the line payload now carries agents, skills, mcp, tools, model, effort, and guardrails (additive keys, never missing)

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

- Entities: none (pure Python kit change)
- Endpoints: `GET /factory/line` response shape enriched (additive keys on each stage; backward-compatible for consumers that ignore unknown keys)
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: stdlib only (no new imports)

### Write set

- `framework/ai-sdlc/aisdlc/line.py`
- `framework/ai-sdlc/aisdlc/stages.py`
- `framework/ai-sdlc/aisdlc/serve.py`
- `framework/ai-sdlc/tests/test_line*.py`
- `framework/ai-sdlc/openapi/factory-control.openapi.yaml`
- `framework/ai-sdlc/README.md`
- `framework/ai-sdlc/CHANGELOG.md`
- `docs/openspec/changes/**`
- `docs/openspec/specs/**`

### Architecture review

The change follows the existing composition pattern: `line.py` calls `stage_dicts(profile)` and `grants.grant_dict()` — no new extraction logic. No ADR required; additive enrichment of an existing payload.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `framework/ai-sdlc/CHANGELOG.md` | Add `[Unreleased]` entry for enriched stage metadata in line payload |
| `framework/ai-sdlc/README.md` | Update `aisdlc line` section to document the new per-stage keys |

## Out of Scope

- The dashboard (#2400) config-merge workaround removal — that is a follow-up in the dashboard branch, not this write set.
- Writing or mutating the production line — tracked in future issues under #2393.
- Any change to the top-level `guardrails` block shape — it stays as-is.
