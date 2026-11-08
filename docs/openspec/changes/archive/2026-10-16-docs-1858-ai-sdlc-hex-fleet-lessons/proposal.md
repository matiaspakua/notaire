# Encode #1693 hex-fleet AI SDLC lessons into durable agent artifacts

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1858 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `docs/1858_ai_sdlc_hex_fleet_lessons` |
| Gate 1 status | passed |

## Objetivo

Real multi-agent pain from epic #1693 (FE hex drain, R2 deed/report, Mac local
gates, CI stewardship on #1844–#1846 / #1854 / #1855) keeps repeating because
lessons live in Project-store notes, not in the files agents load. Encode the
high-leverage do/don't into durable docs, skills, rules, and a small `gh` retry
so the next wave does not rediscover the same failures.

## What Changes

- Add `docs/300-development/304-ai-sdlc-cloud/FLEET-LESSONS-HEX-1693.md` —
  steward cadence, CHANGELOG fence, Mac vs CI ownership, R2 slice definition,
  issue-hygiene paste, anti-patterns.
- Add `docs/300-development/304-ai-sdlc-cloud/MAC-LOCAL-VS-CI.md` — what Mac can
  prove for hex/R2; what CI must still own.
- Extend `CI-MERGE-GATE.md`, `README.md` (304 index), steward skill, cloud-foreman,
  hexagonal rule, and `ai-agent-workflow.md` with the same rules (pointers, not
  duplicated essays).
- Retry transient `gh issue view` flakes in `validate-sdlc-plan.sh` (Mac preflight
  false failure: "Issue #N was not found").
- Guard test coverage for the new durable doc paths / steward topics.
- OpenSpec change folder with `skip_specs: true` (process/docs only).

No product code. No conflict with hex R2 product PRs (#1850 etc.).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Fleet agents follow documented merge / archive / CHANGELOG / slice hygiene | CU76, CONSTITUTION §5, CI-MERGE-GATE | Made explicit from #1693 evidence |
| Mac green does not replace CI ownership of CodeQL / Bruno / Playwright / DAST / image | CU76 | New durable split |
| One domain (R2) / one FE ratchet completion per PR; no entity on port signatures | ADR-030, #1693 | Made explicit |

## Capabilities

### New Capabilities

None — docs/process only. `skip_specs: true` is set in `.openspec.yaml`.

### Modified Capabilities

None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `docs/300-development/304-ai-sdlc-cloud/` | yes | New lessons + Mac/CI docs; extend merge gate + README |
| `.claude/skills/steward/` | yes | Pre-ready checklist + fences |
| `.claude/agents/cloud-foreman.md` | yes | Serialize domains; CHANGELOG; issue paste |
| `.claude/rules/` | yes | Hex R2 slice + workflow anti-patterns |
| `workspace/sdlc/validate-sdlc-plan.sh` | yes | `gh` lookup retry |
| `workspace/tests/` | yes | Guard tests |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

Follows existing process architecture; no ADR (documents ADR-030 fleet practice).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/304-ai-sdlc-cloud/FLEET-LESSONS-HEX-1693.md` | **New** — lesson checklists |
| `docs/300-development/304-ai-sdlc-cloud/MAC-LOCAL-VS-CI.md` | **New** — ownership split |
| `docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md` | CHANGELOG fence; sync-after-land; Process Checks before ready |
| `docs/300-development/304-ai-sdlc-cloud/README.md` | Index new docs |
| `.claude/skills/steward/SKILL.md` | Pre-ready + fences |
| `.claude/agents/cloud-foreman.md` | Hard exclusions from #1693 |
| `.claude/rules/hexagonal-architecture.md` | R2 / FE suppressions completion |
| `.claude/rules/ai-agent-workflow.md` | Anti-patterns pointer |
| `AGENTS.md` | One-line pointer to fleet lessons |
| `CHANGELOG.md` | n/a — process docs; Release Please / Unreleased fence respected (no product Unreleased entry conflicting with release trains) |

## Out of Scope

- Hex R2 product shrinks (#1850 management, #1851 workflow, …).
- Rewriting `CONSTITUTION.md` wholesale.
- Claiming or implementing open #1418 Owner-acceptance list (complement only).
- Board label writes when fleet App returns 403 (document paste; human applies).
