# Encode post-#1859 fleet AI SDLC lessons (delta)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1907 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/docs-ai-sdlc-lessons-delta-69d3` |
| Gate 1 status | passed |

## Objetivo

PR [#1859](https://github.com/matiaspakua/notaire/pull/1859) encoded the first
#1693 hex-fleet lesson set. The subsequent R2 wave (#1857–#1902) and Process
Checks failures (notably #1894 path refs after moving
`RegistrationDraftService`) exposed **gaps**: package-move hygiene, paste
templates, fleet cost/serialize guidance, and stronger Mac≠CI wording. Encode
those deltas without re-shipping #1859 content.

## What Changes

- Extend `FLEET-LESSONS-HEX-1693.md` with R2 path hygiene, human paste templates,
  performance/cost serialize, Docker runbook-only status, anti-pattern rows.
- Strengthen `MAC-LOCAL-VS-CI.md` (**Mac green ≠ CI green**) and
  `CI-MERGE-GATE.md` Process Checks / cost notes.
- Point steward, cloud-foreman, hexagonal rule, and `ai-agent-workflow.md` at the
  deltas (checklists, not essays).
- Harden `validate-sdlc-plan.sh` `gh` lookup retries (5 attempts, longer backoff).
- Guard tests `workspace/tests/test_fleet_lessons_1907.py`.
- OpenSpec change with `skip_specs: true` (process/docs only).
- `CHANGELOG.md` `[Unreleased]` Documentation entry.

No hexagonal product code. No Dockerfile re-change (`-U` already on `main`).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| After R2 helper moves, same-PR docs/tests path refs + INVENTORY | CU76, ADR-030, #1894 | New durable checklist |
| Bot 403 → human paste **templates** (not prose only) | CU76, #1858 | Made actionable |
| Serialize heavy CI; prefer domain-sized slices; Mac ≠ CI | CU76, CI-MERGE-GATE | Made explicit (cost) |
| `gh` issue lookup retries under preflight load | CU76, #1858 | Hardened (#1907) |

## Capabilities

### New Capabilities

None — docs/process only. `skip_specs: true` is set in `.openspec.yaml`.

### Modified Capabilities

None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | Dockerfile already fixed in #1859 — runbook only |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `docs/300-development/304-ai-sdlc-cloud/` | yes | Extend lessons, Mac/CI, merge gate, README |
| `.claude/skills/steward/` | yes | Path hygiene + paste templates pointer |
| `.claude/agents/cloud-foreman.md` | yes | Cost serialize; Mac≠CI; paste templates |
| `.claude/rules/` | yes | Hex R2 path hygiene; workflow Never rows |
| `workspace/sdlc/validate-sdlc-plan.sh` | yes | Retry 5 + backoff |
| `workspace/tests/` | yes | `test_fleet_lessons_1907.py` |
| `CHANGELOG.md` | yes | Unreleased Documentation bullet |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

Follows existing `304-ai-sdlc-cloud/` + steward/foreman pattern from #1859. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/304-ai-sdlc-cloud/FLEET-LESSONS-HEX-1693.md` | Delta sections (path hygiene, paste templates, cost) |
| `docs/300-development/304-ai-sdlc-cloud/MAC-LOCAL-VS-CI.md` | Mac ≠ CI green; path hygiene pointer |
| `docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md` | Process Checks path refs + cost |
| `docs/300-development/304-ai-sdlc-cloud/README.md` | Index wording |
| `.claude/skills/steward/SKILL.md` | Pre-ready path hygiene + templates |
| `.claude/agents/cloud-foreman.md` | Hard exclusions for cost / Mac≠CI |
| `.claude/rules/hexagonal-architecture.md` | R2 helper move + path hygiene |
| `.claude/rules/ai-agent-workflow.md` | Fleet lessons + Never rows |
| `CHANGELOG.md` | Unreleased Documentation entry |

## Out of Scope

- Re-documenting #1859 topics already covered (heavy-CI serialize basics, merge-never-rebase, FE `{}`, Docker `-U` code change).
- Hex R2 leftover product domains (folio/item/user/property).
- Closing board leftovers / Projects writes (paste templates only).
