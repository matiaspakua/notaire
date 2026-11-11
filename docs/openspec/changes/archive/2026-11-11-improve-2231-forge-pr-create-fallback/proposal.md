# Document forge PR-create 403 and ManagePullRequest fallback

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2231 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `docs/2231_forge-pr-create-fallback` |
| Gate 1 status | passed |

## Objetivo

Ambient Cursor `gh pr create` can fail with GraphQL `createPullRequest` **403**
even when branch push works (ASLI-66 #2073 → Owner compare URL #2228; ASLI-67
#2071 → ManagePullRequest on `cursor/*` tip #2229). Agents need a durable
runbook so PR open does not stall the factory.

## What Changes

- Durable docs in `docs/300-development/304-ai-sdlc-cloud/` describe ambient
  `gh` `createPullRequest` 403, preferred push `cursor/<desc>` (+ optional
  `type/#_desc`) then ManagePullRequest / Cursor PR tools, and Owner
  compare-link fallback with exact title/body.
- One short paragraph in steward skill and/or ai-agent-workflow / cloud-foreman
  pointing at that path.
- Optional `FLEET_APP_*` remains documented for labels (#2221); do **not** claim
  fleet App PR scopes were granted unless the Owner configures them.
- Unittest locks the docs wording (TDD).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Ambient Cursor `gh` MAY lack GraphQL `createPullRequest`; agents MUST NOT treat a 403 as a product defect | CU76 / #2231 | Made explicit |
| Preferred agent PR path: push `cursor/<desc>` (optionally also `type/#_desc`) then open via ManagePullRequest / Cursor PR tools | CU76 / #2231 | New |
| If ManagePullRequest also fails, leave an Owner compare-link with exact title and body | CU76 / #2231 / ASLI-66 | New |
| Do not claim fleet App PR scopes were granted unless configured; `FLEET_APP_*` remains optional for labels via `fleet-token.sh` | CU76 / #2231 / #2221 | Made explicit |

## Capabilities

### New Capabilities

- `forge-pr-create-fallback`: Document ambient `gh` PR-create 403, ManagePullRequest preferred path, and Owner compare-link fallback.

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
| `workspace` (harness) | yes | docs unittest only |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none (Owner may add `FLEET_APP_*` offline; already documented)
- Dependencies: none

### Write set

- `docs/300-development/304-ai-sdlc-cloud/FLEET-IDENTITY.md`
- `docs/300-development/304-ai-sdlc-cloud/FLEET-LESSONS-HEX-1693.md`
- `docs/300-development/304-ai-sdlc-cloud/README.md`
- `.claude/skills/steward/SKILL.md`
- `.claude/skills/ai-agent-workflow/SKILL.md`
- `.claude/agents/cloud-foreman.md`
- `.claude/rules/ai-agent-workflow.md`
- `workspace/tests/test_forge_pr_create_fallback_docs.py`
- `CHANGELOG.md`
- `docs/openspec/changes/improve-2231-forge-pr-create-fallback/**`

### Architecture review

Docs/process only; extends #2221 / #1648 fleet identity lessons. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/304-ai-sdlc-cloud/FLEET-IDENTITY.md` | Section on ambient `gh` PR-create 403 vs ManagePullRequest / Owner compare |
| `docs/300-development/304-ai-sdlc-cloud/FLEET-LESSONS-HEX-1693.md` | Do/don’t row + short runbook for forge PR open |
| `docs/300-development/304-ai-sdlc-cloud/README.md` | Index pointer if useful |
| `.claude/skills/steward/SKILL.md` | One short paragraph pointing at the path |
| `.claude/skills/ai-agent-workflow/SKILL.md` | PR step points at ManagePullRequest fallback |
| `.claude/agents/cloud-foreman.md` | PR / merge section points at the path |
| `.claude/rules/ai-agent-workflow.md` | Step 18 notes fallback |
| `CHANGELOG.md` | Documentation entry under `[Unreleased]` |

## Out of Scope

Product UI. Wilson. Portal. Context Lake. `[committer] required=true`. Granting
Cursor App or fleet scopes from the agent. Weakening Gate 1. Label SKIP (#2221
already covered).
