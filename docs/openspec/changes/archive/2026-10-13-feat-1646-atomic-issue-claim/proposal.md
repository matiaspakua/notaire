# Atomic issue claim with lane lock and stale-claim release

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1646 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `feat/1646_atomic-issue-claim` |
| Gate 1 status | passed |

## Objetivo

"Claim atomically" meant a label write and a Status write after a read. Two agents
reading together both win, and a dead agent's `in-progress` blocks its lane
forever. Parallel fleets need a mechanical claim.

## What Changes

- New `workspace/sdlc/claim-issue.py`: refusals, lane lock, a marker-race claim, and stale release.
- `PARALLEL-AGENTS.md`, `cloud-foreman.md` and the `project-management` skill claim only through it.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The earliest claim marker since the last release owns the issue | #1646 | New |
| A claim with no open PR after 24 h is released | #1646 | New |
| Lane conflict matrix | PARALLEL-AGENTS.md | Unchanged, now enforced |

## Capabilities

### New Capabilities
- `issue-claim-protocol`: atomic claim and stale release.

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
| `workspace/sdlc` + PM docs | yes | new script; protocol text |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: `GH_TOKEN` (fleet identity, #1648) optional
- Dependencies: `gh` CLI

### Architecture review

Reuses `milestone_policy.lane` / `lanes_blocked_by`, so there is one lane definition.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/305-project-management/PARALLEL-AGENTS.md` | Step 7; stale release |
| `.claude/agents/cloud-foreman.md` | Claim via script |
| `.claude/skills/project-management/SKILL.md` | Claim via script |
| `CHANGELOG.md` | n/a — not user visible |

## Out of Scope

- The GitHub App identity (#1648); local-ai harness integration (the next foreman change).
