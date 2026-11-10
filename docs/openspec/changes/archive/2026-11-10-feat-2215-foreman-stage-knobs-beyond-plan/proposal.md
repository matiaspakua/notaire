# Notaire Foreman stage knobs beyond plan

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2215 |
| Use Case | CU76 |
| Branch | `feat/2215_foreman-stage-knobs-beyond-plan` |
| Gate 1 status | passed |

## Objetivo

After #2214 honesty, `aisdlc foreman plan --json` still maps only `plan`
(`mapped_count: 1`). Fill real Notaire `[[stages]]` knobs for additional gate
stages (at least red and pre-pr; pre-push as G3 local stand-in where `green` has
no gate) from existing fleet agents/skills — no vanity knobs, no invented MCP.

## What Changes

- Register real fleet agent path refs in `.aisdlc/catalogs/agents/` for roles used
  by new stage knobs
- Declare `[[stages]]` for `red`, `pre-push`, and `pre-pr` (keep `plan`) with
  agents/skills/model/effort drawn from fleet-manifest + skills-dir
- Leave `mcp = []` (empty MCP catalog — no fake MCP ids)
- Document: no Notaire `[[gates]] stage = "green"`; G3 is `pre-push`/`pre-pr`;
  unmapped honesty remains for `merge` / `release` / `signal`
- FACTORY-ROADMAP + remaining-gaps note; CHANGELOG; TDD only if kit behavior changes
  (expected: adopter profile/catalog only + Notaire verify assertions)

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Foreman stage knobs MUST reference real catalog/skills-dir/fleet ids only | CU76 / #2182 / #2215 | Made explicit for Notaire beyond plan |
| Kit MUST NOT invent fake agents/MCP/stages to inflate `mapped_count` | #2211 / #2215 KISS | Made explicit |
| Notaire has no `green` gate stage; G3 verified uses `pre-push` + `pre-pr` | profile STAGES / #2215 | Made explicit |
| Unmapped honesty remains for stages still without knobs | #2211 / FACTORY-ROADMAP | Unchanged |
| Never flip `[committer] required=true`; no Wilson gaming; no portal | Owner / #2215 out of scope | Made explicit |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `stage-profiles`: Notaire adopter maps ≥4 gate stages with real knobs; green
  omission documented when no green gate exists
- `foreman-routing`: Notaire `mapped_count` ≥ 4 with real knobs; unmapped list
  still honest for remaining gate stages

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | preflight --full only |
| `workspace` (harness) | yes | `.aisdlc` profile + agent catalogs; FACTORY-ROADMAP; optional kit Notaire verify assertion |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: `.aisdlc/aisdlc.toml` `[[stages]]`; `.aisdlc/catalogs/agents/**`
- Dependencies: none

### Write set

- `.aisdlc/aisdlc.toml`
- `.aisdlc/catalogs/agents/**`
- `framework/ai-sdlc/tests/**` (only if kit Notaire verify / adopter assertions change)
- `docs/openspec/**`
- `docs/300-development/306-ai-sdlc-framework/FACTORY-ROADMAP.md`
- `CHANGELOG.md`

### Architecture review

No new ADR — uses shipped `[[stages]]` (#2182) + Foreman plan (#2196) + catalog
path refs. Does not claim Isolator/sandbox or execute agents/MCP.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/306-ai-sdlc-framework/FACTORY-ROADMAP.md` | Notaire maps ≥4 stages; green omission + remaining unmapped honesty |
| `docs/openspec/specs/stage-profiles/spec.md` | Sync on archive if delta requires |
| `docs/openspec/specs/foreman-routing/spec.md` | Sync on archive if delta requires |
| `CHANGELOG.md` | `[Unreleased]` Notaire Foreman stage knobs beyond plan |

## Out of Scope

Wilson L1, `[committer] required=true`, portal/Context Lake, inventing MCP/agents,
adding a vanity `green` gate solely to raise `mapped_count`, merge/release knobs
unless clearly needed for ≥4.
