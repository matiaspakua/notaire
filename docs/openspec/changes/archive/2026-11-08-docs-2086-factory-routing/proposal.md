# Factory routing, Impeccable provenance, and the local Cursor handoff

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2086 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure (RNF-90 metodología de desarrollo) |
| Branch | `cursor/factory-phase1-routing-b7c4` |
| Gate 1 status | passed (`validate-sdlc-plan.sh`, `openspec validate --strict`) |

## Objetivo

Specialist prompts still teach the pre-hexagonal layering, form review is not
routed to the vendored Impeccable skill, and the repo never names the local
Cursor worker that should continue epic #1693. This change records those
routes in the AI factory docs and stops a cloud run from pretending it is
that machine.

## What Changes

- Rewrite `.claude/agents/java-architect.md` so new code follows ADR-030.
- Point `.claude/agents/backend-implementer.md` at the same rule. It no longer tells agents to add `repository` dependents.
- Route form review to vendored Impeccable (pbakaus/impeccable 4.8.1, Apache-2.0) from the frontend agent, the skills catalog, and `fleet-manifest.yaml`.
- Add `notaire-cybersec` to the security role in the manifest, matching the auditor prompt.
- Add `LOCAL-CURSOR-HANDOFF.md` and `FORMS-AUDIT-PLAN.md` under `docs/300-development/304-ai-sdlc-cloud/`.
- Report a WIP label that has no claim marker and leave the lane held (`claim-issue.py`).
- Lock the invariants in `workspace/tests/test_factory_phase1_routing.py` and `workspace/tests/test_claim_issue.py`.

No product UI or hexagonal implementation. No privilege change: the Owner still merges.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| New backend code follows Ports & Adapters; legacy packages gain no new dependents | CU76, ADR-030, `.claude/rules/hexagonal-architecture.md` | Made explicit in the specialist prompts that contradicted it |
| A source inventory of forms is not a rendered audit | CU76, RNF-05 / RNF-07 | Made explicit |
| The oMLX harness is not the local Cursor machine | CU76, cloud fleet exclusion of `local-ai/` | Made explicit |
| A WIP label without a claim marker still blocks its lane | CU76, #1646 | Made explicit |

## Capabilities

### New Capabilities

- None. Factory process only.

### Modified Capabilities

- None. `skip_specs: true` in `.openspec.yaml`. No product requirement changes.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | Release-only CI stays as it is. No new pull_request jobs |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none added. Impeccable is already vendored.

### Write set

- `.claude/agents/java-architect.md`
- `.claude/agents/backend-implementer.md`
- `.claude/agents/frontend-design.md`
- `.claude/agents/cloud-foreman.md`
- `.claude/skills/README.md`
- `AGENTS.md`
- `CHANGELOG.md`
- `docs/300-development/304-ai-sdlc-cloud/**`
- `docs/openspec/changes/docs-2086-factory-routing/**`
- `workspace/sdlc/claim-issue.py`
- `workspace/tests/test_factory_phase1_routing.py`
- `workspace/tests/test_claim_issue.py`

### Architecture review

Follows ADR-030 by deleting contradictory agent instructions. No new ADR. Product packages are untouched.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/304-ai-sdlc-cloud/LOCAL-CURSOR-HANDOFF.md` | New. Local Cursor commands, open hex issues, specialist plan |
| `docs/300-development/304-ai-sdlc-cloud/FORMS-AUDIT-PLAN.md` | New. Code inventory and the next rendered issues |
| `docs/300-development/304-ai-sdlc-cloud/README.md` | Index the two docs |
| `docs/300-development/304-ai-sdlc-cloud/FLEET-ARCHITECTURE.md` | Frontend and security skill lists; pointer away from `foreman.sh` |
| `docs/300-development/304-ai-sdlc-cloud/fleet-manifest.yaml` | Dispatch and skill lists |
| `AGENTS.md` | On-demand frontend row names Impeccable |
| `.claude/skills/README.md` | Product UI row names Impeccable provenance |
| `CHANGELOG.md` | One `[Unreleased]` line for the factory docs |

## Out of Scope

- Implementing #577, #580, or #1693 in this change. Those stay later issue-sized cloud slices. Do not start a private worker.
- Rendering or editing product forms. Next slices are #2087 and #2088.
- Merging, force-push, rebase, or changing GitHub rulesets.
- Refreshing `.impeccable/design.json`.
- Closing #577. It remains the in-progress backend lane.
