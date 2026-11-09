# Per-stage profile knobs (agents/skills/MCP/model/effort)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2182 |
| Use Case | CU76 |
| Branch | `cursor/feat-2182-stage-profile-knobs-51cd` |
| Gate 1 status | passed |

## Objetivo

Catalogs CRUD (#2139) stores skills/MCP/agents/memory entries, but the adopter profile has no first-class per-stage knobs. Foreman routing and Warp-style factories-as-code need each stage to declare which agents, skills, MCP allowlist ids, model, and effort budgets apply — validated against catalogs, never executed by list/show/validate.

## What Changes

- Optional `[[stages]]` (or equivalent) in `aisdlc.toml` keyed to an existing gate stage id, with fields `agents`, `skills`, `mcp`, `model`, `effort`.
- Resolve agent/skill/mcp ids against `[catalogs]` entries and the skills-dir overlay; unknown id → `config validate` / `doctor` error (fail closed).
- Omit / empty knobs = no-op (gates still run). List/show/validate never execute MCP or agents.
- CLI surfaces resolved knobs via `config show --redacted` and `stage list|--json`.
- Notaire maps ≥1 real stage to fleet role/skills by catalog or path reference; kit stays free of Notaire product paths.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Stage knobs may reference catalog ids for agents/skills/MCP; model and effort are declarative budgets | CU76 / #2139 unfinished AC | New |
| Unknown stage→catalog refs fail closed on validate/doctor | CU76 | New |
| List/show/validate never execute MCP servers or agent code | catalogs #2139 | Made explicit for stages |
| Empty/omitted stage block is a no-op | CU76 | New |

## Capabilities

### New Capabilities

- `stage-profiles`: Per-stage agents/skills/mcp/model/effort knobs in the profile, resolved against catalogs.

### Modified Capabilities

- `factory-foundation`: `config validate` / `config show --redacted` include stage knobs.
- `catalogs`: Stage refs resolve against catalog entries (+ skills-dir overlay).

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `ai-sdlc-framework` | yes | profile schema, validate, CLI, OpenAPI, tests, README |
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| adopter profile (`.aisdlc/`) | yes | ≥1 `[[stages]]` + catalog entries by reference |

### Surface area

- Entities: none
- Endpoints: none (OpenAPI FactoryConfig gains optional `stages` property — control surface only)
- Database: none
- Configuration: `.aisdlc/aisdlc.toml` `[[stages]]`; optional `.aisdlc/catalogs/**`
- Dependencies: none

### Write set

- `framework/ai-sdlc/**`
- `.aisdlc/aisdlc.toml`
- `.aisdlc/catalogs/**` (minimal agent/skill catalog refs)
- `docs/openspec/**`
- `docs/300-development/306-ai-sdlc-framework/**` (FACTORY-ROADMAP note if present)

### Architecture review

Follows existing profile + catalogs Ports shape; no new ADR. Does not claim Isolator/sandbox.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `framework/ai-sdlc/README.md` | Document `[[stages]]` knobs, effort units, `stage list` |
| `framework/ai-sdlc/templates/project/aisdlc.toml` | Commented example `[[stages]]` |
| `docs/300-development/306-ai-sdlc-framework/FACTORY-ROADMAP.md` | Mark stage knobs shipped when present |
| `CHANGELOG.md` | omit if Unreleased held by another PR; else brief note |

## Out of Scope

- Isolator OS runtime / fake sandbox-from-YAML
- `[committer] required = true`
- ForgePort write / GitHub check attestations
- Portal / configure mega-UI / Context Lake
- Wilson auto-promotion vanity stages
- Executing MCP or agents from the kit
