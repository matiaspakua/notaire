# Memory catalog ↔ always-loaded context budget integration

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2186 |
| Use Case | CU76 |
| Branch | `cursor/feat-2186-memory-context-budget-10f2` |
| Gate 1 status | passed |

## Objetivo

The memory catalog kind (#2139) and the always-loaded context budget (`aisdlc budget` / Notaire pre-push `agent-context-budget.py`) are separate. Gap audit rank 3 asks to wire memory pack ids into profile `[context]` so validate/budget fail closed on unknown packs without executing packs, Isolator, or a portal.

## What Changes

- Profile `[context]` gains optional `memory = ["pack-id", …]` referencing memory catalog entries.
- `config validate` / `doctor` resolve pack ids against `[catalogs]` memory kind; unknown id → error.
- `aisdlc budget` measures `[context].files` plus resolved pack `ref` paths (bytes/4; dedupe); missing pack/ref fails.
- List/validate/show/budget never execute pack payloads.
- Notaire declares ≥1 memory pack by catalog reference; kit stays free of product paths.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Always-loaded context may reference memory catalog pack ids | CU76 / gap audit rank 3 | New |
| Unknown memory pack refs fail closed on validate/doctor/budget | CU76 | New |
| List/validate/show/budget never execute memory packs | catalogs #2139 | Made explicit for context budget |
| Empty/omitted `memory` is a no-op for validation | CU76 | New |

## Capabilities

### New Capabilities

- `context-budget`: Profile `[context].memory` pack ids resolve against the memory catalog and participate in `aisdlc budget`.

### Modified Capabilities

- `catalogs`: Context budget refs resolve against memory catalog entries (read-only).
- `factory-foundation`: `config validate` / `config show --redacted` include context memory pack refs.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `workspace` (harness) | yes | framework/ai-sdlc profile + budget + validate + tests; `.aisdlc` memory pack |

### Surface area

- Entities: none
- Endpoints: none (OpenAPI FactoryConfig optional `context.memory`)
- Database: none
- Configuration: `.aisdlc/aisdlc.toml` `[context]`; `.aisdlc/catalogs/memory/**`
- Dependencies: none

### Write set

- `framework/ai-sdlc/**`
- `.aisdlc/aisdlc.toml`
- `.aisdlc/catalogs/memory/**`
- `docs/openspec/**`
- `docs/300-development/306-ai-sdlc-framework/FACTORY-ROADMAP.md`
- `CHANGELOG.md`

### Architecture review

Extends existing profile + catalogs shape (same fail-closed pattern as #2182 stages). No new ADR. Does not claim Isolator/sandbox.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `framework/ai-sdlc/README.md` | Document `[context].memory` + budget resolution |
| `framework/ai-sdlc/templates/project/aisdlc.toml` | Comment example for memory packs |
| `docs/300-development/306-ai-sdlc-framework/FACTORY-ROADMAP.md` | Note memory↔budget integration landed |
| `CHANGELOG.md` | Unreleased note |

## Out of Scope

Isolator OS runtime; `[committer] required=true`; ForgePort write; portal / Context Lake; executing memory packs; changing the #1259 bytes/4 heuristic
